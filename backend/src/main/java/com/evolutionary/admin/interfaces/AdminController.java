package com.evolutionary.admin.interfaces;

import com.evolutionary.mall.domain.MerchantProfile;
import com.evolutionary.operator.application.ApproveMerchantOnboarding;
import com.evolutionary.operator.domain.OperatorOutcome;
import com.evolutionary.operator.interfaces.OperatorApiErrorTranslator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 平台总后台 HTTP（切片26a · AC-40：商家入驻批准迁出运营商；切片30b：审计 actor）。
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    static final String DEFAULT_ACTOR_USER_ID = "U-PLATFORM";
    static final String DEFAULT_ACTOR_ORG_ID = "PLATFORM";

    private final ApproveMerchantOnboarding approveMerchantOnboarding;

    public AdminController(ApproveMerchantOnboarding approveMerchantOnboarding) {
        this.approveMerchantOnboarding = approveMerchantOnboarding;
    }

    /** 切片26a / AC-40：平台批准 MERCHANT 入驻 → MerchantProfile.active。 */
    @PostMapping("/onboarding/{applicationId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable String applicationId, @RequestBody ApproveRequest body) {
        if (body == null || body.shopName() == null || body.shopName().isBlank()) {
            throw new IllegalArgumentException("shopName required");
        }
        String actorUserId =
                body.actorUserId() == null || body.actorUserId().isBlank()
                        ? DEFAULT_ACTOR_USER_ID
                        : body.actorUserId().trim();
        String actorOrgId =
                body.actorOrgId() == null || body.actorOrgId().isBlank()
                        ? DEFAULT_ACTOR_ORG_ID
                        : body.actorOrgId().trim();
        OperatorOutcome<MerchantProfile> outcome =
                approveMerchantOnboarding.execute(
                        applicationId.trim(),
                        body.shopName().trim(),
                        actorUserId,
                        actorOrgId);
        if (outcome instanceof OperatorOutcome.Ok<MerchantProfile> ok) {
            return ResponseEntity.ok(toView(ok.value()));
        }
        OperatorOutcome.Err<MerchantProfile> err =
                (OperatorOutcome.Err<MerchantProfile>) outcome;
        OperatorApiErrorTranslator.Translated translated =
                OperatorApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OperatorApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest()
                .body(new OperatorApiErrorTranslator.ApiError(msg, null));
    }

    private static ApproveView toView(MerchantProfile profile) {
        return new ApproveView(
                profile.orgId(), profile.shopName(), profile.status().name());
    }

    /** actor 可选；缺省 {@code U-PLATFORM} / {@code PLATFORM}。 */
    public record ApproveRequest(String shopName, String actorUserId, String actorOrgId) {}

    public record ApproveView(String merchantOrgId, String shopName, String status) {}
}
