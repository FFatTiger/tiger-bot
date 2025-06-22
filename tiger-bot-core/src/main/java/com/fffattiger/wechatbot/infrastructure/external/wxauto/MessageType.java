package com.fffattiger.wechatbot.infrastructure.external.wxauto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageType {
    /**
     * 文本消息
     */
    TEXT("text"),
    /**
     * 引用消息
     */ 
    QUOTE("quote"),
    /**
     * 语音消息
     */
    VOICE("voice"),
    /**
     * 图片消息
     */
    IMAGE("image"),
    /**
     * 视频消息
     */
    VIDEO("video"),
    /**
     * 文件消息
     */
    FILE("file"),
    /**
     * 链接消息
     */
    LINK("link"),
    /**
     * 表情消息
     */
    EMOTION("emotion"),
    /**
     * 合并转发消息
     */
    MERGE("merge"),
    /**
     * 个人名片消息
     */
    PERSONAL_CARD("personal_card"),
    /**
     * 笔记消息
     */
    NOTE("note"),
    /**
     * 其他消息
     */
    OTHER("other");

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
        return OTHER;
    }
} 