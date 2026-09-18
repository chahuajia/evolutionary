package com.evolutionary.operator.interfaces;

import com.evolutionary.operator.application.ActivatePackageOverride;
import com.evolutionary.operator.application.PublishPackageTemplate;
import com.evolutionary.operator.application.ResolveEffectiveProduct;
import com.evolutionary.operator.application.RevokePackageOverride;
import com.evolutionary.operator.domain.EffectiveProduct;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.domain.PackageOverride;
import com.evolutionary.operator.domain.PackageTemplate;
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

/** 运营 HTTP（套餐发布 AC-24 · 覆盖/有效价/撤销 AC-26/27/31；商家入驻已迁 /admin）。 */
@RestController
@RequestMapping("/operator")
public class OperatorController {

    private final PublishPackageTemplate publishPackageTemplate;
    private final ActivatePackageOverride activatePackageOverride;
    private final RevokePackageOverride revokePackageOverride;
    private final ResolveEffectiveProduct resolveEffectiveProduct;

    public OperatorController(
            PublishPackageTemplate publishPackageTemplate,
            ActivatePackageOverride activatePackageOverride,
            RevokePackageOverride revokePackageOverride,
            ResolveEffectiveProduct resolveEffectiveProduct) {
        this.publishPackageTemplate = publishPackageTemplate;
        this.activatePackageOverride = activatePackageOverride;
        this.revokePackageOverride = revokePackageOverride;
        this.resolveEffectiveProduct = resolveEffectiveProduct;
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
                template.status().name(),
                template.version(),
                template.publishedAt());
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

    public record PublishRequest(String actorUserId, String actorOrgId) {}

    public record PublishView(String id, String status, int version, Instant publishedAt) {}

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
