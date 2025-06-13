package com.fffattiger.wechatbot.management.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.application.service.AiModelApplicationService;
import com.fffattiger.wechatbot.application.service.AiProviderApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.infrastructure.ai.ChatClientBuilderFactory;
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
    
    @Autowired
    private ObjectProvider<RestClient.Builder> restClientBuilderProvider;

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
            dto.reasoningFlg(),
            dto.streamFlg(),
            dto.enabled(),
            dto.toolCallFlg(),
            dto.params()
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
            dto.reasoningFlg(),
            dto.streamFlg(),
            dto.enabled(),
            dto.toolCallFlg(),
            dto.params()
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

        return new AiModelConfigurationDto(
            null,
            providerId,
            "",
            "",
            "",
            defaultMaxTokens,
            defaultMaxOutputTokens,
            false,
            true,
            true,
            false,
            ""
        );
    }

    /**
     * 测试AI模型
     */
    public boolean testModel(Long id) {
        AiModel model = coreAiModelApplicationService.getModelById(id)
                .orElseThrow(() -> new RuntimeException("AI模型不存在: " + id));

        log.info("开始测试AI模型: 模型ID={}, 模型名称={}", id, model.modelName());

        try {
            // 获取模型对应的提供商
            AiProvider provider = coreAiProviderApplicationService.getProviderById(model.aiProviderId())
                    .orElseThrow(() -> new RuntimeException("AI提供商不存在: " + model.aiProviderId()));

            // 获取一个简单的测试角色
            AiRole testRole = getSimpleTestRole();

            log.debug("模型测试配置: 提供商={}, 模型={}, 角色={}", 
                     provider.providerName(), model.modelName(), testRole.name());

            // 构建ChatClient进行测试
            Map<String, Object> params = new HashMap<>();
            ChatClient chatClient = ChatClientBuilderFactory.builder(provider, model, testRole, params, restClientBuilderProvider)
                    .build();

            // 发送简单的测试消息
            String testMessage = "你好";
            String response = chatClient.prompt()
                    .user(testMessage)
                    .call()
                    .content();

            log.info("模型测试响应: 输入={}, 输出={}", testMessage, response);

            // 检查响应是否有效
            boolean success = StringUtils.hasLength(response) && response.trim().length() > 0;
            
            if (success) {
                log.info("模型测试成功: 模型ID={}, 模型名称={}, 响应长度={}", 
                        id, model.modelName(), response.length());
            } else {
                log.warn("模型测试失败: 模型ID={}, 模型名称={}, 响应为空", 
                        id, model.modelName());
            }

            return success;

        } catch (Exception e) {
            log.error("模型测试出错: 模型ID={}, 模型名称={}, 错误信息={}", 
                     id, model.modelName(), e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取用于测试的简单角色
     */
    private AiRole getSimpleTestRole() {

        // 如果没有任何角色，创建一个临时的简单角色
        log.debug("没有可用角色，创建临时测试角色");
        return new AiRole(null, "临时测试助手", 
                         "你是一个友善的AI助手，会简短地回答用户的问题。", 
                         "", "system");
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
            model.reasoningFlg(),
            model.streamFlg(),
            model.enabled(),
            model.toolCallFlg(),
            model.params()
        );
    }
}
