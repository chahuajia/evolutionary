package com.evolutionary.operator.interfaces;

import com.evolutionary.mall.domain.MerchantProfile;
import com.evolutionary.operator.application.ApproveMerchantOnboarding;
import com.evolutionary.operator.domain.OperatorOutcome;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 运营 HTTP（商家入驻批准 AC-40）。 */
@RestController
@RequestMapping("/operator")
public class OperatorController {

    private final ApproveMerchantOnboarding approveMerchantOnboarding;

    public OperatorController(ApproveMerchantOnboarding approveMerchantOnboarding) {
        this.approveMerchantOnboarding = approveMerchantOnboarding;
    }

    /** 切片19a / AC-40：批准 MERCHANT 入驻 → MerchantProfile.active。 */
    @PostMapping("/onboarding/{applicationId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable String applicationId, @RequestBody ApproveRequest body) {
        if (body == null || body.shopName() == null || body.shopName().isBlank()) {
            throw new IllegalArgumentException("shopName required");
        }
        OperatorOutcome<MerchantProfile> outcome =
                approveMerchantOnboarding.execute(applicationId.trim(), body.shopName().trim());
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

    public record ApproveRequest(String shopName) {}

    public record ApproveView(String merchantOrgId, String shopName, String status) {}
}
