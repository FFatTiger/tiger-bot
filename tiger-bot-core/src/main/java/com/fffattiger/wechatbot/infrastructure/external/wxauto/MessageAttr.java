package com.fffattiger.wechatbot.infrastructure.external.wxauto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageAttr {
    /**
     * 系统消息
     */
    SYS("system"),
    /**
     * 时间消息
     */
    TIME("time"),
    /**
     * 拍一拍消息
     */
    TICKLE("tickle"),
    /**
     * 自己发送的消息
     */
    SELF("self"),
    /**
     * 好友消息
     */
    FRIEND("friend"),
    /**
     * 其他消息
     */
    OTHER("other"); 

    private final String value;

    MessageAttr(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MessageAttr fromValue(String value) {
        for (MessageAttr t : MessageAttr.values()) {
            if (t.value.equalsIgnoreCase(value)) {
                return t;
            }
        }
        return OTHER;
    }
} 