package com.evolutionary.mall.application;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.PaymentIntent;
import com.evolutionary.mall.domain.CouponRedemption;
import com.evolutionary.mall.domain.CouponRules;
import com.evolutionary.mall.domain.CouponTemplate;
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOrderLine;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.domain.UserCoupon;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 带优惠券的商城结账（AC-42..44 / AC-47）。
 *
 * <p>独立于 {@link PurchaseMallOrder}，避免破坏 AC-41 无券路径。仍遵守 INV-16。
 */
public final class CheckoutMallOrderWithCoupons {

    private final MallSkuRepository skus;
    private final MallOrderRepository orders;
    private final UserCouponRepository userCoupons;
    private final CouponTemplateRepository templates;
    private final CouponRedemptionRepository redemptions;
    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final Clock clock;

    public CheckoutMallOrderWithCoupons(
            MallSkuRepository skus,
            MallOrderRepository orders,
            UserCouponRepository userCoupons,
            CouponTemplateRepository templates,
            CouponRedemptionRepository redemptions,
            AccountRepository accounts,
            LedgerRepository ledger,
            Clock clock) {
        this.skus = Objects.requireNonNull(skus, "skus");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.userCoupons = Objects.requireNonNull(userCoupons, "userCoupons");
        this.templates = Objects.requireNonNull(templates, "templates");
        this.redemptions = Objects.requireNonNull(redemptions, "redemptions");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * @param userCouponIds 用户券 id 列表；可空（等价无券结账）
     */
    public MallOutcome<MallOrder> execute(
            String userId,
            String merchantOrgId,
            String skuId,
            int qty,
            List<String> userCouponIds) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(merchantOrgId, "merchantOrgId");
        Objects.requireNonNull(skuId, "skuId");
        List<String> couponIds = userCouponIds == null ? List.of() : List.copyOf(userCouponIds);

        if (qty <= 0) {
            return MallOutcome.err(MallErrorCode.INVALID_QTY, "qty 必须为正");
        }

        MallSku sku = skus.get(skuId);
        if (!sku.isOnSale()) {
            return MallOutcome.err(MallErrorCode.SKU_NOT_ON_SALE, "SKU 未上架");
        }
        if (!sku.merchantOrgId().equals(merchantOrgId)) {
            return MallOutcome.err(MallErrorCode.MERCHANT_MISMATCH, "SKU 不属于该商家");
        }

        MallOutcome<MallSku> deducted = sku.deductStock(qty);
        if (deducted instanceof MallOutcome.Err<MallSku> err) {
            return MallOutcome.err(err.code(), err.message());
        }
        MallSku afterStock = ((MallOutcome.Ok<MallSku>) deducted).value();

        MallOrderLine line = new MallOrderLine(sku.id(), qty, sku.price());
        Money orderTotal = line.lineTotal();

        List<UserCoupon> loadedCoupons = new ArrayList<>();
        List<CouponTemplate> loadedTemplates = new ArrayList<>();
        for (String cid : couponIds) {
            UserCoupon uc =
                    userCoupons
                            .findById(cid)
                            .orElse(null);
            if (uc == null || !uc.userId().equals(userId) || !uc.isAvailable()) {
                return MallOutcome.err(MallErrorCode.COUPON_NOT_AVAILABLE, "用户券不可用: " + cid);
            }
            CouponTemplate tpl =
                    templates
                            .findById(uc.templateId())
                            .orElse(null);
            if (tpl == null) {
                return MallOutcome.err(MallErrorCode.COUPON_NOT_AVAILABLE, "券模板不存在");
            }
            loadedCoupons.add(uc);
            loadedTemplates.add(tpl);
        }

        MallOutcome<List<Money>> discountOutcome =
                CouponRules.validateAndComputeMallDiscounts(
                        orderTotal, merchantOrgId, List.of(skuId), loadedTemplates);
        if (discountOutcome instanceof MallOutcome.Err<List<Money>> err) {
            return MallOutcome.err(err.code(), err.message());
        }
        List<Money> perCouponDiscounts = ((MallOutcome.Ok<List<Money>>) discountOutcome).value();
        Money discountTotal = CouponRules.sumDiscounts(perCouponDiscounts, orderTotal);
        Money paidAmount =
                new Money(orderTotal.cents() - discountTotal.cents(), orderTotal.currency());

        PaymentIntent intent = PaymentIntent.balanceOnly(paidAmount);
        Account userBalance = accounts.findUserBalance(userId, paidAmount.currency());
        if (!userBalance.canCoverCents(paidAmount.cents())) {
            return MallOutcome.err(MallErrorCode.INSUFFICIENT_BALANCE, "余额不足");
        }
        Account orgSettlement = accounts.findOrgSettlement(merchantOrgId, paidAmount.currency());

        var now = clock.instant();
        String orderId = newId("mord");
        MallOrder created =
                MallOrder.create(
                        orderId,
                        userId,
                        merchantOrgId,
                        List.of(line),
                        intent,
                        discountTotal.cents() == 0 ? null : discountTotal,
                        paidAmount,
                        now);

        // lock → pay → used + redemption
        List<UserCoupon> locked = new ArrayList<>();
        for (UserCoupon uc : loadedCoupons) {
            MallOutcome<UserCoupon> lock = uc.lock(orderId);
            if (lock instanceof MallOutcome.Err<UserCoupon> err) {
                return MallOutcome.err(err.code(), err.message());
            }
            locked.add(((MallOutcome.Ok<UserCoupon>) lock).value());
        }

        if (paidAmount.cents() > 0) {
            Account debited = userBalance.debit(paidAmount.cents());
            Account credited = orgSettlement.credit(paidAmount.cents());
            ledger.append(
                    LedgerEntry.orderPaymentBalance(
                            newId("led"),
                            userBalance.id(),
                            orgSettlement.id(),
                            paidAmount,
                            created.id(),
                            now));
            accounts.save(debited);
            accounts.save(credited);
            LedgerInvariant.assertBalanced(ledger.findAll());
        }

        MallOrder paid = created.pay(now);
        skus.save(afterStock);
        orders.save(paid);

        for (int i = 0; i < locked.size(); i++) {
            UserCoupon used = ((MallOutcome.Ok<UserCoupon>) locked.get(i).markUsed()).value();
            userCoupons.save(used);
            redemptions.append(
                    CouponRedemption.record(
                            newId("red"),
                            used.id(),
                            paid.id(),
                            perCouponDiscounts.get(i),
                            now));
        }

        return MallOutcome.ok(paid);
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
