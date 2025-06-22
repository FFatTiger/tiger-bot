package com.fffattiger.wechatbot.domain.ai;

import org.springframework.util.Assert;

import com.fffattiger.wechatbot.domain.common.AggregateRoot;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "ai_roles")
@Getter
public class AiRole extends AggregateRoot {

    private String name;

    private String promptContent;

    private String extraMemory;

    private String promptType;

    protected AiRole() {
        // For JPA
    }

    public AiRole(String name, String promptContent, String extraMemory, String promptType) {
        validate(name, promptContent);
        this.name = name.trim();
        this.promptContent = promptContent.trim();
        this.extraMemory = extraMemory;
        this.promptType = promptType;
    }

    public void updateRole(String name, String promptContent, String extraMemory, String promptType) {
        validate(name, promptContent);
        this.name = name.trim();
        this.promptContent = promptContent.trim();
        this.extraMemory = extraMemory;
        this.promptType = promptType;
    }

    public boolean isCompatibleWith(AiModel model) {
        // Example rule: prompt length cannot exceed half of the model's max tokens
        if (this.promptContent == null || model.getMaxTokens() <= 0) {
            return true; // Or handle as an error case
        }
        return this.promptContent.length() < (model.getMaxTokens() / 2);
    }

    private void validate(String name, String promptContent) {
        Assert.hasText(name, "Role name must not be empty");
        Assert.hasText(promptContent, "Prompt content must not be empty");
    }
}
