package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.AiProviderApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.management.application.dto.AiProviderConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * AI提供商管理应用服务
 * 负责AI提供商配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class AiProviderManagementApplicationService {

    @Autowired
    private AiProviderApplicationService coreAiProviderApplicationService;

    /**
     * 获取所有AI提供商配置
     */
    public List<AiProviderConfigurationDto> getAllProviderConfigurations() {
        List<AiProvider> providers = coreAiProviderApplicationService.getAllProviders();
        return providers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取AI提供商配置
     */
    public AiProviderConfigurationDto getProviderConfigurationById(Long id) {
        AiProvider provider = coreAiProviderApplicationService.getProviderById(id)
                .orElseThrow(() -> new RuntimeException("AI提供商不存在: " + id));
        return convertToDto(provider);
    }

    /**
     * 创建AI提供商配置
     */
    public void createProviderConfiguration(AiProviderConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("AI提供商配置无效");
        }

        

        // 调用核心模块的应用服务
        coreAiProviderApplicationService.createProvider(
            dto.providerType(),
            dto.providerName(),
            dto.apiKey(),
            dto.baseUrl()
        );
    }

    /**
     * 更新AI提供商配置
     */
    public void updateProviderConfiguration(Long id, AiProviderConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("AI提供商配置无效");
        }

        

        // 调用核心模块的应用服务
        coreAiProviderApplicationService.updateProvider(
            id,
            dto.providerType(),
            dto.providerName(),
            dto.apiKey(),
            dto.baseUrl()
        );
    }

    /**
     * 删除AI提供商配置
     */
    public void deleteProviderConfiguration(Long id) {
        

        // 调用核心模块的应用服务
        coreAiProviderApplicationService.deleteProvider(id);
    }



    /**
     * 获取支持的提供商类型列表
     */
    public List<String> getSupportedProviderTypes() {
        return List.of(
            "openai",
            "deepseek", 
            "anthropic",
            "azure-openai",
            "ollama",
            "zhipuai",
            "minimax",
            "mistral"
        );
    }

    /**
     * 获取提供商类型的默认配置
     */
    public AiProviderConfigurationDto getDefaultProviderConfiguration(String providerType) {
        String defaultBaseUrl = switch (providerType.toLowerCase()) {
            case "openai" -> "https://api.openai.com/v1";
            case "deepseek" -> "https://api.deepseek.com/v1";
            case "anthropic" -> "https://api.anthropic.com";
            case "azure-openai" -> "https://your-resource.openai.azure.com";
            case "ollama" -> "http://localhost:11434";
            case "zhipuai" -> "https://open.bigmodel.cn/api/paas/v4";
            case "minimax" -> "https://api.minimax.chat/v1";
            case "mistral" -> "https://api.mistral.ai/v1";
            default -> "";
        };

        String defaultName = switch (providerType.toLowerCase()) {
            case "openai" -> "OpenAI";
            case "deepseek" -> "DeepSeek";
            case "anthropic" -> "Anthropic";
            case "azure-openai" -> "Azure OpenAI";
            case "ollama" -> "Ollama";
            case "zhipuai" -> "智谱AI";
            case "minimax" -> "MiniMax";
            case "mistral" -> "Mistral";
            default -> providerType.toUpperCase();
        };

        return new AiProviderConfigurationDto(
            null,
            providerType,
            defaultName,
            "",
            defaultBaseUrl
        );
    }

    /**
     * 将领域对象转换为DTO
     */
    private AiProviderConfigurationDto convertToDto(AiProvider provider) {
        return new AiProviderConfigurationDto(
            provider.id(),
            provider.providerType(),
            provider.providerName(),
            provider.apiKey(),
            provider.baseUrl()
        );
    }
}
