package com.evolutionary.operator.interfaces;

import com.evolutionary.operator.application.ActivatePackageOverride;
import com.evolutionary.operator.application.ApproveOperatorDownline;
import com.evolutionary.operator.application.CreateNextVersionDraft;
import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.application.PackageTemplateRepository;
import com.evolutionary.operator.application.PublishPackageTemplate;
import com.evolutionary.operator.application.ResolveEffectiveProduct;
import com.evolutionary.operator.application.RevokePackageOverride;
import com.evolutionary.operator.domain.EffectiveProduct;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.OrgCapability;
import com.evolutionary.operator.domain.Organization;
import com.evolutionary.operator.domain.PackageOverride;
import com.evolutionary.operator.domain.PackageTemplate;
import com.evolutionary.operator.domain.TemplateBaseProduct;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 运营 HTTP（套餐 · 覆盖 · 批下线 OPERATOR；商家入驻在 /admin）。 */
@RestController
@RequestMapping("/operator")
public class OperatorController {

    private final OrganizationRepository organizations;
    private final PackageTemplateRepository templates;
    private final PublishPackageTemplate publishPackageTemplate;
    private final CreateNextVersionDraft createNextVersionDraft;
    private final ActivatePackageOverride activatePackageOverride;
    private final RevokePackageOverride revokePackageOverride;
    private final ResolveEffectiveProduct resolveEffectiveProduct;
    private final ApproveOperatorDownline approveOperatorDownline;

    public OperatorController(
            OrganizationRepository organizations,
            PackageTemplateRepository templates,
            PublishPackageTemplate publishPackageTemplate,
            CreateNextVersionDraft createNextVersionDraft,
            ActivatePackageOverride activatePackageOverride,
            RevokePackageOverride revokePackageOverride,
            ResolveEffectiveProduct resolveEffectiveProduct,
            ApproveOperatorDownline approveOperatorDownline) {
        this.organizations = organizations;
        this.templates = templates;
        this.publishPackageTemplate = publishPackageTemplate;
        this.createNextVersionDraft = createNextVersionDraft;
        this.activatePackageOverride = activatePackageOverride;
        this.revokePackageOverride = revokePackageOverride;
        this.resolveEffectiveProduct = resolveEffectiveProduct;
        this.approveOperatorDownline = approveOperatorDownline;
    }

    /** 只读：供操作方面板对齐 canActAsManager（仅 ACTIVE）。 */
    @GetMapping("/orgs/{orgId}")
    public ResponseEntity<OrganizationView> organization(@PathVariable String orgId) {
        return organizations
                .findById(orgId.trim())
                .map(OperatorController::toOrganizationView)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 只读：供发布/派生面板对齐 publishAllowed / nextVersionAllowed。 */
    @GetMapping("/templates/{templateId}")
    public ResponseEntity<TemplateView> template(@PathVariable String templateId) {
        return templates
                .findById(templateId.trim())
                .map(OperatorController::toTemplateView)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 切片29a：运营商批准 OPERATOR 下线入驻（非 MERCHANT）。 */
    @PostMapping("/onboarding/{applicationId}/approve-downline")
    public ResponseEntity<?> approveDownline(
            @PathVariable String applicationId, @RequestBody DownlineApproveRequest body) {
        if (body == null
                || body.actorUserId() == null
                || body.actorUserId().isBlank()
                || body.actorOrgId() == null
                || body.actorOrgId().isBlank()) {
            throw new IllegalArgumentException("actorUserId and actorOrgId required");
        }
        OperatorOutcome<Organization> outcome =
                approveOperatorDownline.execute(
                        body.actorUserId().trim(),
                        body.actorOrgId().trim(),
                        applicationId.trim());
        if (outcome instanceof OperatorOutcome.Ok<Organization> ok) {
            return ResponseEntity.ok(toDownlineView(ok.value()));
        }
        OperatorOutcome.Err<Organization> err = (OperatorOutcome.Err<Organization>) outcome;
        OperatorApiErrorTranslator.Translated translated =
                OperatorApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 切片23a / AC-24：发布草稿套餐模板 → published。 */
    @PostMapping("/templates/{templateId}/publish")
    public ResponseEntity<?> publish(
            @PathVariable String templateId, @RequestBody PublishRequest body) {
        if (body == null
                || body.actorUserId() == null
                || body.actorUserId().isBlank()
                || body.actorOrgId() == null
                || body.actorOrgId().isBlank()) {
            throw new IllegalArgumentException("actorUserId and actorOrgId required");
        }
        OperatorOutcome<PackageTemplate> outcome =
                publishPackageTemplate.execute(
                        body.actorUserId().trim(),
                        body.actorOrgId().trim(),
                        templateId.trim());
        if (outcome instanceof OperatorOutcome.Ok<PackageTemplate> ok) {
            return ResponseEntity.ok(toPublishView(ok.value()));
        }
        OperatorOutcome.Err<PackageTemplate> err =
                (OperatorOutcome.Err<PackageTemplate>) outcome;
        OperatorApiErrorTranslator.Translated translated =
                OperatorApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /**
     * 从已发布模板派生下一版本草稿（AC-25 合法变更路径）。
     *
     * <p>body：actor + newTemplateId + baseProduct（displayName / priceCents / durationDays）。
     */
    @PostMapping("/templates/{templateId}/next-version")
    public ResponseEntity<?> nextVersion(
            @PathVariable String templateId, @RequestBody NextVersionRequest body) {
        if (body == null
                || body.actorUserId() == null
                || body.actorUserId().isBlank()
                || body.actorOrgId() == null
                || body.actorOrgId().isBlank()
                || body.newTemplateId() == null
                || body.newTemplateId().isBlank()
                || body.displayName() == null
                || body.displayName().isBlank()) {
            throw new IllegalArgumentException(
                    "actorUserId, actorOrgId, newTemplateId and displayName required");
        }
        TemplateBaseProduct nextBase =
                TemplateBaseProduct.of(
                        body.displayName().trim(), body.priceCents(), body.durationDays());
        OperatorOutcome<PackageTemplate> outcome =
                createNextVersionDraft.execute(
                        body.actorUserId().trim(),
                        body.actorOrgId().trim(),
                        templateId.trim(),
                        body.newTemplateId().trim(),
                        nextBase);
        if (outcome instanceof OperatorOutcome.Ok<PackageTemplate> ok) {
            return ResponseEntity.ok(toTemplateView(ok.value()));
        }
        OperatorOutcome.Err<PackageTemplate> err =
                (OperatorOutcome.Err<PackageTemplate>) outcome;
        OperatorApiErrorTranslator.Translated translated =
                OperatorApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 切片24a / AC-26·27：L2 激活套餐覆盖（patches ⊆ allowedOverrideFields）。 */
    @PostMapping("/templates/{templateId}/overrides")
    public ResponseEntity<?> activateOverride(
            @PathVariable String templateId, @RequestBody ActivateOverrideRequest body) {
        if (body == null
                || body.actorUserId() == null
                || body.actorUserId().isBlank()
                || body.actorOrgId() == null
                || body.actorOrgId().isBlank()
                || body.overrideId() == null
                || body.overrideId().isBlank()
                || body.patches() == null
                || body.patches().isEmpty()) {
            throw new IllegalArgumentException(
                    "actorUserId, actorOrgId, overrideId and patches required");
        }
        OperatorOutcome<PackageOverride> outcome =
                activatePackageOverride.execute(
                        body.actorUserId().trim(),
                        body.actorOrgId().trim(),
                        templateId.trim(),
                        body.overrideId().trim(),
                        body.patches());
        if (outcome instanceof OperatorOutcome.Ok<PackageOverride> ok) {
            return ResponseEntity.ok(toOverrideView(ok.value()));
        }
        OperatorOutcome.Err<PackageOverride> err =
                (OperatorOutcome.Err<PackageOverride>) outcome;
        OperatorApiErrorTranslator.Translated translated =
                OperatorApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 切片25a / AC-31：撤销已激活覆盖 → REVOKED；目录回落模板原价。 */
    @PostMapping("/overrides/{overrideId}/revoke")
    public ResponseEntity<?> revokeOverride(
            @PathVariable String overrideId, @RequestBody RevokeOverrideRequest body) {
        if (body == null
                || body.actorUserId() == null
                || body.actorUserId().isBlank()
                || body.actorOrgId() == null
                || body.actorOrgId().isBlank()) {
            throw new IllegalArgumentException("actorUserId and actorOrgId required");
        }
        OperatorOutcome<PackageOverride> outcome =
                revokePackageOverride.execute(
                        body.actorUserId().trim(),
                        body.actorOrgId().trim(),
                        overrideId.trim());
        if (outcome instanceof OperatorOutcome.Ok<PackageOverride> ok) {
            return ResponseEntity.ok(toOverrideView(ok.value()));
        }
        OperatorOutcome.Err<PackageOverride> err =
                (OperatorOutcome.Err<PackageOverride>) outcome;
        OperatorApiErrorTranslator.Translated translated =
                OperatorApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 切片24a / AC-26：组织视角有效商品读模型。 */
    @GetMapping("/orgs/{orgId}/templates/{templateId}/effective-product")
    public ResponseEntity<EffectiveProductView> effectiveProduct(
            @PathVariable String orgId, @PathVariable String templateId) {
        EffectiveProduct product =
                resolveEffectiveProduct.execute(orgId.trim(), templateId.trim());
        return ResponseEntity.ok(toEffectiveView(product));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperatorApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(new OperatorApiErrorTranslator.ApiError(msg, null));
    }

    private static PublishView toPublishView(PackageTemplate template) {
        return new PublishView(
                template.id(),
                template.ownerOrgId(),
                template.status().name(),
                template.version(),
                template.publishedAt());
    }

    private static TemplateView toTemplateView(PackageTemplate template) {
        return new TemplateView(
                template.id(),
                template.ownerOrgId(),
                template.status().name(),
                template.version(),
                template.inheritedFrom());
    }

    private static OverrideView toOverrideView(PackageOverride override) {
        return new OverrideView(
                override.id(),
                override.orgId(),
                override.templateId(),
                override.status().name(),
                override.patches().priceCents().orElse(null),
                override.patches().displayName().orElse(null));
    }

    private static EffectiveProductView toEffectiveView(EffectiveProduct product) {
        return new EffectiveProductView(
                product.templateId(),
                product.templateVersion(),
                product.overrideId(),
                product.displayName(),
                product.priceCents(),
                product.durationDays());
    }

    private static DownlineView toDownlineView(Organization org) {
        return new DownlineView(
                org.id(),
                org.name(),
                org.parentId(),
                org.hasCapability(OrgCapability.OPERATOR),
                org.status().name());
    }

    private static OrganizationView toOrganizationView(Organization org) {
        return new OrganizationView(
                org.id(),
                org.name(),
                org.parentId(),
                org.status().name(),
                org.hasCapability(OrgCapability.OPERATOR));
    }

    public record DownlineApproveRequest(String actorUserId, String actorOrgId) {}

    public record DownlineView(
            String orgId,
            String name,
            String parentOrgId,
            boolean operatorCapability,
            String status) {}

    public record OrganizationView(
            String id,
            String name,
            String parentId,
            String status,
            boolean operatorCapability) {}

    public record PublishRequest(String actorUserId, String actorOrgId) {}

    public record PublishView(
            String id, String ownerOrgId, String status, int version, Instant publishedAt) {}

    public record NextVersionRequest(
            String actorUserId,
            String actorOrgId,
            String newTemplateId,
            String displayName,
            long priceCents,
            int durationDays) {}

    public record TemplateView(
            String id, String ownerOrgId, String status, int version, String inheritedFrom) {}

    public record ActivateOverrideRequest(
            String actorUserId, String actorOrgId, String overrideId, Map<String, Object> patches) {}

    public record RevokeOverrideRequest(String actorUserId, String actorOrgId) {}

    public record OverrideView(
            String id,
            String orgId,
            String templateId,
            String status,
            Long priceCents,
            String displayName) {}

    public record EffectiveProductView(
            String templateId,
            int templateVersion,
            String overrideId,
            String displayName,
            long priceCents,
            int durationDays) {}
}
