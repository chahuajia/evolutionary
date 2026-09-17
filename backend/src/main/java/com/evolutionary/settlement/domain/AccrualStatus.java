package com.evolutionary.settlement.domain;

/** 分润意向状态（追加-only：SETTLED / REVERSED 由后续切片写入）。 */
public enum AccrualStatus {
    PENDING,
    SETTLED,
    REVERSED
}
