package com.evolutionary.mall.interfaces;

import com.evolutionary.mall.application.CheckoutMallOrderWithCoupons;
import com.evolutionary.mall.application.ClaimCouponFromCampaign;
import com.evolutionary.mall.application.PurchaseMallOrder;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.UserCoupon;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 商城 HTTP（活动领券 AC-45 · 下单支付 AC-41 · 带券结账 AC-42..44）。 */
@RestController
@RequestMapping("/mall")
public class MallController {

    private final ClaimCouponFromCampaign claimCouponFromCampaign;
    private final PurchaseMallOrder purchaseMallOrder;
    private final CheckoutMallOrderWithCoupons checkoutMallOrderWithCoupons;

    public MallController(
            ClaimCouponFromCampaign claimCouponFromCampaign,
            PurchaseMallOrder purchaseMallOrder,
            CheckoutMallOrderWithCoupons checkoutMallOrderWithCoupons) {
        this.claimCouponFromCampaign = claimCouponFromCampaign;
        this.purchaseMallOrder = purchaseMallOrder;
        this.checkoutMallOrderWithCoupons = checkoutMallOrderWithCoupons;
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
            return ResponseEntity.ok(toCouponView(ok.value()));
        }
        MallOutcome.Err<UserCoupon> err = (MallOutcome.Err<UserCoupon>) outcome;
        MallApiErrorTranslator.Translated translated = MallApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 切片14a / AC-41：余额购 SKU（INV-16 不产生权益）。 */
    @PostMapping("/orders")
    public ResponseEntity<?> purchase(@RequestBody PurchaseRequest body) {
        if (body == null) {
            throw new IllegalArgumentException("body required");
        }
        if (body.userId() == null || body.userId().isBlank()) {
            throw new IllegalArgumentException("userId required");
        }
        if (body.merchantOrgId() == null || body.merchantOrgId().isBlank()) {
            throw new IllegalArgumentException("merchantOrgId required");
        }
        if (body.skuId() == null || body.skuId().isBlank()) {
            throw new IllegalArgumentException("skuId required");
        }
        int qty = body.qty() == null ? 1 : body.qty();
        MallOutcome<MallOrder> outcome =
                purchaseMallOrder.execute(
                        body.userId().trim(),
                        body.merchantOrgId().trim(),
                        body.skuId().trim(),
                        qty);
        if (outcome instanceof MallOutcome.Ok<MallOrder> ok) {
            return ResponseEntity.ok(toOrderView(ok.value()));
        }
        MallOutcome.Err<MallOrder> err = (MallOutcome.Err<MallOrder>) outcome;
        MallApiErrorTranslator.Translated translated = MallApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    /** 切片16a / AC-42..44：先领券再带券下单（userCouponIds 可空 ≡ 无券路径）。 */
    @PostMapping("/orders/checkout-with-coupons")
    public ResponseEntity<?> checkoutWithCoupons(@RequestBody CheckoutWithCouponsRequest body) {
        if (body == null) {
            throw new IllegalArgumentException("body required");
        }
        if (body.userId() == null || body.userId().isBlank()) {
            throw new IllegalArgumentException("userId required");
        }
        if (body.merchantOrgId() == null || body.merchantOrgId().isBlank()) {
            throw new IllegalArgumentException("merchantOrgId required");
        }
        if (body.skuId() == null || body.skuId().isBlank()) {
            throw new IllegalArgumentException("skuId required");
        }
        int qty = body.qty() == null ? 1 : body.qty();
        List<String> couponIds = body.userCouponIds() == null ? List.of() : body.userCouponIds();
        MallOutcome<MallOrder> outcome =
                checkoutMallOrderWithCoupons.execute(
                        body.userId().trim(),
                        body.merchantOrgId().trim(),
                        body.skuId().trim(),
                        qty,
                        couponIds);
        if (outcome instanceof MallOutcome.Ok<MallOrder> ok) {
            return ResponseEntity.ok(toOrderView(ok.value()));
        }
        MallOutcome.Err<MallOrder> err = (MallOutcome.Err<MallOrder>) outcome;
        MallApiErrorTranslator.Translated translated = MallApiErrorTranslator.translate(err);
        return ResponseEntity.status(translated.status()).body(translated.body());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<MallApiErrorTranslator.ApiError> handleBadRequest(
            IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "bad request" : ex.getMessage();
        return ResponseEntity.badRequest().body(new MallApiErrorTranslator.ApiError(msg, null));
    }

    private static UserCouponView toCouponView(UserCoupon c) {
        return new UserCouponView(
                c.id(), c.userId(), c.templateId(), c.status().name().toLowerCase());
    }

    private static MallOrderView toOrderView(MallOrder o) {
        String skuId = o.lines().isEmpty() ? null : o.lines().get(0).skuId();
        int qty = o.lines().isEmpty() ? 0 : o.lines().get(0).qty();
        long discountCents = o.discountTotal() == null ? 0L : o.discountTotal().cents();
        return new MallOrderView(
                o.id(),
                o.userId(),
                o.merchantOrgId(),
                o.status().name(),
                o.paidAmount().cents(),
                discountCents,
                skuId,
                qty);
    }

    public record ClaimRequest(String userId, String templateId) {}

    public record PurchaseRequest(String userId, String merchantOrgId, String skuId, Integer qty) {}

    public record CheckoutWithCouponsRequest(
            String userId,
            String merchantOrgId,
            String skuId,
            Integer qty,
            List<String> userCouponIds) {}

    public record UserCouponView(String id, String userId, String templateId, String status) {}

    public record MallOrderView(
            String orderId,
            String userId,
            String merchantOrgId,
            String status,
            long paidAmountCents,
            long discountCents,
            String skuId,
            int qty) {}
}
