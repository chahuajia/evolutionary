package com.evolutionary.swap.infrastructure;

import com.evolutionary.swap.application.SwapLogRepository;
import com.evolutionary.swap.domain.SwapLog;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/** 换电日志 JPA 适配。 */
@Component
public final class JpaSwapLogRepository implements SwapLogRepository {

    private final SwapLogJpaRepository jpa;

    public JpaSwapLogRepository(SwapLogJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void append(SwapLog log) {
        jpa.save(
                new SwapLogJpaEntity(
                        log.id(),
                        log.stationId(),
                        log.outgoingBatteryId(),
                        log.incomingBatteryId(),
                        log.occurredAt()));
    }

    @Override
    public List<SwapLog> findByStationId(String stationId) {
        List<SwapLog> result = new ArrayList<>();
        for (SwapLogJpaEntity row : jpa.findByStationIdOrderByOccurredAtAsc(stationId)) {
            result.add(
                    SwapLog.of(
                            row.getId(),
                            row.getStationId(),
                            row.getOutgoingBatteryId(),
                            row.getIncomingBatteryId(),
                            row.getOccurredAt()));
        }
        return result;
    }
}
