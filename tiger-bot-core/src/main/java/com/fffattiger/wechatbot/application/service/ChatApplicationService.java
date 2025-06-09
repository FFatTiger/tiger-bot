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
        return chatRepository.findByName(chatName)
                .orElseThrow(() -> new RuntimeException("Chat not found: " + chatName));
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
        log.info("创建聊天对象: {}", savedChat);
        return savedChat;
    }

    /**
     * 更新聊天的AI配置
     */
    public Chat updateAiConfiguration(Long chatId, AiSpecification newSpecification) {
        Chat chat = getChatById(chatId);
        
        // 使用领域对象的业务方法更新AI配置
        chat.updateAiConfiguration(newSpecification);
        
        Chat savedChat = chatRepository.save(chat);
        log.info("更新聊天AI配置: {}", savedChat);
        return savedChat;
    }

    /**
     * 删除聊天对象
     */
    public void deleteChat(Long chatId) {
        // 验证聊天对象存在
        getChatById(chatId);
        
        chatRepository.deleteById(chatId);
        log.info("删除聊天对象: {}", chatId);
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
