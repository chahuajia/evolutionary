package com.evolutionary.battery.domain;

import java.util.Objects;

/**
 * 电池实体 —— 有身份（{@code id}），状态可流转。
 *
 * <p><b>不变量</b>：状态只能沿 {@code AVAILABLE → IN_USE → CHARGING → AVAILABLE}
 * 流转，任何状态都可进入 {@code RETIRED}（终态）。
 *
 * <p><b>为什么不可变</b>：每次流转返回**新的 Battery**，而不是原地改字段。
 * 这样"旧状态"永远不会被意外保留，也不需要防御性拷贝。
 *
 * <p><b>零依赖</b>：本类不 import 任何框架（Spring / JPA 都不）。
 */
public final class Battery {

    private final String id;
    private final BatteryStatus status;

    private Battery(String id, BatteryStatus status) {
        this.id = id;
        this.status = status;
    }

    /**
     * 创建一块电池，初始状态为 {@code AVAILABLE}。
     *
     * @throws IllegalArgumentException id 为空
     */
    public static Battery create(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("battery id must not be blank");
        }
        return new Battery(id, BatteryStatus.AVAILABLE);
    }

    /**
     * 从已知状态恢复（仓储用）。
     *
     * <p>与 {@link #create} 分开：一个是"新造"，一个是"恢复"——
     * 混在一起会让"谁负责校验"变模糊。
     */
    public static Battery rehydrate(String id, BatteryStatus status) {
        return new Battery(id, Objects.requireNonNull(status, "status"));
    }

    public String id() {
        return id;
    }

    public BatteryStatus status() {
        return status;
    }

    /** 换出：{@code AVAILABLE → IN_USE}。 */
    public Battery swapOut() {
        return transitionTo(BatteryStatus.IN_USE, BatteryStatus.AVAILABLE);
    }

    /** 归还充电：{@code IN_USE → CHARGING}。 */
    public Battery returnForCharging() {
        return transitionTo(BatteryStatus.CHARGING, BatteryStatus.IN_USE);
    }

    /** 充满：{@code CHARGING → AVAILABLE}。 */
    public Battery finishCharging() {
        return transitionTo(BatteryStatus.AVAILABLE, BatteryStatus.CHARGING);
    }

    /** 退役：任何非终态 → {@code RETIRED}。 */
    public Battery retire() {
        if (status == BatteryStatus.RETIRED) {
            throw illegal(BatteryStatus.RETIRED);
        }
        return new Battery(id, BatteryStatus.RETIRED);
    }

    private Battery transitionTo(BatteryStatus next, BatteryStatus required) {
        if (status != required) {
            throw illegal(next);
        }
        return new Battery(id, next);
    }

    /**
     * 非法流转异常。
     *
     * <p><b>取舍</b>：本类用异常表达"非法流转"（调用方违反状态机时立刻炸），
     * 而不是返回一个结果类型。取舍理由：这里**只可能是编程错误**，不是可预期的业务失败
     * —— 可预期的失败（如"余额不足"）才该用结果类型逐层返回。
     */
    public static final class IllegalTransitionException extends RuntimeException {
        IllegalTransitionException(BatteryStatus next) {
            super("illegal battery transition to " + next);
        }
    }

    private IllegalTransitionException illegal(BatteryStatus next) {
        return new IllegalTransitionException(next);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Battery b && id.equals(b.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Battery[" + id + " " + status + "]";
    }
}
