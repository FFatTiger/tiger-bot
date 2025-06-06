package com.fffattiger.wechatbot.domain.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

@Table("chats")
public record Chat(
    @Id
    Long id,

    /**
     * 聊天的名称，群名或者私聊名
     */
    String name,

    /**
     * 是否为群聊
     */
    boolean groupFlag,

    /**
     * AI配置
     */
    @Embedded.Empty
    AiSpecification aiSpecification
) {
}
