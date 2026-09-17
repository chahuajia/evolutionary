package com.evolutionary.operator.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.EffectiveProduct;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgAuthorization;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.OverrideStatus;
import com.evolutionary.operator.domain.PackageOverride;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** AC-26 / AC-27 / AC-31：套餐覆盖、有效价与撤销审计。 */
class PackageOverrideEffectiveProductTest {

    private static final Instant FIXED = Instant.parse("2026-09-17T05:00:00Z");

    private Organization l1;
    private Organization l2;
    private InMemoryTemplateRepo templates;
    private InMemoryOverrideRepo overrides;
    private InMemoryAuditRepo audits;
    private InMemoryOrgRepo orgs;
    private ActivatePackageOverride activate;
    private RevokePackageOverride revoke;
    private ResolveEffectiveProduct resolve;
    private PublishPackageTemplate publish;

    @BeforeEach
    void setUp() {
        l1 = Organization.createRoot("ORG-L1", "华南", List.of("GD", "SZ"));
        l2 = Organization.createChild("ORG-L2", "深圳", "ORG-L1", List.of("SZ"));
        templates = new InMemoryTemplateRepo();
        overrides = new InMemoryOverrideRepo();
        audits = new InMemoryAuditRepo();
        orgs = new InMemoryOrgRepo();
        orgs.save(l1);
        orgs.save(l2);
        Clock clock = Clock.fixed(FIXED, ZoneOffset.UTC);
        OrgAuthorization auth = new OrgAuthorization(OrgAuthorization.index(l1, l2));
        activate = new ActivatePackageOverride(templates, overrides, audits, auth, clock);
        revoke = new RevokePackageOverride(overrides, audits, clock);
        resolve = new ResolveEffectiveProduct(templates, overrides);
        publish = new PublishPackageTemplate(templates, audits, orgs, clock);

        TemplateBaseProduct base = TemplateBaseProduct.of("30天卡", 3000, 30);
        templates.save(
                PackageTemplate.createDraft(
                        "T1",
                        l1.id(),
                        base,
                        List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME)));
        assertInstanceOf(OperatorOutcome.Ok.class, publish.execute("U-L1", l1.id(), "T1"));
    }

    @Test
    @DisplayName("AC-26：无覆盖 → EffectiveProduct 原价 30.00；激活 price=28.00 → 有效价 28.00")
    void effectivePriceInheritsThenOverrides() {
        EffectiveProduct before = resolve.execute(l2.id(), "T1");
        assertEquals(3000, before.priceCents());
        assertEquals(30, before.durationDays());

        OperatorOutcome<PackageOverride> activated =
                activate.execute(
                        "U-SZ", l2.id(), "T1", "OV-1", Map.of("price", 2800L));
        assertInstanceOf(OperatorOutcome.Ok.class, activated);
        PackageOverride ov = ((OperatorOutcome.Ok<PackageOverride>) activated).value();
        assertEquals(OverrideStatus.ACTIVE, ov.status());
        assertEquals(2800L, ov.patches().priceCents().orElseThrow());

        EffectiveProduct after = resolve.execute(l2.id(), "T1");
        assertEquals(2800, after.priceCents());
        assertEquals("OV-1", after.overrideId());
        assertEquals(30, after.durationDays());

        List<AuditLog> activateLogs =
                audits.findByResourceId("OV-1").stream()
                        .filter(l -> l.action() == AuditAction.OVERRIDE_ACTIVATE)
                        .toList();
        assertEquals(1, activateLogs.size());
    }

    @Test
    @DisplayName("AC-27：patch durationDays → FIELD_NOT_OVERRIDABLE，无 PackageOverride 激活")
    void illegalFieldRejected() {
        OperatorOutcome<PackageOverride> rejected =
                activate.execute(
                        "U-SZ", l2.id(), "T1", "OV-BAD", Map.of("durationDays", 60));
        assertInstanceOf(OperatorOutcome.Err.class, rejected);
        OperatorOutcome.Err<PackageOverride> err =
                (OperatorOutcome.Err<PackageOverride>) rejected;
        assertEquals(OperatorErrorCode.FIELD_NOT_OVERRIDABLE, err.code());

        assertTrue(overrides.findById("OV-BAD").isEmpty());
        assertTrue(overrides.findActiveByOrgAndTemplate(l2.id(), "T1").isEmpty());
        assertEquals(3000, resolve.execute(l2.id(), "T1").priceCents());
    }

    @Test
    @DisplayName("AC-31：撤销覆盖 → AuditLog override.revoke，目录回落模板原价")
    void revokeRestoresTemplatePrice() {
        assertInstanceOf(
                OperatorOutcome.Ok.class,
                activate.execute(
                        "U-SZ", l2.id(), "T1", "OV-1", Map.of("price", 2800L)));
        assertEquals(2800, resolve.execute(l2.id(), "T1").priceCents());

        OperatorOutcome<PackageOverride> revoked =
                revoke.execute("U-SZ", l2.id(), "OV-1");
        assertInstanceOf(OperatorOutcome.Ok.class, revoked);
        assertEquals(
                OverrideStatus.REVOKED,
                ((OperatorOutcome.Ok<PackageOverride>) revoked).value().status());

        assertEquals(3000, resolve.execute(l2.id(), "T1").priceCents());

        List<AuditLog> revokeLogs =
                audits.findByResourceId("OV-1").stream()
                        .filter(l -> l.action() == AuditAction.OVERRIDE_REVOKE)
                        .toList();
        assertEquals(1, revokeLogs.size());
        assertEquals("PackageOverride", revokeLogs.get(0).resourceType());
        assertEquals(l2.id(), revokeLogs.get(0).orgId());
    }

    private static final class InMemoryTemplateRepo implements PackageTemplateRepository {
        private final Map<String, PackageTemplate> store = new HashMap<>();

        @Override
        public void save(PackageTemplate template) {
            store.put(template.id(), template);
        }

        @Override
        public Optional<PackageTemplate> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    private static final class InMemoryOverrideRepo implements PackageOverrideRepository {
        private final Map<String, PackageOverride> store = new HashMap<>();

        @Override
        public void save(PackageOverride override) {
            store.put(override.id(), override);
        }

        @Override
        public Optional<PackageOverride> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<PackageOverride> findActiveByOrgAndTemplate(
                String orgId, String templateId) {
            return store.values().stream()
                    .filter(o -> o.orgId().equals(orgId))
                    .filter(o -> o.templateId().equals(templateId))
                    .filter(PackageOverride::isActive)
                    .findFirst();
        }
    }

    private static final class InMemoryAuditRepo implements AuditLogRepository {
        private final List<AuditLog> store = new ArrayList<>();

        @Override
        public void append(AuditLog log) {
            store.add(log);
        }

        @Override
        public List<AuditLog> findByResourceId(String resourceId) {
            return store.stream().filter(l -> l.resourceId().equals(resourceId)).toList();
        }
    }

    private static final class InMemoryOrgRepo implements OrganizationRepository {
        private final Map<String, Organization> store = new HashMap<>();

        @Override
        public void save(Organization org) {
            store.put(org.id(), org);
        }

        @Override
        public Optional<Organization> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }
}
