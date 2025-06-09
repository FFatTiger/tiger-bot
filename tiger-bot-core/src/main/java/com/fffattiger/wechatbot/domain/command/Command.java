package com.fffattiger.wechatbot.domain.command;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.AntPathMatcher;

import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

@Table("commands")
public class Command {
    @Id
    private Long id;

    /**
     * 命令的正则或名称
     */
    private String pattern;

    /**
     * 命令描述
     */
    private String description;

    /**
     * AI配置
     */
    @Embedded.Empty
    private AiSpecification aiSpecification;

    // 构造函数
    public Command() {}

    public Command(Long id, String pattern, String description, AiSpecification aiSpecification) {
        this.id = id;
        this.pattern = pattern;
        this.description = description;
        this.aiSpecification = aiSpecification;
    }

    // 业务方法
    public boolean matches(String input) {
        if (pattern == null || input == null) {
            return false;
        }
        return new AntPathMatcher().match(pattern, input);
    }

    public boolean requiresAiConfiguration() {
        return aiSpecification != null &&
               aiSpecification.aiProviderId() != null &&
               aiSpecification.aiModelId() != null &&
               aiSpecification.aiRoleId() != null;
    }

    public boolean isValidCommand() {
        return pattern != null && !pattern.trim().isEmpty() &&
               description != null && !description.trim().isEmpty();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getPattern() {
        return pattern;
    }

    public String getDescription() {
        return description;
    }

    public AiSpecification getAiSpecification() {
        return aiSpecification;
    }

    // Setters (仅用于框架)
    public void setId(Long id) {
        this.id = id;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAiSpecification(AiSpecification aiSpecification) {
        this.aiSpecification = aiSpecification;
    }
}