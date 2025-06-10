package com.fffattiger.wechatbot.management.application.dto;

import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

/**
 * 聊天配置DTO
 * 用于管理界面的聊天对象配置数据传输
 */
public record ChatConfigurationDto(
    Long id,
    String name,
    boolean groupFlag,
    Long aiProviderId,
    Long aiModelId,
    Long aiRoleId,
    String aiProviderName,
    String aiModelName,
    String aiRoleName,
    boolean hasAiConfiguration,
    long messageCount
) {

    /**
     * 验证配置是否有效
     */
    public boolean isValidConfiguration() {
        return name != null && !name.trim().isEmpty();
    }

    /**
     * 验证AI配置是否有效
     */
    public boolean isValidAiConfiguration() {
        return aiProviderId != null && aiModelId != null && aiRoleId != null;
    }

    /**
     * 获取聊天类型显示文本
     */
    public String getChatTypeDisplay() {
        return groupFlag ? "群聊" : "私聊";
    }

    /**
     * 获取AI配置显示文本
     */
    public String getAiConfigurationDisplay() {
        if (!hasAiConfiguration) {
            return "未配置";
        }
        return String.format("%s / %s / %s", 
            aiProviderName != null ? aiProviderName : "未知提供商",
            aiModelName != null ? aiModelName : "未知模型", 
            aiRoleName != null ? aiRoleName : "未知角色");
    }

    /**
     * 转换为AiSpecification值对象
     */
    public AiSpecification toAiSpecification() {
        if (!isValidAiConfiguration()) {
            return null;
        }
        return new AiSpecification(aiProviderId, aiModelId, aiRoleId);
    }

    /**
     * 创建用于新增的DTO
     */
    public static ChatConfigurationDto forCreate(String name, boolean groupFlag, 
                                                Long aiProviderId, Long aiModelId, Long aiRoleId) {
        return new ChatConfigurationDto(null, name, groupFlag, aiProviderId, aiModelId, aiRoleId,
                                      null, null, null, false, 0);
    }

    /**
     * 创建用于更新AI配置的DTO
     */
    public static ChatConfigurationDto forUpdateAi(Long id, Long aiProviderId, Long aiModelId, Long aiRoleId) {
        return new ChatConfigurationDto(id, null, false, aiProviderId, aiModelId, aiRoleId,
                                      null, null, null, false, 0);
    }
}
