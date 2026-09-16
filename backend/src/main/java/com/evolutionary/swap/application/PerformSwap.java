package com.evolutionary.swap.application;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.domain.SwapSession;
import java.util.Objects;

/**
 * 换电用例：取站 → 领域换电 → 存站 → 返回会话。
 *
 * <p>与 domain 共享同一套语言（Station / Battery / SwapSession）→
 * 同一限界上下文里的<strong>外层</strong>，不是新上下文（{@code layer-vs-context}）。
 */
public final class PerformSwap {

    private final StationRepository stations;

    public PerformSwap(StationRepository stations) {
        this.stations = Objects.requireNonNull(stations, "stations");
    }

    public SwapSession execute(String stationId, Battery incoming) {
        Objects.requireNonNull(stationId, "stationId");
        Objects.requireNonNull(incoming, "incoming");

        Station station = stations.get(stationId);
        Station.Swap swap = station.swap(incoming);
        stations.save(swap.station());
        return swap.session();
    }
}
