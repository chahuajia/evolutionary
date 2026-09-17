package com.evolutionary.commerce.interfaces;

/**
 * 边界解析产物 —— parse-dont-validate。
 *
 * <p>{@code socBefore}/{@code socAfter} 须同时出现或同时缺省；同时出现则计量入口。
 * <p>省略或空 {@code entitlementId} 时标记 default-select（AC-14）；计量路径仍要求显式 id。
 */
public record IncomingEntitledSwapRequest(
        String userId,
        String entitlementId,
        String cabinetId,
        Integer socBefore,
        Integer socAfter,
        boolean defaultSelect) {

    public static IncomingEntitledSwapRequest parse(
            String userId,
            String entitlementId,
            String cabinetId,
            Integer socBefore,
            Integer socAfter) {
        if ((socBefore == null) != (socAfter == null)) {
            throw new IllegalArgumentException("socBefore and socAfter must both be set or both omitted");
        }
        String uid = require("userId", userId);
        String cab = require("cabinetId", cabinetId);
        boolean blankEntitlement = entitlementId == null || entitlementId.isBlank();
        boolean metered = socBefore != null;
        if (metered && blankEntitlement) {
            throw new IllegalArgumentException("entitlementId required");
        }
        if (blankEntitlement) {
            return new IncomingEntitledSwapRequest(uid, null, cab, socBefore, socAfter, true);
        }
        return new IncomingEntitledSwapRequest(
                uid, entitlementId.trim(), cab, socBefore, socAfter, false);
    }

    public boolean isMetered() {
        return socBefore != null;
    }

    /** 省略/空 entitlementId → 默认选卡路径（非计量）。 */
    public boolean useDefaultSelect() {
        return defaultSelect;
    }

    private static String require(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
        return value.trim();
    }
}
