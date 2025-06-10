package com.fffattiger.wechatbot.management.application.dto;

/**
 * AI提供商配置DTO
 * 用于管理界面的数据传输
 */
public record AiProviderConfigurationDto(
    Long id,
    String providerType,
    String providerName,
    String apiKey,
    String baseUrl
) {
    
    /**
     * 验证配置是否有效
     */
    public boolean isValidConfiguration() {
        return providerType != null && !providerType.trim().isEmpty() &&
               providerName != null && !providerName.trim().isEmpty() &&
               apiKey != null && !apiKey.trim().isEmpty() &&
               baseUrl != null && !baseUrl.trim().isEmpty();
    }

    /**
     * 获取脱敏的API密钥（用于显示）
     */
    public String getMaskedApiKey() {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    /**
     * 获取提供商类型的显示名称
     */
    public String getProviderTypeDisplayName() {
        if (providerType == null) {
            return "未知";
        }
        
        return switch (providerType.toLowerCase()) {
            case "openai" -> "OpenAI";
            case "deepseek" -> "DeepSeek";
            case "anthropic" -> "Anthropic";
            case "azure-openai" -> "Azure OpenAI";
            case "ollama" -> "Ollama";
            case "zhipuai" -> "智谱AI";
            case "minimax" -> "MiniMax";
            case "mistral" -> "Mistral";
            default -> providerType.toUpperCase();
        };
    }

    /**
     * 检查是否为本地部署的提供商
     */
    public boolean isLocalProvider() {
        if (providerType == null) {
            return false;
        }
        return "ollama".equalsIgnoreCase(providerType) || 
               (baseUrl != null && (baseUrl.contains("localhost") || baseUrl.contains("127.0.0.1")));
    }

    /**
     * 获取状态标识
     */
    public String getStatusBadge() {
        if (isLocalProvider()) {
            return "本地";
        }
        return "云端";
    }
}
