package com.fffattiger.wechatbot.management.application.dto;

/**
 * AI角色配置DTO
 * 用于管理界面的数据传输
 */
public record AiRoleConfigurationDto(
    Long id,
    String name,
    String promptContent,
    String extraMemory,
    String promptType
) {
    
    /**
     * 验证配置是否有效
     */
    public boolean isValidConfiguration() {
        return name != null && !name.trim().isEmpty() &&
               promptContent != null && !promptContent.trim().isEmpty() &&
               promptType != null && !promptType.trim().isEmpty();
    }

    /**
     * 获取提示词类型的显示名称
     */
    public String getPromptTypeDisplayName() {
        if (promptType == null) {
            return "未知";
        }
        
        return switch (promptType.toLowerCase()) {
            case "system" -> "系统";
            case "user" -> "用户";
            case "assistant" -> "助手";
            case "function" -> "函数";
            default -> promptType;
        };
    }

    /**
     * 获取提示词类型的样式类
     */
    public String getPromptTypeBadgeClass() {
        if (promptType == null) {
            return "bg-secondary";
        }
        
        return switch (promptType.toLowerCase()) {
            case "system" -> "bg-primary";
            case "user" -> "bg-info";
            case "assistant" -> "bg-success";
            case "function" -> "bg-warning";
            default -> "bg-secondary";
        };
    }

    /**
     * 获取角色描述（从提示词内容中提取）
     */
    public String getRoleDescription() {
        if (promptContent == null || promptContent.isEmpty()) {
            return "无描述";
        }
        
        // 尝试从提示词中提取描述信息
        String content = promptContent.trim();
        
        // 查找描述相关的关键词
        String[] descriptionKeywords = {"description:", "描述:", "角色:", "Role:", "Profile"};
        for (String keyword : descriptionKeywords) {
            int index = content.indexOf(keyword);
            if (index != -1) {
                String afterKeyword = content.substring(index + keyword.length()).trim();
                int endIndex = Math.min(afterKeyword.indexOf('\n'), 100);
                if (endIndex == -1) {
                    endIndex = Math.min(afterKeyword.length(), 100);
                }
                if (endIndex > 0) {
                    return afterKeyword.substring(0, endIndex).trim();
                }
            }
        }
        
        // 如果没有找到特定描述，返回前100个字符
        if (content.length() <= 100) {
            return content;
        }
        
        return content.substring(0, 100) + "...";
    }

    /**
     * 获取简短的提示词内容预览
     */
    public String getPromptPreview() {
        if (promptContent == null || promptContent.isEmpty()) {
            return "无内容";
        }
        
        String content = promptContent.trim();
        if (content.length() <= 200) {
            return content;
        }
        
        return content.substring(0, 200) + "...";
    }

    /**
     * 获取额外记忆的简短预览
     */
    public String getExtraMemoryPreview() {
        if (extraMemory == null || extraMemory.trim().isEmpty()) {
            return "无额外记忆";
        }
        
        String memory = extraMemory.trim();
        if (memory.length() <= 100) {
            return memory;
        }
        
        return memory.substring(0, 100) + "...";
    }

    /**
     * 检查是否有额外记忆
     */
    public boolean hasExtraMemory() {
        return extraMemory != null && !extraMemory.trim().isEmpty();
    }

    /**
     * 获取提示词内容的行数
     */
    public int getPromptContentLines() {
        if (promptContent == null || promptContent.isEmpty()) {
            return 0;
        }
        
        return promptContent.split("\n").length;
    }

    /**
     * 获取提示词内容的字符数
     */
    public int getPromptContentLength() {
        if (promptContent == null) {
            return 0;
        }
        
        return promptContent.length();
    }

    /**
     * 获取角色复杂度标识
     */
    public String getComplexityBadge() {
        int length = getPromptContentLength();
        
        if (length < 500) {
            return "简单";
        } else if (length < 2000) {
            return "中等";
        } else {
            return "复杂";
        }
    }

    /**
     * 获取角色复杂度样式类
     */
    public String getComplexityBadgeClass() {
        int length = getPromptContentLength();
        
        if (length < 500) {
            return "bg-success";
        } else if (length < 2000) {
            return "bg-warning";
        } else {
            return "bg-danger";
        }
    }
}
