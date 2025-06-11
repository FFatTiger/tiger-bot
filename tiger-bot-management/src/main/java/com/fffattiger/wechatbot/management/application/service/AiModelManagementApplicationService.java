package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.AiModelApplicationService;
import com.fffattiger.wechatbot.application.service.AiProviderApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.management.application.dto.AiModelConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * AI模型管理应用服务
 * 负责AI模型配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class AiModelManagementApplicationService {

    @Autowired
    private AiModelApplicationService coreAiModelApplicationService;

    @Autowired
    private AiProviderApplicationService coreAiProviderApplicationService;

    /**
     * 获取所有AI模型配置
     */
    public List<AiModelConfigurationDto> getAllModelConfigurations() {
        List<AiModel> models = coreAiModelApplicationService.getAllModels();
        return models.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据提供商ID获取AI模型配置
     */
    public List<AiModelConfigurationDto> getModelConfigurationsByProviderId(Long providerId) {
        List<AiModel> models = coreAiModelApplicationService.getModelsByProviderId(providerId);
        return models.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取AI模型配置
     */
    public AiModelConfigurationDto getModelConfigurationById(Long id) {
        AiModel model = coreAiModelApplicationService.getModelById(id)
                .orElseThrow(() -> new RuntimeException("AI模型不存在: " + id));
        return convertToDto(model);
    }

    /**
     * 获取所有可用的AI提供商
     */
    public List<AiProvider> getAllAvailableProviders() {
        return coreAiProviderApplicationService.getAllProviders();
    }

    /**
     * 创建AI模型配置
     */
    public void createModelConfiguration(AiModelConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("AI模型配置无效");
        }

        

        // 调用核心模块的应用服务
        coreAiModelApplicationService.createModel(
            dto.aiProviderId(),
            dto.modelName(),
            dto.description(),
            dto.maxTokens(),
            dto.maxOutputTokens(),
            dto.temperature(),
            dto.frequencyPenalty(),
            dto.presencePenalty(),
            dto.topK(),
            dto.topP(),
            dto.reasoningFlg(),
            dto.streamFlg(),
            dto.enabled(),
            dto.toolCallFlg()
        );
    }

    /**
     * 更新AI模型配置
     */
    public void updateModelConfiguration(Long id, AiModelConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("AI模型配置无效");
        }

        

        // 调用核心模块的应用服务
        coreAiModelApplicationService.updateModel(
            id,
            dto.aiProviderId(),
            dto.modelName(),
            dto.description(),
            dto.maxTokens(),
            dto.maxOutputTokens(),
            dto.temperature(),
            dto.frequencyPenalty(),
            dto.presencePenalty(),
            dto.topK(),
            dto.topP(),
            dto.reasoningFlg(),
            dto.streamFlg(),
            dto.enabled(),
            dto.toolCallFlg()
        );
    }

    /**
     * 删除AI模型配置
     */
    public void deleteModelConfiguration(Long id) {
        

        // 调用核心模块的应用服务
        coreAiModelApplicationService.deleteModel(id);
    }

    /**
     * 切换AI模型启用状态
     */
    public void toggleModelEnabled(Long id) {
        

        // 调用核心模块的应用服务
        coreAiModelApplicationService.toggleModelEnabled(id);
    }

    /**
     * 获取模型的默认配置
     */
    public AiModelConfigurationDto getDefaultModelConfiguration(Long providerId, String providerType) {
        // 根据提供商类型设置默认值
        int defaultMaxTokens = 4096;
        int defaultMaxOutputTokens = 4096;
        double defaultTemperature = 0.7;

        if ("deepseek".equalsIgnoreCase(providerType)) {
            defaultMaxTokens = 100000;
            defaultMaxOutputTokens = 100000;
            defaultTemperature = 0.5;
        } else if ("openai".equalsIgnoreCase(providerType)) {
            defaultMaxTokens = 128000;
            defaultMaxOutputTokens = 4096;
            defaultTemperature = 0.7;
        }

        return new AiModelConfigurationDto(
            null,
            providerId,
            "",
            "",
            "",
            defaultMaxTokens,
            defaultMaxOutputTokens,
            defaultTemperature,
            0.0,
            0.0,
            0.0,
            0.0,
            false,
            true,
            true,
            false
        );
    }

    /**
     * 测试AI模型
     */
    public boolean testModel(Long id) {
        AiModel model = coreAiModelApplicationService.getModelById(id)
                .orElseThrow(() -> new RuntimeException("AI模型不存在: " + id));

        

        // TODO: 实现实际的模型测试逻辑
        // 这里可以发送一个简单的测试请求来验证模型是否可用
        try {
            // 模拟模型测试
            Thread.sleep(2000);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 将领域对象转换为DTO
     */
    private AiModelConfigurationDto convertToDto(AiModel model) {
        // 获取提供商名称
        String providerName = coreAiProviderApplicationService.getProviderById(model.aiProviderId())
                .map(AiProvider::providerName)
                .orElse("未知提供商");

        return new AiModelConfigurationDto(
            model.id(),
            model.aiProviderId(),
            providerName,
            model.modelName(),
            model.description(),
            model.maxTokens(),
            model.maxOutputTokens(),
            model.temperature(),
            model.frequencyPenalty(),
            model.presencePenalty(),
            model.topK(),
            model.topP(),
            model.reasoningFlg(),
            model.streamFlg(),
            model.enabled(),
            model.toolCallFlg()
        );
    }
}
