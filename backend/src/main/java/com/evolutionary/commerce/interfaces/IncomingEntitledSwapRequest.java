package com.evolutionary.commerce.interfaces;

/**
 * 边界解析产物 —— parse-dont-validate。
 *
 * <p>{@code socBefore}/{@code socAfter} 须同时出现或同时缺省；同时出现则计量入口。
 */
public record IncomingEntitledSwapRequest(
        String userId,
        String entitlementId,
        String cabinetId,
        Integer socBefore,
        Integer socAfter) {

    public static IncomingEntitledSwapRequest parse(
            String userId,
            String entitlementId,
            String cabinetId,
            Integer socBefore,
            Integer socAfter) {
        if ((socBefore == null) != (socAfter == null)) {
            throw new IllegalArgumentException("socBefore and socAfter must both be set or both omitted");
        }
        return new IncomingEntitledSwapRequest(
                require("userId", userId),
                require("entitlementId", entitlementId),
                require("cabinetId", cabinetId),
                socBefore,
                socAfter);
    }

    public boolean isMetered() {
        return socBefore != null;
    }

    private static String require(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
        return value.trim();
    }
}
