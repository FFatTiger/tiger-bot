package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天应用服务
 * 专门负责聊天相关的应用逻辑
 */
@Service
@Slf4j
public class ChatApplicationService {

    @Resource
    private ChatRepository chatRepository;

    /**
     * 获取所有聊天对象
     */
    public List<Chat> getAllChats() {
        List<Chat> chats = new ArrayList<>();
        chatRepository.findAll().forEach(chats::add);
        return chats;
    }

    /**
     * 根据ID获取聊天对象
     */
    public Chat getChatById(Long chatId) {
        return chatRepository.findById(chatId)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatId));
    }

    /**
     * 根据名称获取聊天对象
     */
    public Chat getChatByName(String chatName) {
        log.debug("根据名称查找聊天对象: 聊天名称={}", chatName);

        try {
            Chat chat = chatRepository.findByName(chatName)
                    .orElse(null);

            if (chat != null) {
                log.debug("找到聊天对象: ID={}, 名称={}, 群聊={}, AI配置={}",
                        chat.getId(), chat.getName(), chat.isGroupFlag(),
                        chat.hasAiConfiguration() ? "已配置" : "未配置");
            } else {
                log.debug("未找到聊天对象: 聊天名称={}", chatName);
            }

            return chat;
        } catch (Exception e) {
            log.error("查找聊天对象失败: 聊天名称={}, 错误信息={}", chatName, e.getMessage(), e);
            throw new RuntimeException("Chat not found: " + chatName, e);
        }
    }

    /**
     * 创建聊天对象
     */
    public Chat createChat(String name, boolean groupFlag, AiSpecification aiSpecification) {
        Chat newChat = new Chat(null, name, groupFlag, aiSpecification);
        
        // 使用领域对象验证
        if (!newChat.canReceiveMessage()) {
            throw new RuntimeException("Invalid chat data: cannot receive messages");
        }
        
        Chat savedChat = chatRepository.save(newChat);
        
        return savedChat;
    }

    /**
     * 更新聊天的AI配置
     */
    public Chat updateAiConfiguration(Long chatId, AiSpecification newSpecification) {
        log.info("更新聊天AI配置: 聊天ID={}, 新配置=提供商ID:{}, 模型ID:{}, 角色ID:{}",
                chatId, newSpecification.aiProviderId(),
                newSpecification.aiModelId(), newSpecification.aiRoleId());

        try {
            Chat chat = getChatById(chatId);

            // 使用领域对象的业务方法更新AI配置
            chat.updateAiConfiguration(newSpecification);

            Chat savedChat = chatRepository.save(chat);
            log.info("聊天AI配置更新成功: 聊天ID={}, 聊天名称={}", chatId, savedChat.getName());

            return savedChat;
        } catch (Exception e) {
            log.error("更新聊天AI配置失败: 聊天ID={}, 错误信息={}", chatId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 删除聊天对象
     */
    public void deleteChat(Long chatId) {
        // 验证聊天对象存在
        getChatById(chatId);
        
        chatRepository.deleteById(chatId);
        
    }

    /**
     * 检查聊天对象是否可以接收消息
     */
    public boolean canReceiveMessage(Long chatId) {
        Chat chat = getChatById(chatId);
        return chat.canReceiveMessage();
    }

    /**
     * 检查聊天对象是否有AI配置
     */
    public boolean hasAiConfiguration(Long chatId) {
        Chat chat = getChatById(chatId);
        return chat.hasAiConfiguration();
    }
}
