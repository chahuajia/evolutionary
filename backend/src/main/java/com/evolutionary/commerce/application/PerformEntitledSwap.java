package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import com.evolutionary.commerce.domain.LedgerEntry;
import com.evolutionary.commerce.domain.LedgerInvariant;
import com.evolutionary.commerce.domain.MeterReading;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Product;
import com.evolutionary.commerce.domain.UsageEvent;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 权益换电（phase-0）：一次调用完成 STARTED→COMPLETED，电池 idle→rented→idle。
 *
 * <p>顺带完成切片 2 过期校验；INV-3 在分配前检查同电池是否已有 STARTED。
 * phase-1：无 entitlementId 时走 {@link SelectEntitlement} 默认策略（AC-14）；
 * PAY_AS_YOU_GO 在 COMPLETED 后写 METERED_CHARGE（INV-8 / AC-13）。
 */
public final class PerformEntitledSwap {

    private final EntitlementRepository entitlements;
    private final BatteryAssetRepository batteries;
    private final UsageEventRepository usages;
    private final ProductRepository products;
    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final Clock clock;

    /** 非计量路径（phase-0 / FINITE）；METERED 请用全参构造。 */
    public PerformEntitledSwap(
            EntitlementRepository entitlements,
            BatteryAssetRepository batteries,
            UsageEventRepository usages,
            Clock clock) {
        this(entitlements, batteries, usages, null, null, null, clock);
    }

    public PerformEntitledSwap(
            EntitlementRepository entitlements,
            BatteryAssetRepository batteries,
            UsageEventRepository usages,
            ProductRepository products,
            AccountRepository accounts,
            LedgerRepository ledger,
            Clock clock) {
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.batteries = Objects.requireNonNull(batteries, "batteries");
        this.usages = Objects.requireNonNull(usages, "usages");
        this.products = products;
        this.accounts = accounts;
        this.ledger = ledger;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public DomainOutcome<UsageEvent> execute(String userId, String entitlementId, String cabinetId) {
        return execute(userId, entitlementId, cabinetId, null);
    }

    /** P3 计量入口：soc 已为 int，领域不解析字符串。 */
    public DomainOutcome<UsageEvent> execute(
            String userId, String entitlementId, String cabinetId, int socBefore, int socAfter) {
        return execute(userId, entitlementId, cabinetId, new MeterReading(socBefore, socAfter));
    }

    private DomainOutcome<UsageEvent> execute(
            String userId, String entitlementId, String cabinetId, MeterReading meterReading) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(entitlementId, "entitlementId");
        Objects.requireNonNull(cabinetId, "cabinetId");

        Entitlement entitlement = entitlements.get(entitlementId);
        var now = clock.instant();

        DomainOutcome<Void> gate = gateEntitlement(entitlement, userId, now);
        if (gate instanceof DomainOutcome.Err<Void> err) {
            return DomainOutcome.err(err.code(), err.message());
        }

        BatteryAsset idle = batteries.findAnyIdle().orElse(null);
        if (idle == null) {
            return DomainOutcome.err(DomainErrorCode.BATTERY_NOT_AVAILABLE, "no idle battery");
        }
        return runSwap(userId, entitlementId, cabinetId, idle, now, meterReading);
    }

    /** 无显式 entitlementId：默认优先 FINITE，否则 UNLIMITED（AC-14）。 */
    public DomainOutcome<UsageEvent> executeWithoutId(String userId, String cabinetId) {
        Objects.requireNonNull(userId, "userId");
        Objects.requireNonNull(cabinetId, "cabinetId");

        var now = clock.instant();
        List<Entitlement> active = entitlements.findActiveByUser(userId);
        return SelectEntitlement.selectDefault(active, now)
                .map(selected -> execute(userId, selected.id(), cabinetId))
                .orElseGet(
                        () ->
                                DomainOutcome.err(
                                        DomainErrorCode.ENTITLEMENT_INACTIVE,
                                        "no usable entitlement"));
    }

    /** 柜机已选定电池时的入口。 */
    public DomainOutcome<UsageEvent> executeWithBattery(
            String userId, String entitlementId, String cabinetId, String batteryId) {
        Objects.requireNonNull(batteryId, "batteryId");
        Entitlement entitlement = entitlements.get(entitlementId);
        var now = clock.instant();

        DomainOutcome<Void> gate = gateEntitlement(entitlement, userId, now);
        if (gate instanceof DomainOutcome.Err<Void> err) {
            return DomainOutcome.err(err.code(), err.message());
        }

        BatteryAsset battery = batteries.get(batteryId);
        if (!battery.isIdle()) {
            return DomainOutcome.err(DomainErrorCode.BATTERY_NOT_AVAILABLE, "battery not idle");
        }
        return runSwap(userId, entitlementId, cabinetId, battery, now, null);
    }

    private DomainOutcome<Void> gateEntitlement(Entitlement entitlement, String userId, java.time.Instant now) {
        if (entitlement.status() != EntitlementStatus.ACTIVE) {
            return DomainOutcome.err(DomainErrorCode.ENTITLEMENT_INACTIVE, "entitlement not active");
        }
        if (!entitlement.isActiveAt(now)) {
            return DomainOutcome.err(DomainErrorCode.ENTITLEMENT_EXPIRED, "entitlement expired or not yet valid");
        }
        if (!entitlement.userId().equals(userId)) {
            return DomainOutcome.err(DomainErrorCode.ENTITLEMENT_INACTIVE, "entitlement user mismatch");
        }
        if (entitlement.isExhausted()) {
            return DomainOutcome.err(DomainErrorCode.ENTITLEMENT_EXHAUSTED, "no remaining swaps");
        }
        return DomainOutcome.ok(null);
    }

    private DomainOutcome<UsageEvent> runSwap(
            String userId,
            String entitlementId,
            String cabinetId,
            BatteryAsset battery,
            java.time.Instant now,
            MeterReading meterReading) {
        if (usages.findStartedByBattery(battery.id()).isPresent()) {
            return DomainOutcome.err(
                    DomainErrorCode.BATTERY_ALREADY_RENTED, "battery already has started usage");
        }

        Entitlement entitlement = entitlements.get(entitlementId);

        Money charge = null;
        Product meteredProduct = null;
        if (entitlement.isPayAsYouGo()) {
            DomainOutcome<Money> priced = priceMetered(entitlement, meterReading, userId);
            if (priced instanceof DomainOutcome.Err<Money> err) {
                return DomainOutcome.err(err.code(), err.message());
            }
            charge = ((DomainOutcome.Ok<Money>) priced).value();
            meteredProduct = products.get(entitlement.productId());
        }

        UsageEvent started =
                UsageEvent.start(
                        "ue-" + UUID.randomUUID(),
                        userId,
                        entitlementId,
                        battery.id(),
                        cabinetId,
                        now);
        BatteryAsset rented = battery.checkout(userId);
        batteries.save(rented);
        usages.save(started);

        UsageEvent completed = started.complete(now, meterReading, charge);
        BatteryAsset returned = rented.returnToIdle();
        batteries.save(returned);
        usages.save(completed);

        // INV-8：COMPLETED 之后写 METERED_CHARGE
        if (charge != null) {
            settleMeteredCharge(userId, meteredProduct, charge, completed.id(), now);
        }

        // INV-6：COMPLETED 后扣次（PAY_AS_YOU_GO 无 remainingSwaps，no-op）
        Entitlement consumed = entitlement.consumeSwap();
        entitlements.save(consumed);

        return DomainOutcome.ok(completed);
    }

    private DomainOutcome<Money> priceMetered(
            Entitlement entitlement, MeterReading meterReading, String userId) {
        if (products == null || accounts == null || ledger == null) {
            throw new IllegalStateException("METERED path requires Product/Account/Ledger repositories");
        }
        if (meterReading == null) {
            throw new IllegalArgumentException("soc meter reading required for PAY_AS_YOU_GO");
        }
        Product product = products.get(entitlement.productId());
        if (!product.isMetered()) {
            throw new IllegalStateException("PAY_AS_YOU_GO entitlement requires METERED product");
        }
        Money rate = product.meteredRate();
        Money charge = new Money(meterReading.unitsConsumed() * rate.cents(), rate.currency());
        Account userBalance = accounts.findUserBalance(userId, charge.currency());
        // AC-16：余额不足 → 不写分录、不 COMPLETED（本方法在 start 前调用）
        if (!userBalance.canCover(charge)) {
            return DomainOutcome.err(DomainErrorCode.INSUFFICIENT_BALANCE, "insufficient balance for metered charge");
        }
        return DomainOutcome.ok(charge);
    }

    private void settleMeteredCharge(
            String userId, Product product, Money charge, String usageEventId, java.time.Instant now) {
        Account userBalance = accounts.findUserBalance(userId, charge.currency());
        Account orgSettlement = accounts.findOrgSettlement(product.orgId(), charge.currency());

        Account debitedUser = userBalance.debit(charge.cents());
        Account creditedOrg = orgSettlement.credit(charge.cents());

        LedgerEntry entry =
                LedgerEntry.meteredCharge(
                        "led-" + UUID.randomUUID(),
                        userBalance.id(),
                        orgSettlement.id(),
                        charge,
                        usageEventId,
                        now);

        ledger.append(entry);
        LedgerInvariant.assertBalanced(ledger.findAll());

        accounts.save(debitedUser);
        accounts.save(creditedOrg);
    }
}
