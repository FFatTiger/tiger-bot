package com.fffattiger.wechatbot.management.application.dto;

/**
 * 命令权限配置DTO
 */
public record CommandAuthConfigurationDto(
    Long id,
    Long chatId,
    String chatName,
    Long commandId,
    String commandName,
    Long userId,
    String username
) {
    
    /**
     * 创建新命令权限配置的构造方法
     */
    public static CommandAuthConfigurationDto forCreation(
            Long chatId,
            Long commandId,
            Long userId) {
        return new CommandAuthConfigurationDto(
            null, chatId, null, commandId, null, userId, null
        );
    }
    
    /**
     * 检查配置是否有效
     */
    public boolean isValidConfiguration() {
        return chatId != null && chatId > 0 && commandId != null && commandId > 0;
    }
    
    /**
     * 是否为全局权限（所有用户可用）
     */
    public boolean isGlobalPermission() {
        return userId == null;
    }
}
