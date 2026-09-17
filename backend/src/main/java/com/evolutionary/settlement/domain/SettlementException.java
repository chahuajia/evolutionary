package com.evolutionary.settlement.domain;

/** 分润 / 结算业务异常。 */
public final class SettlementException extends RuntimeException {

    private final SettlementErrorCode code;

    public SettlementException(SettlementErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public SettlementErrorCode code() {
        return code;
    }
}
