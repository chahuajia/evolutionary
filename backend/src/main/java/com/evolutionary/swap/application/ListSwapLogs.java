package com.evolutionary.swap.application;

import com.evolutionary.swap.domain.SwapLog;
import java.util.List;
import java.util.Objects;

/** 按站查询换电日志（只读）；未知站抛 {@link UnknownStationException}。 */
public final class ListSwapLogs {

    private final StationRepository stations;
    private final SwapLogRepository swapLogs;

    public ListSwapLogs(StationRepository stations, SwapLogRepository swapLogs) {
        this.stations = Objects.requireNonNull(stations, "stations");
        this.swapLogs = Objects.requireNonNull(swapLogs, "swapLogs");
    }

    public List<SwapLog> execute(String stationId) {
        Objects.requireNonNull(stationId, "stationId");
        stations.get(stationId);
        return swapLogs.findByStationId(stationId);
    }
}
