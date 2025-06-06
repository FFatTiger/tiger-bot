package com.fffattiger.wechatbot.infrastructure.external.wchat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    SYS("sys"),
    TIME("time"),
    RECALL("recall"),
    FRIEND("friend"),
    SELF("self"),
    UNKNOWN("unknown"); // 兼容未来扩展

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MessageType fromValue(String value) {
        for (MessageType t : MessageType.values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        return UNKNOWN;
    }
} 