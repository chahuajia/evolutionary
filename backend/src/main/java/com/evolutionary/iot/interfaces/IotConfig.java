package com.evolutionary.iot.interfaces;

import com.evolutionary.iot.application.AlertStore;
import com.evolutionary.iot.application.ApplyTelemetryToShadow;
import com.evolutionary.iot.application.DetectCommLost;
import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.application.TelemetryStore;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.infrastructure.InMemoryAlertStore;
import com.evolutionary.iot.infrastructure.InMemoryDeviceShadowRepository;
import com.evolutionary.iot.infrastructure.InMemoryMaintenanceTicketRepository;
import com.evolutionary.iot.infrastructure.InMemoryTelemetryStore;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IotConfig {

    @Bean
    DeviceShadowRepository deviceShadowRepository() {
        return new InMemoryDeviceShadowRepository();
    }

    @Bean
    AlertStore alertStore() {
        return new InMemoryAlertStore();
    }

    @Bean
    MaintenanceTicketRepository maintenanceTicketRepository() {
        return new InMemoryMaintenanceTicketRepository();
    }

    @Bean
    TelemetryStore telemetryStore() {
        return new InMemoryTelemetryStore();
    }

    @Bean
    ApplyTelemetryToShadow applyTelemetryToShadow(
            DeviceShadowRepository shadows, TelemetryStore telemetryStore) {
        return new ApplyTelemetryToShadow(shadows, telemetryStore, Clock.systemUTC());
    }

    @Bean
    DetectCommLost detectCommLost(
            DeviceShadowRepository shadows,
            AlertStore alerts,
            MaintenanceTicketRepository tickets) {
        return new DetectCommLost(shadows, alerts, tickets, Clock.systemUTC());
    }

    /**
     * 正式本地种子：BAT-IOT-1 lastSeen 远超 {@link DeviceShadow#STALE_AFTER}，便于 COMM_LOST 联调。
     */
    @Bean
    ApplicationRunner seedIot(DeviceShadowRepository shadows) {
        return args -> {
            Instant lastSeen =
                    Instant.now().minus(DeviceShadow.STALE_AFTER).minusSeconds(120);
            shadows.save(
                    DeviceShadow.seed("BAT-IOT-1", "vendorA", "ext-iot-1", 42, 3800, lastSeen));
        };
    }
}
