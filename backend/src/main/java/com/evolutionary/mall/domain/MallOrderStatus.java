package com.evolutionary.mall.domain;

/** 商城订单状态（与换电 Order 独立）。 */
public enum MallOrderStatus {
    CREATED,
    PAID,
    SHIPPED,
    COMPLETED,
    REFUNDED
}
