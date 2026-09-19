package com.evolutionary.iot.interfaces;

import com.evolutionary.commerce.application.TelemetryFreshnessPort;
import com.evolutionary.iot.application.AlertStore;
import com.evolutionary.iot.application.ApplyTelemetryToShadow;
import com.evolutionary.iot.application.AssertShadowFreshForMetered;
import com.evolutionary.iot.application.DetectCommLost;
import com.evolutionary.iot.application.DeviceShadowRepository;
import com.evolutionary.iot.application.MaintenanceTicketRepository;
import com.evolutionary.iot.application.ResolveMaintenanceTicket;
import com.evolutionary.iot.application.ShadowTelemetryFreshnessAdapter;
import com.evolutionary.iot.application.TelemetryStore;
import com.evolutionary.iot.application.TriageOutdatedSoc;
import com.evolutionary.iot.domain.DeviceShadow;
import java.time.Clock;
import java.time.Instant;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IotConfig {

    /** DeviceShadowRepository → {@code JpaDeviceShadowRepository}（表 device_shadows）。 */

    // AlertStore → JpaAlertStore（表 battery_alerts）

    // MaintenanceTicketRepository → JpaMaintenanceTicketRepository（表 maintenance_tickets）
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

    @Bean
    ResolveMaintenanceTicket resolveMaintenanceTicket(MaintenanceTicketRepository tickets) {
        return new ResolveMaintenanceTicket(tickets);
    }

    /** AC-58：计量换电前影子新鲜度守卫。 */
    @Bean
    AssertShadowFreshForMetered assertShadowFreshForMetered(DeviceShadowRepository shadows) {
        return new AssertShadowFreshForMetered(shadows, Clock.systemUTC());
    }

    @Bean
    TelemetryFreshnessPort telemetryFreshnessPort(AssertShadowFreshForMetered guard) {
        return new ShadowTelemetryFreshnessAdapter(guard);
    }

    /**
     * 正式本地种子：
     *
     * <ul>
     *   <li>BAT-IOT-1 stale —— COMM_LOST / triage 联调
     *   <li>BAT-1 / BAT-M1 fresh —— 权益换电 findAnyIdle 计量门（AC-58）
     * </ul>
     */
    @Bean
    ApplicationRunner seedIot(DeviceShadowRepository shadows) {
        return args -> {
            Instant now = Instant.now();
            Instant staleSeen =
                    now.minus(DeviceShadow.STALE_AFTER).minusSeconds(120);
            shadows.save(
                    DeviceShadow.seed("BAT-IOT-1", "vendorA", "ext-iot-1", 42, 3800, staleSeen));
            shadows.save(DeviceShadow.seed("BAT-1", "vendorA", "ext-bat-1", 80, 4200, now));
            shadows.save(DeviceShadow.seed("BAT-M1", "vendorA", "ext-bat-m1", 80, 4200, now));
        };
    }
}
