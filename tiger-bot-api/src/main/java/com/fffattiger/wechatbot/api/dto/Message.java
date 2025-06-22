package com.fffattiger.wechatbot.api.dto;

public interface Message {

    /**
     * 消息唯一ID
     * @return
     */
    Long getId();

    /**
     * 聊天ID
     * @return
     */
    Long getChatId();

    /**
     * 聊天名称
     * @return
     */
    String getChatName();

    /**
     * 消息内容类型（如text、image、video等）
     * @return
     */
    String getType();

    /**
     * 消息来源属性
     * friend: 好友/群友消息
     * self: 自己发送的消息
     * system: 系统消息
     * time: 时间消息
     * tickle: 拍一拍消息
     * other: 其他消息
     * @return
     */
    String getAttr();

    /**
     * 消息hash值（可能重复，切换UI后不变）
     * @return
     */
    String getHash();

    /**
     * 原始消息内容
     * @return
     */
    String getRawContent();

    /**
     * 清理后的消息内容
     * @return
     */
    String getCleanContent();

    /**
     * 发送者ID
     * @return
     */
    String getSenderId();

    /**
     * 发送者名称
     * @return
     */
    String getSenderName();

    /**
     * 消息时间戳
     * @return
     */
    Long getTimestamp();
}