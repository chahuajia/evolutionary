package com.evolutionary.commerce.domain;

public enum EntitlementStatus {
    ACTIVE,
    /** 信用逾期冻结，可恢复为 ACTIVE（区别于 REVOKED）。 */
    FROZEN,
    EXPIRED,
    REVOKED
}
