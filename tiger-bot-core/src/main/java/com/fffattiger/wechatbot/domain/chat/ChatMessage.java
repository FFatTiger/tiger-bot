package com.fffattiger.wechatbot.domain.chat;

import java.time.LocalDateTime;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageAttr;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

/**
 * 消息聚合根
 * 代表系统中的一条消息，包含消息的所有信息和行为
 * 根据新的wxauto文档API更新字段结构
 */
@Entity
@Table(name = "messages")
@Getter
public class ChatMessage extends com.fffattiger.wechatbot.domain.common.Entity {

    /**
     * 聊天群组ID
     */
    private Long chatId;

    /**
     * 消息内容类型（如text、image、video等）
     */
    @Enumerated(EnumType.STRING)
    private MessageType type;

    /**
     * 消息来源属性（如friend、self、system等）
     */
    @Enumerated(EnumType.STRING)
    private MessageAttr attr;

    /**
     * 消息内容
     */
    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * 发送者标识
     */
    private String sender;

    /**
     * 消息附加信息（JSON格式存储）
     */
    @Column(columnDefinition = "TEXT")
    private String info;

    /**
     * 消息hash值（可能重复，切换UI后不变）
     */
    private String hash;

    /**
     * 消息处理状态
     */
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.PENDING;

    /**
     * 是否为机器人发送的消息
     */
    private boolean botMessage = false;

    /**
     * 消息接收时间（由系统生成）
     */
    private LocalDateTime time;

    // 构造函数
    protected ChatMessage() {}

    public ChatMessage(Long chatId, String type, String attr, String content, String sender,
                  String info, String hash, LocalDateTime time) {
        this.chatId = chatId;
        this.type = MessageType.fromValue(type);
        this.attr = MessageAttr.fromValue(attr);
        this.content = content;
        this.sender = sender;
        this.info = info;
        this.hash = hash;
        this.time = time;
        this.botMessage = attr.equals(MessageAttr.FRIEND.getValue());
    }

    
}
