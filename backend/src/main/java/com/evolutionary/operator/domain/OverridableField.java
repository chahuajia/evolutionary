package com.evolutionary.operator.domain;

import java.util.Locale;
import java.util.Optional;

/** L1 发布模板时声明的可覆盖字段白名单（wire 名对齐 IDL）。 */
public enum OverridableField {
    PRICE("price"),
    DISPLAY_NAME("displayName");

    private final String wireName;

    OverridableField(String wireName) {
        this.wireName = wireName;
    }

    public String wireName() {
        return wireName;
    }

    /** 按契约 wire 名解析；未知字段返回 empty（调用方映射为 FIELD_NOT_OVERRIDABLE）。 */
    public static Optional<OverridableField> fromWire(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        for (OverridableField field : values()) {
            if (field.wireName.equals(name)) {
                return Optional.of(field);
            }
        }
        // 兼容全大写枚举名误传
        try {
            return Optional.of(valueOf(name.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException ignored) {
            return Optional.empty();
        }
    }
}
