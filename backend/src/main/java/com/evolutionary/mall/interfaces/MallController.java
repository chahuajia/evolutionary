package com.evolutionary.mall.interfaces;

import com.evolutionary.mall.application.ClaimCouponFromCampaign;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.UserCoupon;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商城 HTTP（活动领券 AC-45）。 */
@RestController
@RequestMapping("/mall")
public class MallController {

    private final ClaimCouponFromCampaign claimCouponFromCampaign;

    public MallController(ClaimCouponFromCampaign claimCouponFromCampaign) {
        this.claimCouponFromCampaign = claimCouponFromCampaign;
    }

    @PostMapping("/campaigns/{campaignId}/claims")
    public ResponseEntity<?> claim(
            @PathVariable String campaignId, @RequestBody ClaimRequest body) {
        if (body == null || body.userId() == null || body.userId().isBlank()) {
            throw new IllegalArgumentException("userId required");
        }
        if (body.templateId() == null || body.templateId().isBlank()) {
            throw new IllegalArgumentException("templateId required");
        }
        MallOutcome<UserCoupon> outcome =
                claimCouponFromCampaign.execute(
                        body.userId().trim(), campaignId.trim(), body.templateId().trim());
        if (outcome instanceof MallOutcome.Ok<UserCoupon> ok) {
            return ResponseEntity.ok(toView(ok.value()));
        }
        MallOutcome.Err<UserCoupon> err = (MallOutcome.Err<UserCoupon>) outcome;
        MallApiErrorTranslator.Translated translated = MallApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MallApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest().body(new MallApiErrorTranslator.ApiError(msg, null));
    }

    private static UserCouponView toView(UserCoupon c) {
        return new UserCouponView(
                c.id(), c.userId(), c.templateId(), c.status().name().toLowerCase());
    }

    public record ClaimRequest(String userId, String templateId) {}

    public record UserCouponView(String id, String userId, String templateId, String status) {}
}
