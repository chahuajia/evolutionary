package com.evolutionary.commerce.interfaces;

/**
 * 边界解析产物 —— parse-dont-validate：JSON 形状在此变为用例可消费的非空字段。
 */
public record IncomingEntitledSwapRequest(String userId, String entitlementId, String cabinetId) {

    public static IncomingEntitledSwapRequest parse(
            String userId, String entitlementId, String cabinetId) {
        return new IncomingEntitledSwapRequest(
                require("userId", userId),
                require("entitlementId", entitlementId),
                require("cabinetId", cabinetId));
    }

    private static String require(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
        return value.trim();
    }
}
