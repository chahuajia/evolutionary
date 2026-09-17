package com.evolutionary.mall.domain;

/** 优惠券减免类型。 */
public enum CouponKind {
    /** 固定减免；value = 分 */
    FIXED_OFF,
    /**
     * 百分比减免；value = 减免百分点（10 = 减 10%，即九折）。
     */
    PERCENT_OFF
}
