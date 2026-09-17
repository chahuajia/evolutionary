package com.evolutionary.iot.domain;

/** 遥测解析失败。 */
public final class ParseError extends RuntimeException {
    public ParseError(String message) {
        super(message);
    }
}
