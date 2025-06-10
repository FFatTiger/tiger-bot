package com.fffattiger.wechatbot.management.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.application.service.MessageApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.message.Message;
import com.fffattiger.wechatbot.management.application.dto.MessageRecordDto;

import lombok.extern.slf4j.Slf4j;

/**
 * 消息管理应用服务
 * 负责消息记录的管理逻辑，作为管理模块与核心模块的协调层
 */
@Service
@Slf4j
public class MessageManagementApplicationService {

    @Autowired
    private MessageApplicationService coreMessageApplicationService;

    @Autowired
    private ChatApplicationService coreChatApplicationService;

    /**
     * 获取所有消息记录（分页）
     */
    public Page<MessageRecordDto> getAllMessageRecords(Pageable pageable) {
        Page<Message> messages = coreMessageApplicationService.getAllMessages(pageable);
        List<MessageRecordDto> dtos = messages.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, messages.getTotalElements());
    }

    /**
     * 根据聊天ID获取消息记录（分页）
     */
    public Page<MessageRecordDto> getMessageRecordsByChatId(Long chatId, Pageable pageable) {
        Page<Message> messages = coreMessageApplicationService.getMessagesByChatId(chatId, pageable);
        List<MessageRecordDto> dtos = messages.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, messages.getTotalElements());
    }

    /**
     * 搜索消息记录
     */
    public List<MessageRecordDto> searchMessageRecords(Long chatId, String keyword, 
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        List<Message> messages;

        if (chatId != null && keyword != null && !keyword.trim().isEmpty()) {
            // 按聊天ID和关键词搜索
            messages = coreMessageApplicationService.searchMessagesByChatIdAndContent(chatId, keyword);
        } else if (chatId != null) {
            // 按聊天ID搜索
            if (startTime != null && endTime != null) {
                messages = coreMessageApplicationService.getMessagesByChatIdAndTimeRange(chatId, startTime, endTime);
            } else {
                messages = coreMessageApplicationService.getMessagesByChatId(chatId);
            }
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // 按关键词搜索
            messages = coreMessageApplicationService.searchMessagesByContent(keyword);
        } else if (startTime != null && endTime != null) {
            // 按时间范围搜索
            messages = coreMessageApplicationService.getMessagesByTimeRange(startTime, endTime);
        } else {
            // 获取所有消息（限制数量）
            messages = coreMessageApplicationService.getAllMessages(Pageable.ofSize(1000)).getContent();
        }

        return messages.stream()
                .filter(msg -> filterByTimeRange(msg, startTime, endTime))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 根据发送者获取消息记录
     */
    public List<MessageRecordDto> getMessageRecordsBySender(String sender) {
        List<Message> messages = coreMessageApplicationService.getMessagesBySender(sender);
        return messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有聊天对象（用于筛选）
     */
    public List<Chat> getAllAvailableChats() {
        return coreChatApplicationService.getAllChats();
    }

    /**
     * 删除消息记录
     */
    public void deleteMessageRecord(Long messageId) {
        log.info("删除消息记录: {}", messageId);
        coreMessageApplicationService.deleteMessage(messageId);
    }

    /**
     * 清理聊天对象的历史消息
     */
    public void cleanupChatMessages(Long chatId) {
        log.info("清理聊天{}的历史消息", chatId);
        coreMessageApplicationService.deleteMessagesByChatId(chatId);
    }

    /**
     * 清理指定时间之前的历史消息
     */
    public void cleanupMessagesBeforeTime(LocalDateTime beforeTime) {
        log.info("清理{}之前的历史消息", beforeTime);
        coreMessageApplicationService.cleanupMessagesBeforeTime(beforeTime);
    }

    /**
     * 获取消息统计信息
     */
    public MessageApplicationService.MessageStatistics getMessageStatistics() {
        return coreMessageApplicationService.getMessageStatistics();
    }

    /**
     * 导出聊天记录（返回消息列表，由控制器处理导出格式）
     */
    public List<MessageRecordDto> exportChatRecords(Long chatId, LocalDateTime startTime, LocalDateTime endTime) {
        log.info("导出聊天记录: chatId={}, startTime={}, endTime={}", chatId, startTime, endTime);
        return searchMessageRecords(chatId, null, startTime, endTime);
    }

    /**
     * 转换为DTO
     */
    private MessageRecordDto convertToDto(Message message) {
        String chatName = "未知聊天";
        try {
            Chat chat = coreChatApplicationService.getChatById(message.getChatId());
            chatName = chat.getName();
        } catch (Exception e) {
            log.warn("获取聊天名称失败: chatId={}", message.getChatId(), e);
        }

        return new MessageRecordDto(
            message.getId(),
            message.getChatId(),
            chatName,
            message.getType(),
            message.getContent(),
            message.getSender(),
            message.getSenderRemark(),
            message.getInfo(),
            message.getTime(),
            message.getTime() != null ? message.getTime().toString() : "",
            message.getDisplaySender(),
            message.getType() != null ? message.getType().name() : "未知",
            message.isTextMessage()
        );
    }

    /**
     * 按时间范围过滤消息
     */
    private boolean filterByTimeRange(Message message, LocalDateTime startTime, LocalDateTime endTime) {
        if (message.getTime() == null) {
            return false;
        }
        if (startTime != null && message.getTime().isBefore(startTime)) {
            return false;
        }
        if (endTime != null && message.getTime().isAfter(endTime)) {
            return false;
        }
        return true;
    }
}
