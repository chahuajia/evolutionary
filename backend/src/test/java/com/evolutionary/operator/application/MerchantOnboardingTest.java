package com.evolutionary.operator.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.mall.application.MerchantProfileRepository;
import com.evolutionary.mall.domain.MerchantProfile;
import com.evolutionary.operator.domain.AuditAction;
import com.evolutionary.operator.domain.AuditLog;
import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.OverridableField;
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

/** AC-40：商家入驻授予 MERCHANT，且不可发布 PackageTemplate；切片30b 审计。 */
class MerchantOnboardingTest {

    private static final Instant FIXED = Instant.parse("2026-09-17T06:00:00Z");

    private InMemoryOrgRepo orgs;
    private InMemoryOnboardingRepo applications;
    private InMemoryMerchantRepo merchants;
    private InMemoryTemplateRepo templates;
    private InMemoryAuditRepo audits;
    private ApproveMerchantOnboarding approve;
    private PublishPackageTemplate publish;
    private Clock clock;

    @BeforeEach
    void setUp() {
        orgs = new InMemoryOrgRepo();
        applications = new InMemoryOnboardingRepo();
        merchants = new InMemoryMerchantRepo();
        templates = new InMemoryTemplateRepo();
        audits = new InMemoryAuditRepo();
        clock = Clock.fixed(FIXED, ZoneOffset.UTC);
        approve = new ApproveMerchantOnboarding(applications, orgs, merchants, audits, clock);
        publish = new PublishPackageTemplate(templates, audits, orgs, clock);
    }

    @Test
    @DisplayName("AC-40：批准 MERCHANT 入驻 → MerchantProfile.active，且禁发 PackageTemplate")
    void approveMerchantCannotPublishTemplate() {
        Organization merchantOnly =
                Organization.create(
                        "ORG-M1",
                        "小店",
                        null,
                        List.of(),
                        List.of("SZ"),
                        com.evolutionary.operator.domain.Organization.Status.ACTIVE);
        orgs.save(merchantOnly);

        OnboardingApplication app =
                OnboardingApplication.submit(
                        "ONB-1", merchantOnly.id(), OrgCapability.MERCHANT, FIXED.minusSeconds(60));
        applications.save(app);

        OperatorOutcome<MerchantProfile> outcome =
                approve.execute("ONB-1", "黑鸟旗舰店", "U-PLATFORM", "PLATFORM");
        assertInstanceOf(OperatorOutcome.Ok.class, outcome);
        MerchantProfile profile = ((OperatorOutcome.Ok<MerchantProfile>) outcome).value();
        assertEquals(MerchantProfile.Status.ACTIVE, profile.status());
        assertTrue(profile.isActive());
        assertEquals("ORG-M1", profile.orgId());

        List<AuditLog> logs = audits.findByResourceId("ONB-1");
        assertEquals(1, logs.size());
        assertEquals(AuditAction.ONBOARDING_APPROVE, logs.get(0).action());
        assertEquals("OnboardingApplication", logs.get(0).resourceType());
        assertEquals("U-PLATFORM", logs.get(0).actorUserId());
        assertEquals("PLATFORM", logs.get(0).orgId());

        Organization after = orgs.get("ORG-M1");
        assertTrue(after.hasCapability(OrgCapability.MERCHANT));
        assertFalse(after.hasCapability(OrgCapability.OPERATOR));
        assertFalse(after.canPublishPackageTemplate());

        TemplateBaseProduct base = TemplateBaseProduct.of("30天卡", 3000, 30);
        templates.save(
                PackageTemplate.createDraft(
                        "T-M",
                        "ORG-M1",
                        base,
                        List.of(OverridableField.PRICE)));

        OperatorOutcome<PackageTemplate> rejected = publish.execute("U-M", "ORG-M1", "T-M");
        assertInstanceOf(OperatorOutcome.Err.class, rejected);
        assertEquals(
                OperatorErrorCode.CAPABILITY_DENIED,
                ((OperatorOutcome.Err<PackageTemplate>) rejected).code());
    }

    @Test
    @DisplayName("切片30b：批准 APP-M1 → findByResourceId 有 ONBOARDING_APPROVE")
    void approveAppM1RecordsAudit() {
        Organization merchantOnly =
                Organization.create(
                        "ORG-NEW",
                        "新商家待入驻",
                        null,
                        List.of(),
                        List.of("SZ"),
                        com.evolutionary.operator.domain.Organization.Status.ACTIVE);
        orgs.save(merchantOnly);
        applications.save(
                OnboardingApplication.submit(
                        "APP-M1",
                        merchantOnly.id(),
                        OrgCapability.MERCHANT,
                        FIXED.minusSeconds(60)));

        OperatorOutcome<MerchantProfile> outcome =
                approve.execute("APP-M1", "黑鸟旗舰店", "U-PLATFORM", "PLATFORM");
        assertInstanceOf(OperatorOutcome.Ok.class, outcome);

        List<AuditLog> logs = audits.findByResourceId("APP-M1");
        assertEquals(1, logs.size());
        assertEquals(AuditAction.ONBOARDING_APPROVE, logs.get(0).action());
    }

    @Test
    @DisplayName("切片30b：非 MERCHANT 申请批准失败 → 无 ONBOARDING_APPROVE 审计")
    void rejectOperatorOnboardingWritesNoAudit() {
        Organization pending =
                Organization.create(
                        "ORG-OP",
                        "待批运营商",
                        null,
                        List.of(),
                        List.of("SZ"),
                        com.evolutionary.operator.domain.Organization.Status.ACTIVE);
        orgs.save(pending);
        applications.save(
                OnboardingApplication.submit(
                        "APP-OP-X", pending.id(), OrgCapability.OPERATOR, FIXED.minusSeconds(60)));

        OperatorOutcome<MerchantProfile> outcome =
                approve.execute("APP-OP-X", "不应开店", "U-PLATFORM", "PLATFORM");
        assertInstanceOf(OperatorOutcome.Err.class, outcome);
        assertEquals(
                OperatorErrorCode.CAPABILITY_DENIED,
                ((OperatorOutcome.Err<MerchantProfile>) outcome).code());
        assertTrue(audits.findByResourceId("APP-OP-X").isEmpty());
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

        @Override
        public List<Organization> findAll() {
            return List.copyOf(store.values());
        }
    }

    private static final class InMemoryOnboardingRepo implements OnboardingApplicationRepository {
        private final Map<String, OnboardingApplication> store = new HashMap<>();

        @Override
        public void save(OnboardingApplication application) {
            store.put(application.id(), application);
        }

        @Override
        public Optional<OnboardingApplication> findById(String id) {
            return Optional.ofNullable(store.get(id));
        }
    }

    private static final class InMemoryMerchantRepo implements MerchantProfileRepository {
        private final Map<String, MerchantProfile> store = new HashMap<>();

        @Override
        public void save(MerchantProfile profile) {
            store.put(profile.orgId(), profile);
        }

        @Override
        public Optional<MerchantProfile> findByOrgId(String orgId) {
            return Optional.ofNullable(store.get(orgId));
        }
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
}
