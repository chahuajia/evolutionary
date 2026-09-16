package com.evolutionary.swap.interfaces;

import com.evolutionary.swap.application.PerformSwap;
import com.evolutionary.swap.application.StationRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwapConfig {

    @Bean
    PerformSwap performSwap(StationRepository stations) {
        return new PerformSwap(stations);
    }
}
