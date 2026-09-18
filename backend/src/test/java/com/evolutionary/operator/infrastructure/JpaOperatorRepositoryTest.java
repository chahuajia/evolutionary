package com.evolutionary.operator.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.evolutionary.operator.application.OnboardingApplicationRepository;
import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.application.PackageOverrideRepository;
import com.evolutionary.operator.application.PackageTemplateRepository;
import com.evolutionary.operator.domain.OnboardingApplication;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.OverridePatches;
import com.evolutionary.operator.domain.OverridableField;
import com.evolutionary.operator.domain.PackageOverride;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * operator 域四仓储 JPA 落库。
 *
 * <p>**刻意不写 {@code @BeforeEach deleteAllInBatch}** —— 与本仓其他 JPA 测试不同。
 * 原因：{@code OperatorConfig} 的组织种子在 **bean 构造时**写入，而
 * {@code OrgAuthorization} 也是那时建的索引。清表会让索引与实际库**不一致**，
 * 且清掉的行再也不会被重新播种（种子的执行时机只有一次）。
 * 所以本测试用独立的测试 ID，不断言"表里只有我这一条"。
 */
@SpringBootTest
class JpaOperatorRepositoryTest {

    private static final Instant AT = Instant.parse("2026-09-19T00:00:00Z");

    @Autowired
    private OrganizationRepository organizations;

    @Autowired
    private OnboardingApplicationRepository applications;

    @Autowired
    private PackageTemplateRepository templates;

    @Autowired
    private PackageOverrideRepository overrides;

    @Autowired
    private OrganizationJpaRepository orgJpa;

    /** 取出 Ok 值 —— 测试里失败即视为断言失败（pattern matching，不用裸 cast）。 */
    private static <T> T unwrap(OperatorOutcome<T> outcome) {
        if (outcome instanceof OperatorOutcome.Ok<T> ok) {
            return ok.value();
        }
        throw new AssertionError("预期 Ok，实际 " + outcome);
    }

    @Test
    @DisplayName("组织：种子已落库，且带 parentId 的祖先链可回放")
    void organizationSeedPersisted() {
        // 种子在 bean 构造时写入 —— 它必须真的在库里（OrgAuthorization 靠它建索引）
        assertTrue(orgJpa.findById("ORG-L1").isPresent(), "种子 ORG-L1 应已落库");
        assertTrue(orgJpa.findById("ORG-L2").isPresent(), "种子 ORG-L2 应已落库");

        Organization l2 = organizations.get("ORG-L2");
        assertEquals("ORG-L1", l2.parentId());
        assertEquals(List.of("SZ"), l2.regionScope());
        assertTrue(l2.hasCapability(OrgCapability.OPERATOR));

        // 集合类字段要能回放完整（capabilities / regionScope 都是 @ElementCollection）
        Organization l1 = organizations.get("ORG-L1");
        assertEquals(List.of("GD", "SZ"), l1.regionScope());
        assertTrue(l1.isActive());

        // 商家种子无法发模板（无 OPERATOR）—— 能力集合没在持久化里丢失
        assertFalse(organizations.get("ORG-NEW").canPublishPackageTemplate());
    }

    @Test
    @DisplayName("组织：新建组织落库并可按 id 读回")
    void organizationRoundTrip() {
        organizations.save(
                Organization.create(
                        "ORG-T-JPA",
                        "测试组织",
                        "ORG-L1",
                        List.of(OrgCapability.MERCHANT),
                        List.of("SZ", "GZ"),
                        Organization.Status.ACTIVE));

        Organization found = organizations.get("ORG-T-JPA");
        assertEquals("测试组织", found.name());
        assertEquals("ORG-L1", found.parentId());
        assertEquals(List.of(OrgCapability.MERCHANT), found.capabilities());
        assertEquals(List.of("SZ", "GZ"), found.regionScope());
        assertTrue(orgJpa.findById("ORG-T-JPA").isPresent());
    }

    @Test
    @DisplayName("入驻申请：SUBMITTED 的 reviewedAt 为 null，APPROVED 后回放保留")
    void onboardingApplicationRoundTrip() {
        applications.save(
                OnboardingApplication.submit("APP-T-1", "ORG-NEW", OrgCapability.MERCHANT, AT));

        OnboardingApplication submitted = applications.get("APP-T-1");
        assertEquals(OnboardingApplication.Status.SUBMITTED, submitted.status());
        assertEquals(null, submitted.reviewedAt(), "未审批时 reviewedAt 应为 null");

        Instant reviewed = Instant.parse("2026-09-19T01:00:00Z");
        applications.save(submitted.approve(reviewed));

        OnboardingApplication approved = applications.get("APP-T-1");
        assertEquals(OnboardingApplication.Status.APPROVED, approved.status());
        assertEquals(reviewed, approved.reviewedAt());
        assertEquals(AT, approved.submittedAt(), "submittedAt 不该被审批改写");
    }

    @Test
    @DisplayName("套餐模板：草稿 publishedAt 为 null；发布后状态与时间都回放正确")
    void packageTemplateRoundTrip() {
        TemplateBaseProduct base = TemplateBaseProduct.of("30天卡", 3000, 30);
        templates.save(
                PackageTemplate.createDraft(
                        "T-T-1",
                        "ORG-L1",
                        base,
                        List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME)));

        PackageTemplate draft = templates.get("T-T-1");
        assertEquals(PackageTemplate.Status.DRAFT, draft.status());
        assertEquals(null, draft.publishedAt(), "草稿不该有 publishedAt");
        // 值对象展平后要能完整还原
        assertEquals("30天卡", draft.baseProduct().displayName());
        assertEquals(3000, draft.baseProduct().priceCents());
        assertEquals(30, draft.baseProduct().durationDays());
        assertEquals(
                List.of(OverridableField.PRICE, OverridableField.DISPLAY_NAME),
                draft.allowedOverrideFields());

        Instant publishedAt = Instant.parse("2026-09-19T02:00:00Z");
        templates.save(unwrap(draft.publish(publishedAt)));

        PackageTemplate published = templates.get("T-T-1");
        assertEquals(PackageTemplate.Status.PUBLISHED, published.status());
        assertEquals(publishedAt, published.publishedAt());
        assertTrue(published.isPublished());
    }

    @Test
    @DisplayName("套餐覆盖：patches 的 null（不覆盖）与 0（覆盖为 0）不能混淆")
    void packageOverrideNullVsZeroPatch() {
        // 只覆盖价格，不动名称 —— displayName 必须是 null，不是空串
        overrides.save(
                PackageOverride.rehydrate(
                        "OV-T-PRICE",
                        "ORG-L2",
                        "T-T-1",
                        1,
                        List.of(OverridableField.PRICE),
                        OverridePatches.priceOnly(999),
                        AT,
                        null,
                        PackageOverride.Status.DRAFT));

        PackageOverride priceOnly = overrides.get("OV-T-PRICE");
        assertEquals(999L, priceOnly.patches().priceCents().orElseThrow());
        assertTrue(priceOnly.patches().displayName().isEmpty(), "未覆盖的字段应是空 Optional");

        // 价格覆盖为 0 —— 必须与"不覆盖"区分开
        overrides.save(
                PackageOverride.rehydrate(
                        "OV-T-ZERO",
                        "ORG-L2",
                        "T-T-1",
                        1,
                        List.of(OverridableField.PRICE),
                        OverridePatches.priceOnly(0),
                        AT,
                        null,
                        PackageOverride.Status.DRAFT));

        PackageOverride zero = overrides.get("OV-T-ZERO");
        assertEquals(0L, zero.patches().priceCents().orElseThrow(), "0 是合法的覆盖值");
        assertFalse(zero.patches().isEmpty());
    }

    @Test
    @DisplayName("套餐覆盖：只有 ACTIVE 能被按 (org, template) 查到")
    void activeOverrideLookup() {
        overrides.save(
                PackageOverride.rehydrate(
                        "OV-T-DRAFT",
                        "ORG-T-ACTIVE",
                        "T-T-ACTIVE",
                        1,
                        List.of(OverridableField.PRICE),
                        OverridePatches.priceOnly(100),
                        AT,
                        null,
                        PackageOverride.Status.DRAFT));

        assertTrue(
                overrides.findActiveByOrgAndTemplate("ORG-T-ACTIVE", "T-T-ACTIVE").isEmpty(),
                "草稿不该被 findActive 命中");

        overrides.save(unwrap(overrides.get("OV-T-DRAFT").activate()));

        PackageOverride found =
                overrides
                        .findActiveByOrgAndTemplate("ORG-T-ACTIVE", "T-T-ACTIVE")
                        .orElseThrow();
        assertEquals("OV-T-DRAFT", found.id());
        assertEquals(PackageOverride.Status.ACTIVE, found.status());
        assertEquals(100L, found.patches().priceCents().orElseThrow());
    }
}
