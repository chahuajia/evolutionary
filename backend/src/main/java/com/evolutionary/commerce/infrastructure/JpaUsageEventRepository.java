package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.UsageEventRepository;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.MeterReading;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.UsageEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 用量事件 JPA 适配。 */
@Component
public final class JpaUsageEventRepository implements UsageEventRepository {

    private final UsageEventJpaRepository jpa;

    public JpaUsageEventRepository(UsageEventJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(UsageEvent event) {
        Integer socBefore = null;
        Integer socAfter = null;
        if (event.meterReading() != null) {
            socBefore = event.meterReading().socBefore();
            socAfter = event.meterReading().socAfter();
        }
        Long cents = null;
        String currency = null;
        if (event.chargedAmount() != null) {
            cents = event.chargedAmount().cents();
            currency = event.chargedAmount().currency().name();
        }
        jpa.save(
                new UsageEventJpaEntity(
                        event.id(),
                        event.userId(),
                        event.entitlementId(),
                        event.batteryId(),
                        event.cabinetId(),
                        event.status(),
                        event.startedAt(),
                        event.completedAt(),
                        socBefore,
                        socAfter,
                        cents,
                        currency));
    }

    @Override
    public Optional<UsageEvent> findStartedByBattery(String batteryId) {
        return jpa.findFirstByBatteryIdAndStatus(batteryId, UsageEvent.Status.STARTED)
                .map(JpaUsageEventRepository::toDomain);
    }

    @Override
    public List<UsageEvent> findStartedByUser(String userId) {
        List<UsageEvent> result = new ArrayList<>();
        for (UsageEventJpaEntity row :
                jpa.findByUserIdAndStatus(userId, UsageEvent.Status.STARTED)) {
            result.add(toDomain(row));
        }
        return result;
    }

    @Override
    public Optional<UsageEvent> findStartedByEntitlement(String entitlementId) {
        return jpa.findFirstByEntitlementIdAndStatus(entitlementId, UsageEvent.Status.STARTED)
                .map(JpaUsageEventRepository::toDomain);
    }

    private static UsageEvent toDomain(UsageEventJpaEntity row) {
        MeterReading meter = null;
        if (row.getMeterSocBefore() != null && row.getMeterSocAfter() != null) {
            meter = new MeterReading(row.getMeterSocBefore(), row.getMeterSocAfter());
        }
        Money charged = null;
        if (row.getChargedCents() != null && row.getChargedCurrency() != null) {
            charged =
                    new Money(
                            row.getChargedCents(), Currency.valueOf(row.getChargedCurrency()));
        }
        return UsageEvent.rehydrate(
                row.getId(),
                row.getUserId(),
                row.getEntitlementId(),
                row.getBatteryId(),
                row.getCabinetId(),
                row.getStatus(),
                row.getStartedAt(),
                row.getCompletedAt(),
                meter,
                charged);
    }
}
