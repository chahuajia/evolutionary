package com.evolutionary.swap.application;

/** 端口读不到站点 —— 供边界翻译为 404，禁止用消息前缀猜测。 */
public final class UnknownStationException extends RuntimeException {

    public UnknownStationException(String stationId) {
        super("unknown station: " + stationId);
    }
}
