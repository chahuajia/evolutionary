package com.evolutionary.mall.domain;

/** 商城域错误码。 */
public enum MallErrorCode {
    SKU_NOT_ON_SALE,
    INSUFFICIENT_STOCK,
    INSUFFICIENT_BALANCE,
    INVALID_QTY,
    MERCHANT_MISMATCH,
    /** 商家档案非 ACTIVE（停用或缺失） */
    MERCHANT_NOT_ACTIVE,
    /** 同 mutexGroup 多张券叠加 */
    COUPON_MUTEX_VIOLATION,
    /** 叠加超过上限（≤2） */
    COUPON_STACK_LIMIT,
    /** 券 scope 与购买标的不匹配 */
    COUPON_SCOPE_MISMATCH,
    /** Campaign 预算不足，无法领券 */
    CAMPAIGN_BUDGET_EXHAUSTED,
    /** 未达最低消费门槛 */
    COUPON_MIN_SPEND_NOT_MET,
    /** 用户券不可用（非 available / 非本人） */
    COUPON_NOT_AVAILABLE,
    /** 活动未激活或不含该模板 */
    CAMPAIGN_NOT_ACTIVE
}
