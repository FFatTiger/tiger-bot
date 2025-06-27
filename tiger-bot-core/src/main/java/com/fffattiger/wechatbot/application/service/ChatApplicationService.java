package com.fffattiger.wechatbot.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.ChatMessage;
import com.fffattiger.wechatbot.domain.chat.event.MessageReceivedEvent;
import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.chat.repository.MessageRepository;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;
import com.fffattiger.wechatbot.infrastructure.mapper.MessageMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatApplicationService {

    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 获取所有聊天
     * @return
     */
    public List<Chat> findAll() {
        return chatRepository.findAll();
    }

    /**
     * 获取所有监听的聊天
     * @return
     */
    public List<Chat> findAllListened() {
        return chatRepository.findByListenerEnableIsTrue();
    }


    /**
     * 接收消息
     * @param chatName
     * @param spec
     */
    @Transactional
    public void receiveMessage(String chatName, WxAuto.WechatMessageSpecification.ChatSpecification.MessageSpecification spec, Long time) {
        Chat chat = chatRepository.findByName(chatName).orElseThrow(() -> new RuntimeException("Chat not found"));

        ChatMessage message = messageMapper.toMessage(spec, chat.getId(), time);
        messageRepository.save(message);

        if (chat.isListened()) {
            eventPublisher.publishEvent(new MessageReceivedEvent(chat, message));
        }
    }

    /**
     * 根据聊天ID获取聊天
     * @param chatId
     * @return
     */
    public Chat findById(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    /**
     * 切换AI角色
     * @param chatId
     * @param newRoleId
     */
    @Transactional
    public void changeAiRole(Long chatId, Long newRoleId) {
        Chat chat = findById(chatId);
        chat.changeAiRole(newRoleId);
        chatRepository.save(chat);
    }

    /**
     * 开始监听
     * @param chatId
     */
    @Transactional
    public void startListening(Long chatId) {
        Chat chat = findById(chatId);
        chat.startListening();
        chatRepository.save(chat);
        log.info("Chat [{}] started listening.", chat.getName());
    }

    /**
     * Get messages for summary
     * @param chatId
     * @param date
     * @return
     */
    public List<ChatMessage> findMessagesByChatIdAndDate(Long chatId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<ChatMessage> messages = messageRepository.findByChatIdAndTimeBetweenOrderByTimeAsc(chatId, startOfDay, endOfDay);

        if (messages.isEmpty()) {
            return List.of();
        }

        return messages;
    }
}
