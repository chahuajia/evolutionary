package com.evolutionary.swap.interfaces;

import com.evolutionary.battery.domain.Battery;
import com.evolutionary.station.domain.Station;
import com.evolutionary.swap.infrastructure.JpaStationRepository;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 开发/正式本地种子：S1/S2/S3 供 FE RSC 站列表与联调。
 *
 * <p>与 {@code CreditConfig} U1 共用同一 Spring 启动种子面；H2 内存，重启重置。
 */
@Configuration
public class DevSeedConfig {

    @Bean
    ApplicationRunner seedStations(JpaStationRepository stations) {
        return args -> {
            stations.seed(Station.create("S1", "东门站", List.of(Battery.create("B-out"))));
            stations.seed(Station.create("S2", "西门站", List.of(Battery.create("B-west"))));
            stations.seed(Station.create("S3", "南站", List.of()));
        };
    }
}
