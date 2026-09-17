package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.OperatorErrorCode;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import java.util.Objects;

/**
 * 试图原地修改已发布模板的 baseProduct（AC-25 拒绝路径）。
 *
 * <p>合法变更走 {@link PackageTemplate#createNextVersionDraft}。
 */
public final class MutatePackageTemplateBaseProduct {

    private final PackageTemplateRepository templates;

    public MutatePackageTemplateBaseProduct(PackageTemplateRepository templates) {
        this.templates = Objects.requireNonNull(templates, "templates");
    }

    public OperatorOutcome<PackageTemplate> execute(
            String actorOrgId, String templateId, TemplateBaseProduct nextBase) {
        Objects.requireNonNull(actorOrgId, "actorOrgId");
        Objects.requireNonNull(templateId, "templateId");
        Objects.requireNonNull(nextBase, "nextBase");

        PackageTemplate template = templates.get(templateId);
        if (!actorOrgId.equals(template.ownerOrgId())) {
            return OperatorOutcome.err(OperatorErrorCode.ORG_NOT_OWNER, "仅归属组织可改模板");
        }

        OperatorOutcome<PackageTemplate> outcome = template.replaceBaseProduct(nextBase);
        if (outcome instanceof OperatorOutcome.Ok<PackageTemplate> ok) {
            templates.save(ok.value());
        }
        return outcome;
    }
}
