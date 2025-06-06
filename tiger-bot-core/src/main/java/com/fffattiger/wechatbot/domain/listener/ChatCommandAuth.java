package com.fffattiger.wechatbot.domain.listener;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("chat_command_auths")
public record ChatCommandAuth(
    @Id
    Long id,

    /**
     * 聊天id
     */
    Long chatId,

    /**
     * 命令id
     */
    Long commandId,

    /**
     * 允许为null，表示所有人可用
     */
    Long userId
) {} 