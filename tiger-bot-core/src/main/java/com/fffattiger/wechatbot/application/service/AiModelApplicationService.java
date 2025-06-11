package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.repository.AiModelRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * AI模型应用服务
 * 负责AI模型的业务逻辑处理
 */
@Service
@Slf4j
public class AiModelApplicationService {

    @Resource
    private AiModelRepository aiModelRepository;

    /**
     * 获取所有AI模型
     */
    public List<AiModel> getAllModels() {
        List<AiModel> models = new ArrayList<>();
        aiModelRepository.findAll().forEach(models::add);
        log.debug("获取所有AI模型，共{}个", models.size());
        return models;
    }

    /**
     * 根据ID获取AI模型
     */
    public Optional<AiModel> getModelById(Long id) {
        return aiModelRepository.findById(id);
    }

    /**
     * 根据提供商ID获取AI模型列表
     */
    public List<AiModel> getModelsByProviderId(Long providerId) {
        return getAllModels().stream()
                .filter(model -> model.aiProviderId().equals(providerId))
                .toList();
    }

    /**
     * 创建AI模型
     */
    public AiModel createModel(Long aiProviderId, String modelName, String description, 
                              int maxTokens, int maxOutputTokens, Double temperature,
                              Double frequencyPenalty, Double presencePenalty, Double topK, Double topP,
                              boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg) {
        
        validateModelParameters(aiProviderId, modelName, description, maxTokens, maxOutputTokens, 
                               temperature, frequencyPenalty, presencePenalty, topK, topP);

        AiModel newModel = new AiModel(null, aiProviderId, modelName.trim(), description.trim(),
                                      maxTokens, maxOutputTokens, temperature, frequencyPenalty, 
                                      presencePenalty, topK, topP, reasoningFlg, streamFlg, enabled, toolCallFlg);
        
        AiModel savedModel = aiModelRepository.save(newModel);
        
        return savedModel;
    }

    /**
     * 更新AI模型
     */
    public AiModel updateModel(Long id, Long aiProviderId, String modelName, String description,
                              int maxTokens, int maxOutputTokens, Double temperature,
                              Double frequencyPenalty, Double presencePenalty, Double topK, Double topP,
                              boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg) {
        
        AiModel existingModel = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI模型不存在: " + id));

        validateModelParameters(aiProviderId, modelName, description, maxTokens, maxOutputTokens,
                               temperature, frequencyPenalty, presencePenalty, topK, topP);

        AiModel updatedModel = new AiModel(id, aiProviderId, modelName.trim(), description.trim(),
                                          maxTokens, maxOutputTokens, temperature, frequencyPenalty,
                                          presencePenalty, topK, topP, reasoningFlg, streamFlg, enabled, toolCallFlg);
        
        AiModel savedModel = aiModelRepository.save(updatedModel);
        
        return savedModel;
    }

    /**
     * 删除AI模型
     */
    public void deleteModel(Long id) {
        if (!aiModelRepository.existsById(id)) {
            throw new RuntimeException("AI模型不存在: " + id);
        }
        
        aiModelRepository.deleteById(id);
        
    }

    /**
     * 启用/禁用AI模型
     */
    public AiModel toggleModelEnabled(Long id) {
        AiModel existingModel = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI模型不存在: " + id));

        AiModel updatedModel = new AiModel(existingModel.id(), existingModel.aiProviderId(),
                                          existingModel.modelName(), existingModel.description(),
                                          existingModel.maxTokens(), existingModel.maxOutputTokens(),
                                          existingModel.temperature(), existingModel.frequencyPenalty(),
                                          existingModel.presencePenalty(), existingModel.topK(), existingModel.topP(),
                                          existingModel.reasoningFlg(), existingModel.streamFlg(), 
                                          !existingModel.enabled(), existingModel.toolCallFlg());
        
        AiModel savedModel = aiModelRepository.save(updatedModel);
        log.info("切换AI模型启用状态: {} -> {}", id, savedModel.enabled());
        return savedModel;
    }

    /**
     * 检查模型是否存在
     */
    public boolean existsById(Long id) {
        return aiModelRepository.existsById(id);
    }

    /**
     * 验证模型参数
     */
    private void validateModelParameters(Long aiProviderId, String modelName, String description,
                                       int maxTokens, int maxOutputTokens, Double temperature,
                                       Double frequencyPenalty, Double presencePenalty, Double topK, Double topP) {
        if (aiProviderId == null) {
            throw new IllegalArgumentException("AI提供商ID不能为空");
        }
        if (modelName == null || modelName.trim().isEmpty()) {
            throw new IllegalArgumentException("模型名称不能为空");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("模型描述不能为空");
        }
        if (maxTokens <= 0) {
            throw new IllegalArgumentException("最大令牌数必须大于0");
        }
        if (maxOutputTokens <= 0) {
            throw new IllegalArgumentException("最大输出令牌数必须大于0");
        }
        if (temperature != null && (temperature < 0.0 || temperature > 2.0)) {
            throw new IllegalArgumentException("温度值必须在0.0-2.0之间");
        }
        if (frequencyPenalty != null && (frequencyPenalty < -2.0 || frequencyPenalty > 2.0)) {
            throw new IllegalArgumentException("频率惩罚值必须在-2.0-2.0之间");
        }
        if (presencePenalty != null && (presencePenalty < -2.0 || presencePenalty > 2.0)) {
            throw new IllegalArgumentException("存在惩罚值必须在-2.0-2.0之间");
        }
        if (topK != null && topK < 0.0) {
            throw new IllegalArgumentException("Top-K值必须大于等于0");
        }
        if (topP != null && (topP < 0.0 || topP > 1.0)) {
            throw new IllegalArgumentException("Top-P值必须在0.0-1.0之间");
        }
    }
}
