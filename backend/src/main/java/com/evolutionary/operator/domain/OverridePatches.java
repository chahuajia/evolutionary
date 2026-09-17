package com.evolutionary.operator.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 覆盖补丁（仅 price / displayName）。
 *
 * <p>从原始 map 解析时校验 key ⊆ 模板 {@code allowedOverrideFields}，否则
 * {@link OperatorErrorCode#FIELD_NOT_OVERRIDABLE}。
 */
public final class OverridePatches {

    private final Long priceCents;
    private final String displayName;

    private OverridePatches(Long priceCents, String displayName) {
        this.priceCents = priceCents;
        this.displayName = displayName;
    }

    public static OverridePatches priceOnly(long priceCents) {
        if (priceCents < 0) {
            throw new IllegalArgumentException("priceCents 不能为负");
        }
        return new OverridePatches(priceCents, null);
    }

    public static OverridePatches of(Long priceCents, String displayName) {
        if (priceCents != null && priceCents < 0) {
            throw new IllegalArgumentException("priceCents 不能为负");
        }
        if (displayName != null && displayName.isBlank()) {
            throw new IllegalArgumentException("displayName 不能为空白");
        }
        return new OverridePatches(priceCents, displayName);
    }

    /**
     * 从原始补丁 map 解析并做白名单校验。
     *
     * <p>未知 key（如 {@code durationDays}）或未在 allowed 中声明 → FIELD_NOT_OVERRIDABLE。
     */
    public static OperatorOutcome<OverridePatches> fromRaw(
            Map<String, ?> raw, List<OverridableField> allowed) {
        Objects.requireNonNull(raw, "raw");
        Objects.requireNonNull(allowed, "allowed");
        if (raw.isEmpty()) {
            return OperatorOutcome.err(OperatorErrorCode.OVERRIDE_INVALID, "patches 不能为空");
        }

        Long price = null;
        String name = null;
        for (Map.Entry<String, ?> entry : raw.entrySet()) {
            Optional<OverridableField> field = OverridableField.fromWire(entry.getKey());
            if (field.isEmpty() || !allowed.contains(field.get())) {
                return OperatorOutcome.err(
                        OperatorErrorCode.FIELD_NOT_OVERRIDABLE,
                        "字段不可覆盖: " + entry.getKey());
            }
            switch (field.get()) {
                case PRICE -> {
                    Object v = entry.getValue();
                    if (!(v instanceof Number n)) {
                        return OperatorOutcome.err(
                                OperatorErrorCode.OVERRIDE_INVALID, "price 必须为数字");
                    }
                    price = n.longValue();
                    if (price < 0) {
                        return OperatorOutcome.err(
                                OperatorErrorCode.OVERRIDE_INVALID, "price 不能为负");
                    }
                }
                case DISPLAY_NAME -> {
                    if (!(entry.getValue() instanceof String s) || s.isBlank()) {
                        return OperatorOutcome.err(
                                OperatorErrorCode.OVERRIDE_INVALID, "displayName 必须为非空字符串");
                    }
                    name = s;
                }
            }
        }
        return OperatorOutcome.ok(new OverridePatches(price, name));
    }

    /** 将补丁叠到模板 base 上（未 patch 的字段沿用 base）。 */
    public TemplateBaseProduct applyTo(TemplateBaseProduct base) {
        Objects.requireNonNull(base, "base");
        TemplateBaseProduct next = base;
        if (priceCents != null) {
            next = next.withPriceCents(priceCents);
        }
        if (displayName != null) {
            next = next.withDisplayName(displayName);
        }
        return next;
    }

    public boolean isEmpty() {
        return priceCents == null && displayName == null;
    }

    public Optional<Long> priceCents() {
        return Optional.ofNullable(priceCents);
    }

    public Optional<String> displayName() {
        return Optional.ofNullable(displayName);
    }
}
