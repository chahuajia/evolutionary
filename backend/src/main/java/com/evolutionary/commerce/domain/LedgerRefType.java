package com.evolutionary.commerce.domain;

/**
 * 账本引用类型。
 *
 * <p>phase-2：余额与积分分录必须用不同 refType，禁止混账（INV-10 / AC-22）。
 */
public enum LedgerRefType {
    /** @deprecated 请用 ORDER_PAYMENT_BALANCE；保留以兼容旧测试数据 */
    ORDER_PAYMENT,
    ORDER_PAYMENT_BALANCE,
    ORDER_PAYMENT_POINTS,
    /** @deprecated 请用 ORDER_REFUND_BALANCE */
    ORDER_REFUND,
    ORDER_REFUND_BALANCE,
    ORDER_REFUND_POINTS,
    METERED_CHARGE
}
