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
                              int maxTokens, int maxOutputTokens,
                              boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg, String params) {
        
        validateModelParameters(aiProviderId, modelName, description, maxTokens, maxOutputTokens);

        AiModel newModel = new AiModel(null, aiProviderId, modelName.trim(), description.trim(),
                                      maxTokens, maxOutputTokens, reasoningFlg, streamFlg, enabled, toolCallFlg, params);
        
        AiModel savedModel = aiModelRepository.save(newModel);
        
        return savedModel;
    }

    /**
     * 更新AI模型
     */
    public AiModel updateModel(Long id, Long aiProviderId, String modelName, String description,
                              int maxTokens, int maxOutputTokens,
                              boolean reasoningFlg, boolean streamFlg, boolean enabled, boolean toolCallFlg, String params) {
        
        AiModel existingModel = aiModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("AI模型不存在: " + id));

        validateModelParameters(aiProviderId, modelName, description, maxTokens, maxOutputTokens);

        AiModel updatedModel = new AiModel(id, aiProviderId, modelName.trim(), description.trim(),
                                          maxTokens, maxOutputTokens, reasoningFlg, streamFlg, enabled, toolCallFlg, params);
        
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
                                          existingModel.reasoningFlg(), existingModel.streamFlg(), 
                                          !existingModel.enabled(), existingModel.toolCallFlg(), existingModel.params());
        
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
                                       int maxTokens, int maxOutputTokens) {
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
    }
}
