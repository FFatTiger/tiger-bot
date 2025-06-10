package com.fffattiger.wechatbot.management.application.dto;

/**
 * 命令配置DTO
 */
public record CommandConfigurationDto(
    Long id,
    String pattern,
    String description,
    Long aiProviderId,
    String aiProviderName,
    Long aiModelId,
    String aiModelName,
    Long aiRoleId,
    String aiRoleName,
    boolean hasAiConfiguration
) {
    
    /**
     * 创建新命令配置的构造方法
     */
    public static CommandConfigurationDto forCreation(
            String pattern,
            String description,
            Long aiProviderId,
            Long aiModelId,
            Long aiRoleId) {
        return new CommandConfigurationDto(
            null, pattern, description, aiProviderId, null,
            aiModelId, null, aiRoleId, null, false
        );
    }
    
    /**
     * 检查配置是否有效
     */
    public boolean isValidConfiguration() {
        return pattern != null && !pattern.trim().isEmpty() &&
               description != null && !description.trim().isEmpty();
    }
    
    /**
     * 检查是否有完整的AI配置
     */
    public boolean hasCompleteAiConfiguration() {
        return aiProviderId != null && aiModelId != null && aiRoleId != null;
    }
}
