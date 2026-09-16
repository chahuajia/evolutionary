package com.evolutionary.swap.application;

import com.evolutionary.station.domain.Station;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** 列出全部站点（同上下文读用例）。见 specs/round-13.md */
public final class ListStations {

    private final StationRepository stations;

    public ListStations(StationRepository stations) {
        this.stations = Objects.requireNonNull(stations, "stations");
    }

    public List<Station> execute() {
        return stations.findAll().stream()
                .sorted(Comparator.comparing(Station::id))
                .toList();
    }
}
