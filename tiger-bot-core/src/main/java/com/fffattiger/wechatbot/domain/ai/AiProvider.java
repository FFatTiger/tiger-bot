package com.fffattiger.wechatbot.domain.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.util.Assert;

import com.fffattiger.wechatbot.domain.common.AggregateRoot;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "ai_providers")
@Getter
public class AiProvider extends AggregateRoot{

    private String providerType;

    private String providerName;

    private String apiKey;

    private String baseUrl;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AiModel> models = new ArrayList<>();

    protected AiProvider() {
        // For JPA
    }

    public AiProvider(String providerType, String providerName, String apiKey, String baseUrl) {
        Assert.hasText(providerType, "Provider type must not be empty");
        Assert.hasText(providerName, "Provider name must not be empty");
        this.providerType = providerType;
        this.providerName = providerName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public void updateDetails(String providerName, String apiKey, String baseUrl) {
        Assert.hasText(providerName, "Provider name must not be empty");
        this.providerName = providerName;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public AiModel addModel(String modelName, String description, int maxTokens, int maxOutputTokens, 
                            boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg, String params) {
        if (this.models.stream().anyMatch(m -> m.getModelName().equals(modelName))) {
            throw new IllegalArgumentException("Model with name '" + modelName + "' already exists for this provider.");
        }
        AiModel newModel = new AiModel(this, modelName, description, maxTokens, maxOutputTokens, reasoningFlg, streamFlg, enabled, toolCallFlg, params);
        this.models.add(newModel);
        return newModel;
    }

    public void removeModel(Long modelId) {
        this.models.removeIf(model -> model.getId().equals(modelId));
    }

    public Optional<AiModel> findModelById(Long modelId) {
        return this.models.stream().filter(m -> Objects.equals(m.getId(), modelId)).findFirst();
    }

    public void updateModel(Long modelId, String modelName, String description, int maxTokens, int maxOutputTokens,
                            boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg, String params) {
        AiModel modelToUpdate = findModelById(modelId)
            .orElseThrow(() -> new IllegalArgumentException("Model with id " + modelId + " not found."));
        
        // Check for name uniqueness if the name is being changed
        if (!modelToUpdate.getModelName().equals(modelName) && this.models.stream().anyMatch(m -> m.getModelName().equals(modelName))) {
             throw new IllegalArgumentException("Model with name '" + modelName + "' already exists for this provider.");
        }

        modelToUpdate.updateDetails(modelName, description, maxTokens, maxOutputTokens, reasoningFlg, streamFlg, toolCallFlg, params);
        if (enabled) {
            modelToUpdate.enable();
        } else {
            modelToUpdate.disable();
        }
    }
}
