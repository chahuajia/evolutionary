package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.DeviceShadow;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * 监控页 SOC 过时诊断（AC-61 / W1）。
 *
 * <p>必须先查 shadow.stale / lastSeenAt，再考虑适配器连通性。
 */
public final class TriageOutdatedSoc {

    public enum NextStep {
        /** 影子已过期——先处理遥测/通信，不要先怪适配器。 */
        SHADOW_STALE,
        /** 影子新鲜——再查适配器连通性。 */
        CHECK_ADAPTER
    }

    private final DeviceShadowRepository shadows;
    private final Clock clock;

    public TriageOutdatedSoc(DeviceShadowRepository shadows, Clock clock) {
        this.shadows = Objects.requireNonNull(shadows, "shadows");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public Report execute(String batteryId) {
        Objects.requireNonNull(batteryId, "batteryId");
        DeviceShadow shadow = shadows.get(batteryId).refreshStale(clock.instant());
        shadows.save(shadow);

        if (shadow.stale()) {
            return new Report(
                    shadow,
                    NextStep.SHADOW_STALE,
                    List.of(
                            "检查 shadow.stale 与 lastSeenAt",
                            "确认 COMM_LOST / 工单",
                            "（暂缓）适配器连通性"));
        }
        return new Report(
                shadow,
                NextStep.CHECK_ADAPTER,
                List.of("shadow 新鲜（stale=false）", "再查适配器连通性 / parse 路径"));
    }

    public record Report(DeviceShadow shadow, NextStep nextStep, List<String> orderedChecks) {}
}
