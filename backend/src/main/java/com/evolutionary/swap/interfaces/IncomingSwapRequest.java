package com.evolutionary.swap.interfaces;

import com.evolutionary.battery.domain.Battery;

/**
 * 边界解析产物 —— parse-dont-validate：JSON 形状在此变为领域可消费类型。
 * 见 specs/round-12.md。
 */
public record IncomingSwapRequest(Battery incomingBattery) {

    public static IncomingSwapRequest parse(String incomingBatteryId) {
        if (incomingBatteryId == null || incomingBatteryId.isBlank()) {
            throw new IllegalArgumentException("incomingBatteryId required");
        }
        Battery incoming = Battery.create(incomingBatteryId.trim()).swapOut();
        return new IncomingSwapRequest(incoming);
    }
}
