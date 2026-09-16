package com.evolutionary.swap.application;

import com.evolutionary.station.domain.Station;
import java.util.Objects;

/** 按 id 读取站点（同上下文读用例）。见 specs/round-13.md */
public final class GetStation {

    private final StationRepository stations;

    public GetStation(StationRepository stations) {
        this.stations = Objects.requireNonNull(stations, "stations");
    }

    public Station execute(String stationId) {
        Objects.requireNonNull(stationId, "stationId");
        return stations.get(stationId);
    }
}
