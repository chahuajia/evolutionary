package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.DomainOutcome;

/**
 * 计量换电前的遥测新鲜度端口（INV-19 / AC-58）。
 *
 * <p>由 IoT DeviceShadow 适配实现；commerce 不依赖厂商。
 */
@FunctionalInterface
public interface TelemetryFreshnessPort {

    DomainOutcome<Void> assertFresh(String batteryId);
}
