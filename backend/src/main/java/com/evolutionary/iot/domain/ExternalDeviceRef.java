package com.evolutionary.iot.domain;

import java.util.Objects;

/** 外部设备引用（适配器发现结果）。 */
public record ExternalDeviceRef(String externalDeviceId, String vendorId) {
    public ExternalDeviceRef {
        Objects.requireNonNull(externalDeviceId, "externalDeviceId");
        Objects.requireNonNull(vendorId, "vendorId");
    }
}
