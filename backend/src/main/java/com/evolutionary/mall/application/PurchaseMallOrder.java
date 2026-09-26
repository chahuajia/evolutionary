package com.evolutionary.mall.application;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.PaymentIntent;
import com.evolutionary.mall.domain.MallErrorCode;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOrderLine;
import com.evolutionary.mall.domain.MallOutcome;
import com.evolutionary.mall.domain.MallSku;
import com.evolutionary.mall.domain.MerchantProfile;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 商城下单并支付（AC-41 / INV-16）。
 *
 * <p><b>故意不注入</b> {@code EntitlementRepository} / {@code UsageEventRepository}——
 * MallOrder PAID 后禁止创建换电权益或用量事件。
 */
public final class PurchaseMallOrder {

    private final MallSkuRepository skus;
    private final MallOrderRepository orders;
    private final MerchantProfileRepository merchants;
    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final Clock clock;

    public PurchaseMallOrder(
            MallSkuRepository skus,
            MallOrderRepository orders,
            MerchantProfileRepository merchants,
            AccountRepository accounts,
            LedgerRepository ledger,
            Clock clock) {
        this.skus = Objects.requireNonNull(skus, "skus");
        this.orders = Objects.requireNonNull(orders, "orders");
        this.merchants = Objects.requireNonNull(merchants, "merchants");
        this.accounts = Objects.requireNonNull(accounts, "accounts");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * 购买单个 SKU。
     *
     * @param merchantOrgId 商家组织 id（字符串依赖，可与 Merchant 入驻并行）
     */
    public MallOutcome<MallOrder> execute(
            String userId, String merchantOrgId, String skuId, int qty) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(merchantOrgId, "merchantOrgId");
        Objects.requireNonNull(skuId, "skuId");
        if (qty <= 0) {
            return MallOutcome.err(MallErrorCode.INVALID_QTY, "qty 必须为正");
        }

        MerchantProfile merchant =
                merchants.findByOrgId(merchantOrgId).orElse(null);
        if (merchant == null || !merchant.isActive()) {
            return MallOutcome.err(MallErrorCode.MERCHANT_NOT_ACTIVE, "商家不可交易");
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
        Money total = line.lineTotal();
        PaymentIntent intent = PaymentIntent.balanceOnly(total);

        Account userBalance = accounts.findUserBalance(userId, total.currency());
        if (!userBalance.canCoverCents(total.cents())) {
            return MallOutcome.err(MallErrorCode.INSUFFICIENT_BALANCE, "余额不足");
        }
        Account orgSettlement = accounts.findOrgSettlement(merchantOrgId, total.currency());

        var now = clock.instant();
        MallOrder created =
                MallOrder.create(
                        newId("mord"),
                        userId,
                        merchantOrgId,
                        List.of(line),
                        intent,
                        null,
                        total,
                        now);

        Account debited = userBalance.debit(total.cents());
        Account credited = orgSettlement.credit(total.cents());
        ledger.append(
                LedgerEntry.orderPaymentBalance(
                        newId("led"),
                        userBalance.id(),
                        orgSettlement.id(),
                        total,
                        created.id(),
                        now));
        accounts.save(debited);
        accounts.save(credited);
        LedgerInvariant.assertBalanced(ledger.findAll());

        // INV-16：仅 mark PAID，不创建 Entitlement / UsageEvent
        MallOrder paid = created.pay(now);
        skus.save(afterStock);
        orders.save(paid);
        return MallOutcome.ok(paid);
    }

    private static String newId(String prefix) {
        return prefix + "-" + UUID.randomUUID();
    }
}
