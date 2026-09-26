package com.evolutionary.swap.application;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.domain.SwapLog;
import com.evolutionary.swap.domain.SwapSession;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

/**
 * 换电用例：取站 → 领域换电 → <strong>同事务双写</strong>（存站 + 追加换电日志）→ 返回会话。
 *
 * <p>唯一写入口：业务不得绕过本用例只改站库存，否则会漏日志。
 */
public final class PerformSwap {

    private final StationRepository stations;
    private final SwapLogRepository swapLogs;
    private final Clock clock;

    public PerformSwap(StationRepository stations, SwapLogRepository swapLogs, Clock clock) {
        this.stations = Objects.requireNonNull(stations, "stations");
        this.swapLogs = Objects.requireNonNull(swapLogs, "swapLogs");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public SwapSession execute(String stationId, Battery incoming) {
        Objects.requireNonNull(stationId, "stationId");
        Objects.requireNonNull(incoming, "incoming");

        Station station = stations.get(stationId);
        Station.Swap swap = station.swap(incoming);
        stations.save(swap.station());

        SwapSession session = swap.session();
        swapLogs.append(
                SwapLog.fromSession(UUID.randomUUID().toString(), session, clock.instant()));
        return session;
    }
}
