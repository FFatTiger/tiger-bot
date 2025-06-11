package com.fffattiger.wechatbot.management.application.dto;

import java.util.ArrayList;
import java.util.List;

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
    boolean reasoningFlg,
    boolean streamFlg,
    boolean enabled,
    boolean toolCallFlg,
    String params
) {
    
    /**
     * 验证配置是否有效
     */
    public boolean isValidConfiguration() {
        return aiProviderId != null &&
               modelName != null && !modelName.trim().isEmpty() &&
               description != null && !description.trim().isEmpty() &&
               maxTokens > 0 &&
               maxOutputTokens > 0;
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
        return description.length() > 50 ? description.substring(0, 47) + "..." : description;
    }

    /**
     * 获取参数摘要 - 解析新的参数格式
     */
    public String getParameterSummary() {
        if (params == null || params.trim().isEmpty()) {
            return "无参数";
        }
        
        List<String> paramSummaries = new ArrayList<>();
        
        try {
            // 解析 key:value; 格式的参数
            String[] pairs = params.split(";");
            for (String pair : pairs) {
                if (pair.trim().isEmpty()) {
                    continue;
                }
                
                String[] keyValue = pair.split(":", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    
                    // 只显示前几个参数，避免过长
                    if (paramSummaries.size() < 3) {
                        paramSummaries.add(key + ": " + value);
                    }
                }
            }
            
            if (paramSummaries.isEmpty()) {
                return "无有效参数";
            }
            
            String result = String.join(", ", paramSummaries);
            
            // 如果还有更多参数，显示省略号
            if (pairs.length > paramSummaries.size()) {
                result += "...";
            }
            
            return result;
            
        } catch (Exception e) {
            return "参数格式错误";
        }
    }
}
