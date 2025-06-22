package com.fffattiger.wechatbot.domain.ai;


import org.springframework.util.Assert;

import com.fffattiger.wechatbot.domain.common.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@jakarta.persistence.Entity
@Table(name = "ai_models")
@Getter
public class AiModel extends Entity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ai_provider_id")
    private AiProvider provider;
    
    @Column(name = "model_name")
    private String modelName;

    private String description;

    private int maxTokens;

    private int maxOutputTokens;

    private boolean reasoningFlg;

    private boolean streamFlg;

    private boolean enabled;

    private boolean toolCallFlg;

    private String params;

    protected AiModel() {
        // For JPA
    }

    AiModel(AiProvider provider, String modelName, String description, int maxTokens, int maxOutputTokens, 
            boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg, String params) {
        Assert.notNull(provider, "Provider must not be null");
        Assert.hasText(modelName, "Model name must not be empty");
        this.provider = provider;
        this.modelName = modelName;
        this.description = description;
        this.maxTokens = maxTokens;
        this.maxOutputTokens = maxOutputTokens;
        this.reasoningFlg = reasoningFlg;
        this.streamFlg = streamFlg;
        this.enabled = enabled;
        this.toolCallFlg = toolCallFlg;
        this.params = params;
    }

    void updateDetails(String modelName, String description, int maxTokens, int maxOutputTokens,
                       boolean reasoningFlg, boolean streamFlg, boolean toolCallFlg, String params) {
        Assert.hasText(modelName, "Model name must not be empty");
        this.modelName = modelName;
        this.description = description;
        this.maxTokens = maxTokens;
        this.maxOutputTokens = maxOutputTokens;
        this.reasoningFlg = reasoningFlg;
        this.streamFlg = streamFlg;
        this.toolCallFlg = toolCallFlg;
        this.params = params;
    }

    void enable() {
        this.enabled = true;
    }

    void disable() {
        this.enabled = false;
    }
}
