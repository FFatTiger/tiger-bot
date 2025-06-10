package com.fffattiger.wechatbot.management.application.dto;

/**
 * 用户配置DTO
 */
public record UserConfigurationDto(
    Long id,
    String username,
    String remark
) {
    
    /**
     * 创建新用户配置的构造方法
     */
    public static UserConfigurationDto forCreation(
            String username,
            String remark) {
        return new UserConfigurationDto(null, username, remark);
    }
    
    /**
     * 检查配置是否有效
     */
    public boolean isValidConfiguration() {
        return username != null && !username.trim().isEmpty();
    }
    
    /**
     * 获取显示名称
     */
    public String getDisplayName() {
        if (remark != null && !remark.trim().isEmpty()) {
            return remark;
        }
        return username;
    }
}
