package com.evolutionary.operator.domain;

import java.util.Objects;

/**
 * 有效商品只读视图（非持久化聚合根）。
 *
 * <p>{@code resolve(template, override?)} = 模板 base + active override patches；无覆盖时继承原价（INV-13）。
 */
public final class EffectiveProduct {

    private final String templateId;
    private final int templateVersion;
    private final String overrideId;
    private final TemplateBaseProduct product;

    private EffectiveProduct(
            String templateId, int templateVersion, String overrideId, TemplateBaseProduct product) {
        this.templateId = templateId;
        this.templateVersion = templateVersion;
        this.overrideId = overrideId;
        this.product = product;
    }

    /**
     * @param override 可为 null；非 {@link PackageOverride#isActive()} 时等同无覆盖
     */
    public static EffectiveProduct resolve(PackageTemplate template, PackageOverride override) {
        Objects.requireNonNull(template, "template");
        if (override == null || !override.isActive()) {
            return new EffectiveProduct(
                    template.id(), template.version(), null, template.baseProduct());
        }
        if (!template.id().equals(override.templateId())) {
            throw new IllegalArgumentException("override 与 template 不匹配");
        }
        TemplateBaseProduct merged = override.patches().applyTo(template.baseProduct());
        return new EffectiveProduct(
                template.id(), template.version(), override.id(), merged);
    }

    public String templateId() {
        return templateId;
    }

    public int templateVersion() {
        return templateVersion;
    }

    /** 无覆盖时为 null。 */
    public String overrideId() {
        return overrideId;
    }

    public TemplateBaseProduct product() {
        return product;
    }

    public long priceCents() {
        return product.priceCents();
    }

    public String displayName() {
        return product.displayName();
    }

    public int durationDays() {
        return product.durationDays();
    }
}
