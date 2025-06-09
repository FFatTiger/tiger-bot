package com.fffattiger.wechatbot.domain.listener;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("chat_command_auths")
public class ChatCommandAuth {
    @Id
    private Long id;

    /**
     * 聊天id
     */
    private Long chatId;

    /**
     * 命令id
     */
    private Long commandId;

    /**
     * 允许为null，表示所有人可用
     */
    private Long userId;

    // 构造函数
    public ChatCommandAuth() {}

    public ChatCommandAuth(Long id, Long chatId, Long commandId, Long userId) {
        this.id = id;
        this.chatId = chatId;
        this.commandId = commandId;
        this.userId = userId;
    }

    // 业务方法
    public boolean isGlobalPermission() {
        return userId == null;
    }

    public boolean allowsUser(Long targetUserId) {
        if (isGlobalPermission()) {
            return true;
        }
        return userId != null && userId.equals(targetUserId);
    }

    public boolean isValidAuth() {
        return chatId != null && commandId != null;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public Long getCommandId() {
        return commandId;
    }

    public Long getUserId() {
        return userId;
    }

    // Setters (仅用于框架)
    public void setId(Long id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setCommandId(Long commandId) {
        this.commandId = commandId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}