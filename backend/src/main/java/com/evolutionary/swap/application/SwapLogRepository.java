package com.evolutionary.swap.application;

import com.evolutionary.swap.domain.SwapLog;
import java.util.List;

/** 换电日志端口：仅应由 {@link PerformSwap} 在换电成功后 append（同事务双写）。 */
public interface SwapLogRepository {

    void append(SwapLog log);

    List<SwapLog> findByStationId(String stationId);
}
