package com.evolutionary.operator.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import com.evolutionary.operator.domain.TemplateStatus;
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

/** AC-24 / AC-25：草稿发布与 published 不可原地改。 */
class PackageTemplatePublishTest {

    private static final Instant FIXED = Instant.parse("2026-09-17T04:00:00Z");

    private Organization l1;
    private InMemoryTemplateRepo templates;
    private InMemoryAuditRepo audits;
    private InMemoryOrgRepo orgs;
    private PublishPackageTemplate publish;
    private MutatePackageTemplateBaseProduct mutate;

    @BeforeEach
    void setUp() {
        l1 = Organization.createRoot("ORG-L1", "华南", List.of("GD", "SZ"));
        templates = new InMemoryTemplateRepo();
        audits = new InMemoryAuditRepo();
        orgs = new InMemoryOrgRepo();
        orgs.save(l1);
        Clock clock = Clock.fixed(FIXED, ZoneOffset.UTC);
        publish = new PublishPackageTemplate(templates, audits, orgs, clock);
        mutate = new MutatePackageTemplateBaseProduct(templates);
    }

    @Test
    @DisplayName("AC-24：L1 草稿发布 → published version=1，并记 AuditLog")
    void publishDraftRecordsAudit() {
        TemplateBaseProduct base = TemplateBaseProduct.of("30天卡", 3000, 30);
        PackageTemplate draft =
                PackageTemplate.createDraft(
                        "T1",
                        l1.id(),
                        base,
                        List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME));
        templates.save(draft);

        OperatorOutcome<PackageTemplate> outcome = publish.execute("U-ADMIN", l1.id(), "T1");
        assertInstanceOf(OperatorOutcome.Ok.class, outcome);
        PackageTemplate published = ((OperatorOutcome.Ok<PackageTemplate>) outcome).value();

        assertEquals(TemplateStatus.PUBLISHED, published.status());
        assertEquals(1, published.version());
        assertEquals(FIXED, published.publishedAt());
        assertEquals(l1.id(), published.ownerOrgId());

        List<AuditLog> logs = audits.findByResourceId("T1");
        assertEquals(1, logs.size());
        assertEquals(AuditAction.TEMPLATE_PUBLISH, logs.get(0).action());
        assertEquals("PackageTemplate", logs.get(0).resourceType());
        assertEquals("U-ADMIN", logs.get(0).actorUserId());
        assertEquals(l1.id(), logs.get(0).orgId());
    }

    @Test
    @DisplayName("AC-25：published 原地改 durationDays → TEMPLATE_IMMUTABLE；须新建 version 2 草稿")
    void publishedCannotMutateInPlace() {
        TemplateBaseProduct base = TemplateBaseProduct.of("30天卡", 3000, 30);
        templates.save(
                PackageTemplate.createDraft(
                        "T1",
                        l1.id(),
                        base,
                        List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME)));
        assertInstanceOf(
                OperatorOutcome.Ok.class, publish.execute("U-ADMIN", l1.id(), "T1"));

        OperatorOutcome<PackageTemplate> rejected =
                mutate.execute(l1.id(), "T1", base.withDurationDays(60));
        assertInstanceOf(OperatorOutcome.Err.class, rejected);
        OperatorOutcome.Err<PackageTemplate> err =
                (OperatorOutcome.Err<PackageTemplate>) rejected;
        assertEquals(OperatorErrorCode.TEMPLATE_IMMUTABLE, err.code());

        PackageTemplate still = templates.get("T1");
        assertEquals(30, still.baseProduct().durationDays());
        assertEquals(TemplateStatus.PUBLISHED, still.status());

        OperatorOutcome<PackageTemplate> v2 =
                still.createNextVersionDraft("T1-v2", base.withDurationDays(60));
        assertInstanceOf(OperatorOutcome.Ok.class, v2);
        PackageTemplate draftV2 = ((OperatorOutcome.Ok<PackageTemplate>) v2).value();
        assertEquals(2, draftV2.version());
        assertTrue(draftV2.isDraft());
        assertEquals(60, draftV2.baseProduct().durationDays());
        assertNotNull(draftV2);
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
