package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.AiModelApplicationService;
import com.fffattiger.wechatbot.application.service.AiProviderApplicationService;
import com.fffattiger.wechatbot.application.service.AiRoleApplicationService;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.application.service.MessageApplicationService;
import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;
import com.fffattiger.wechatbot.management.application.dto.ChatConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 聊天管理应用服务
 * 负责聊天对象配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class ChatManagementApplicationService {

    @Autowired
    private ChatApplicationService coreChatApplicationService;

    @Autowired
    private MessageApplicationService coreMessageApplicationService;

    @Autowired
    private AiProviderApplicationService coreAiProviderApplicationService;

    @Autowired
    private AiModelApplicationService coreAiModelApplicationService;

    @Autowired
    private AiRoleApplicationService coreAiRoleApplicationService;

    /**
     * 获取所有聊天配置
     */
    public List<ChatConfigurationDto> getAllChatConfigurations() {
        List<Chat> chats = coreChatApplicationService.getAllChats();
        return chats.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取聊天配置
     */
    public ChatConfigurationDto getChatConfigurationById(Long id) {
        Chat chat = coreChatApplicationService.getChatById(id);
        return convertToDto(chat);
    }

    /**
     * 获取所有可用的AI提供商
     */
    public List<AiProvider> getAllAvailableProviders() {
        return coreAiProviderApplicationService.getAllProviders();
    }

    /**
     * 获取所有可用的AI模型
     */
    public List<AiModel> getAllAvailableModels() {
        return coreAiModelApplicationService.getAllModels();
    }

    /**
     * 根据提供商ID获取AI模型
     */
    public List<AiModel> getModelsByProviderId(Long providerId) {
        return coreAiModelApplicationService.getModelsByProviderId(providerId);
    }

    /**
     * 获取所有可用的AI角色
     */
    public List<AiRole> getAllAvailableRoles() {
        return coreAiRoleApplicationService.getAllRoles();
    }

    /**
     * 创建聊天配置
     */
    public void createChatConfiguration(ChatConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("聊天配置无效");
        }

        log.info("创建聊天配置: {}", dto);

        AiSpecification aiSpecification = null;
        if (dto.isValidAiConfiguration()) {
            aiSpecification = dto.toAiSpecification();
        }

        // 调用核心模块的应用服务
        coreChatApplicationService.createChat(
            dto.name().trim(),
            dto.groupFlag(),
            aiSpecification
        );
    }

    /**
     * 更新聊天的AI配置
     */
    public void updateChatAiConfiguration(Long chatId, ChatConfigurationDto dto) {
        if (!dto.isValidAiConfiguration()) {
            throw new IllegalArgumentException("AI配置无效");
        }

        log.info("更新聊天AI配置: {} -> {}", chatId, dto);

        AiSpecification newSpecification = dto.toAiSpecification();
        coreChatApplicationService.updateAiConfiguration(chatId, newSpecification);
    }

    /**
     * 删除聊天配置
     */
    public void deleteChatConfiguration(Long chatId) {
        log.info("删除聊天配置: {}", chatId);

        // 先删除相关的消息记录
        coreMessageApplicationService.deleteMessagesByChatId(chatId);
        
        // 再删除聊天对象
        coreChatApplicationService.deleteChat(chatId);
    }

    /**
     * 批量配置AI设置
     */
    public void batchUpdateAiConfiguration(List<Long> chatIds, ChatConfigurationDto dto) {
        if (!dto.isValidAiConfiguration()) {
            throw new IllegalArgumentException("AI配置无效");
        }

        log.info("批量更新聊天AI配置: {} -> {}", chatIds, dto);

        AiSpecification newSpecification = dto.toAiSpecification();
        for (Long chatId : chatIds) {
            try {
                coreChatApplicationService.updateAiConfiguration(chatId, newSpecification);
            } catch (Exception e) {
                log.error("批量更新聊天{}的AI配置失败", chatId, e);
            }
        }
    }

    /**
     * 转换为DTO
     */
    private ChatConfigurationDto convertToDto(Chat chat) {
        String aiProviderName = null;
        String aiModelName = null;
        String aiRoleName = null;
        Long aiProviderId = null;
        Long aiModelId = null;
        Long aiRoleId = null;

        if (chat.hasAiConfiguration()) {
            AiSpecification aiSpec = chat.getAiSpecification();
            aiProviderId = aiSpec.aiProviderId();
            aiModelId = aiSpec.aiModelId();
            aiRoleId = aiSpec.aiRoleId();

            // 获取AI配置的显示名称
            try {
                aiProviderName = coreAiProviderApplicationService.getProviderById(aiProviderId)
                        .map(AiProvider::providerName).orElse("未知提供商");
                aiModelName = coreAiModelApplicationService.getModelById(aiModelId)
                        .map(AiModel::modelName).orElse("未知模型");
                aiRoleName = coreAiRoleApplicationService.getRoleById(aiRoleId)
                        .map(AiRole::name).orElse("未知角色");
            } catch (Exception e) {
                log.warn("获取AI配置显示名称失败", e);
            }
        }

        // 获取消息数量
        long messageCount = coreMessageApplicationService.countMessagesByChatId(chat.getId());

        return new ChatConfigurationDto(
            chat.getId(),
            chat.getName(),
            chat.isGroupFlag(),
            aiProviderId,
            aiModelId,
            aiRoleId,
            aiProviderName,
            aiModelName,
            aiRoleName,
            chat.hasAiConfiguration(),
            messageCount
        );
    }
}
