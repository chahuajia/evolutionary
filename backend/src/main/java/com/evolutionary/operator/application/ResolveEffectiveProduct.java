package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.EffectiveProduct;
import com.evolutionary.operator.domain.PackageOverride;
import com.evolutionary.operator.domain.PackageTemplate;
import java.util.Objects;

/**
 * 解析某组织视角下的有效商品（AC-26 / INV-13）。
 *
 * <p>无 active override 时返回模板原价。
 */
public final class ResolveEffectiveProduct {

    private final PackageTemplateRepository templates;
    private final PackageOverrideRepository overrides;

    public ResolveEffectiveProduct(
            PackageTemplateRepository templates, PackageOverrideRepository overrides) {
        this.templates = Objects.requireNonNull(templates, "templates");
        this.overrides = Objects.requireNonNull(overrides, "overrides");
    }

    public EffectiveProduct execute(String viewerOrgId, String templateId) {
        Objects.requireNonNull(viewerOrgId, "viewerOrgId");
        Objects.requireNonNull(templateId, "templateId");

        PackageTemplate template = templates.get(templateId);
        PackageOverride active =
                overrides.findActiveByOrgAndTemplate(viewerOrgId, templateId).orElse(null);
        return EffectiveProduct.resolve(template, active);
    }
}
