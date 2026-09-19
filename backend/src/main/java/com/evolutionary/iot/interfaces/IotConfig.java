package com.evolutionary.iot.interfaces;

import com.evolutionary.iot.application.AlertStore;
import com.evolutionary.iot.application.ApplyTelemetryToShadow;
import com.evolutionary.iot.application.DetectCommLost;
import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.application.TelemetryStore;
import com.evolutionary.iot.application.TriageOutdatedSoc;
import com.evolutionary.iot.domain.DeviceShadow;
import com.evolutionary.iot.infrastructure.InMemoryMaintenanceTicketRepository;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IotConfig {

    /** DeviceShadowRepository → {@code JpaDeviceShadowRepository}（表 device_shadows）。 */

    // AlertStore → JpaAlertStore（表 battery_alerts）

    @Bean
    MaintenanceTicketRepository maintenanceTicketRepository() {
        return new InMemoryMaintenanceTicketRepository();
    }

    // TelemetryStore → JpaTelemetryStore（表 telemetry_records）
    // CommandDispatchLogRepository → JpaCommandDispatchLogRepository（表 command_dispatch_logs）

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

    @Bean
    TriageOutdatedSoc triageOutdatedSoc(DeviceShadowRepository shadows) {
        return new TriageOutdatedSoc(shadows, Clock.systemUTC());
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
