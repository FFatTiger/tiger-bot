package com.fffattiger.wechatbot.domain.message;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageType;

@Table("messages")
public record Message(
    @Id
    Long id,

    /**
     * 聊天的id
     */
    Long chatId,
    /**
     * 消息类型
     */
    MessageType type,

    /**
     * 消息内容
     */
    String content,

    /**
     * 发送者
     */
    String sender,

    /**
     * 消息信息
     */
    List<String> info,

    /**
     * 消息时间
     */
    LocalDateTime time,

    /**
     * 发送者备注
     */
    String senderRemark
) {
    
}
