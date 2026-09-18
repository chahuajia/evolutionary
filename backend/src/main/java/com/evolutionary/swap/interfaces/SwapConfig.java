package com.evolutionary.swap.interfaces;

import com.evolutionary.swap.application.GetStation;
import com.evolutionary.swap.application.ListStations;
import com.evolutionary.swap.application.ListSwapLogs;
import com.evolutionary.swap.application.PerformSwap;
import com.evolutionary.swap.application.StationRepository;
import com.evolutionary.swap.application.SwapLogRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwapConfig {

    @Bean
    PerformSwap performSwap(StationRepository stations, SwapLogRepository swapLogs) {
        return new PerformSwap(stations, swapLogs, Clock.systemUTC());
    }

    @Bean
    ListStations listStations(StationRepository stations) {
        return new ListStations(stations);
    }

    @Bean
    GetStation getStation(StationRepository stations) {
        return new GetStation(stations);
    }

    @Bean
    ListSwapLogs listSwapLogs(StationRepository stations, SwapLogRepository swapLogs) {
        return new ListSwapLogs(stations, swapLogs);
    }
}
