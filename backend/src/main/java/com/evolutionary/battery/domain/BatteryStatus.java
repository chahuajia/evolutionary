package com.evolutionary.battery.domain;

/**
 * 电池在换电站中的状态。
 *
 * <p>用 enum 而不是字符串常量：状态是**封闭集合**，编译器能穷尽检查；
 * 而且 {@code "available"} 这种字面量一旦跨模块传，就再也说不清它是"哪一层"的 available。
 */
public enum BatteryStatus {
    /** 在站点架上，可被换出 */
    AVAILABLE,
    /** 已被用户取走，在路上 */
    IN_USE,
    /** 已归还，正在充电 */
    CHARGING,
    /** 退役，不再参与换电 */
    RETIRED
}
