package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.BatteryAsset;
import com.evolutionary.commerce.domain.DomainErrorCode;
import com.evolutionary.commerce.domain.DomainOutcome;
import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.EntitlementStatus;
import com.evolutionary.commerce.domain.UsageEvent;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 权益换电（phase-0）：一次调用完成 STARTED→COMPLETED，电池 idle→rented→idle。
 *
 * <p>顺带完成切片 2 过期校验；INV-3 在分配前检查同电池是否已有 STARTED。
 */
public final class PerformEntitledSwap {

    private final EntitlementRepository entitlements;
    private final BatteryAssetRepository batteries;
    private final UsageEventRepository usages;
    private final Clock clock;

    public PerformEntitledSwap(
            EntitlementRepository entitlements,
            BatteryAssetRepository batteries,
            UsageEventRepository usages,
            Clock clock) {
        this.entitlements = Objects.requireNonNull(entitlements, "entitlements");
        this.batteries = Objects.requireNonNull(batteries, "batteries");
        this.usages = Objects.requireNonNull(usages, "usages");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public DomainOutcome<UsageEvent> execute(String userId, String entitlementId, String cabinetId) {
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
        return runSwap(userId, entitlementId, cabinetId, idle, now);
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
        return runSwap(userId, entitlementId, cabinetId, battery, now);
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
        return DomainOutcome.ok(null);
    }

    private DomainOutcome<UsageEvent> runSwap(
            String userId,
            String entitlementId,
            String cabinetId,
            BatteryAsset battery,
            java.time.Instant now) {
        if (usages.findStartedByBattery(battery.id()).isPresent()) {
            return DomainOutcome.err(
                    DomainErrorCode.BATTERY_ALREADY_RENTED, "battery already has started usage");
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

        UsageEvent completed = started.complete(now);
        BatteryAsset returned = rented.returnToIdle();
        batteries.save(returned);
        usages.save(completed);

        return DomainOutcome.ok(completed);
    }
}
