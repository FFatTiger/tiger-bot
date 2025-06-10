package com.fffattiger.wechatbot.management.application.dto;

/**
 * AI模型配置DTO
 * 用于管理界面的数据传输
 */
public record AiModelConfigurationDto(
    Long id,
    Long aiProviderId,
    String providerName,
    String modelName,
    String description,
    int maxTokens,
    int maxOutputTokens,
    Double temperature,
    Double frequencyPenalty,
    Double presencePenalty,
    Double topK,
    Double topP,
    boolean reasoningFlg,
    boolean streamFlg,
    boolean enabled,
    boolean toolCallFlg
) {
    
    /**
     * 验证配置是否有效
     */
    public boolean isValidConfiguration() {
        return aiProviderId != null &&
               modelName != null && !modelName.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               maxTokens > 0 &&
               maxOutputTokens > 0 &&
               isValidTemperature() &&
               isValidPenalties() &&
               isValidTopValues();
    }

    /**
     * 验证温度值
     */
    private boolean isValidTemperature() {
        return temperature == null || (temperature >= 0.0 && temperature <= 2.0);
    }

    /**
     * 验证惩罚值
     */
    private boolean isValidPenalties() {
        boolean freqValid = frequencyPenalty == null || 
                           (frequencyPenalty >= -2.0 && frequencyPenalty <= 2.0);
        boolean presValid = presencePenalty == null || 
                           (presencePenalty >= -2.0 && presencePenalty <= 2.0);
        return freqValid && presValid;
    }

    /**
     * 验证Top值
     */
    private boolean isValidTopValues() {
        boolean topKValid = topK == null || topK >= 0.0;
        boolean topPValid = topP == null || (topP >= 0.0 && topP <= 1.0);
        return topKValid && topPValid;
    }

    /**
     * 获取状态标识
     */
    public String getStatusBadge() {
        return enabled ? "启用" : "禁用";
    }

    /**
     * 获取状态样式类
     */
    public String getStatusBadgeClass() {
        return enabled ? "bg-success" : "bg-secondary";
    }

    /**
     * 获取功能标识列表
     */
    public String getFeatureBadges() {
        StringBuilder badges = new StringBuilder();
        
        if (reasoningFlg) {
            badges.append("<span class=\"badge bg-info me-1\">推理</span>");
        }
        if (streamFlg) {
            badges.append("<span class=\"badge bg-primary me-1\">流式</span>");
        }
        if (toolCallFlg) {
            badges.append("<span class=\"badge bg-warning me-1\">工具调用</span>");
        }
        
        return badges.toString();
    }

    /**
     * 获取模型参数摘要
     */
    public String getParameterSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (temperature != null) {
            summary.append("温度: ").append(temperature).append(" ");
        }
        if (frequencyPenalty != null && frequencyPenalty != 0.0) {
            summary.append("频率惩罚: ").append(frequencyPenalty).append(" ");
        }
        if (presencePenalty != null && presencePenalty != 0.0) {
            summary.append("存在惩罚: ").append(presencePenalty).append(" ");
        }
        if (topK != null && topK != 0.0) {
            summary.append("Top-K: ").append(topK).append(" ");
        }
        if (topP != null && topP != 0.0) {
            summary.append("Top-P: ").append(topP).append(" ");
        }
        
        return summary.toString().trim();
    }

    /**
     * 获取令牌限制摘要
     */
    public String getTokenSummary() {
        return String.format("输入: %,d / 输出: %,d", maxTokens, maxOutputTokens);
    }

    /**
     * 获取简短描述
     */
    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "无描述";
        }
        
        if (description.length() <= 50) {
            return description;
        }
        
        return description.substring(0, 50) + "...";
    }
}
