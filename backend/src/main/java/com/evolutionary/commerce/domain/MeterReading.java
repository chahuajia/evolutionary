package com.evolutionary.commerce.domain;

/** 柜机适配层已解析的 SOC；领域只收 int（parse-dont-validate）。 */
public record MeterReading(int socBefore, int socAfter) {

    public MeterReading {
        if (socBefore < 0 || socAfter < 0) {
            throw new IllegalArgumentException("soc must not be negative");
        }
        if (socAfter > socBefore) {
            throw new IllegalArgumentException("socAfter must not exceed socBefore");
        }
    }

    public int unitsConsumed() {
        return socBefore - socAfter;
    }
}
