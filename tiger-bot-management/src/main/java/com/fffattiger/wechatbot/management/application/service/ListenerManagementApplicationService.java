package com.fffattiger.wechatbot.management.application.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.application.service.ListenerApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.management.application.dto.ListenerConfigurationDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 监听器管理应用服务
 * 负责监听器配置的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class ListenerManagementApplicationService {

    @Autowired
    private ListenerApplicationService coreListenerApplicationService;

    @Autowired
    private ChatApplicationService coreChatApplicationService;

    /**
     * 获取所有监听器配置
     */
    public List<ListenerConfigurationDto> getAllListenerConfigurations() {
        List<MessageProcessingData> listeners = coreListenerApplicationService.findAll();
        return listeners.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有可用的聊天对象
     */
    public List<Chat> getAllAvailableChats() {
        return coreChatApplicationService.getAllChats();
    }

    /**
     * 创建监听器配置
     */
    public void createListenerConfiguration(ListenerConfigurationDto dto) {
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("监听器配置无效");
        }

        log.info("创建监听器配置: {}", dto);

        // 调用核心模块的应用服务，需要提供creatorId，这里使用默认值1
        coreListenerApplicationService.createListener(
            dto.chatId(),
            1L, // 默认创建者ID，实际应用中应该从当前用户上下文获取
            dto.atReplyEnable(),
            dto.keywordReplyEnable(),
            dto.savePic(),
            dto.saveVoice(),
            dto.parseLinks(),
            dto.keywordReply()
        );
    }

    /**
     * 更新监听器配置
     */
    public void updateListenerConfiguration(Long id, ListenerConfigurationDto dto) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("监听器ID无效");
        }
        
        if (!dto.isValidConfiguration()) {
            throw new IllegalArgumentException("监听器配置无效");
        }

        log.info("更新监听器配置: id={}, dto={}", id, dto);
        
        // 调用核心模块的应用服务
        coreListenerApplicationService.updateListener(
            id,
            dto.atReplyEnable(),
            dto.keywordReplyEnable(),
            dto.savePic(),
            dto.saveVoice(),
            dto.parseLinks(),
            dto.keywordReply()
        );
    }

    /**
     * 删除监听器配置
     */
    public void deleteListenerConfiguration(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("监听器ID无效");
        }

        log.info("删除监听器配置: {}", id);
        
        // 调用核心模块的应用服务
        coreListenerApplicationService.deleteListener(id);
    }

    /**
     * 转换为DTO
     */
    private ListenerConfigurationDto convertToDto(MessageProcessingData data) {
        return new ListenerConfigurationDto(
            data.listener().getId(),
            data.listener().getChatId(),
            data.chat().getName(),
            data.listener().isAtReplyEnable(),
            data.listener().isKeywordReplyEnable(),
            data.listener().isSavePic(),
            data.listener().isSaveVoice(),
            data.listener().isParseLinks(),
            data.listener().getKeywordReply()
        );
    }
}
