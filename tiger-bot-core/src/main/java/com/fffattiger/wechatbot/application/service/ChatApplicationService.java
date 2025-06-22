package com.fffattiger.wechatbot.application.service;

import java.util.List;

import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.Message;
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
    private final ChatBotProperties chatBotProperties;

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
        return chatRepository.findAllByListenerIsNotNull();
    }


    /**
     * 接收消息
     * @param chatName
     * @param spec
     */
    @Transactional
    public void receiveMessage(String chatName, WxAuto.WechatMessageSpecification.ChatSpecification.MessageSpecification spec, Long time) {
        Chat chat = chatRepository.findByName(chatName).orElseThrow(() -> new RuntimeException("Chat not found"));

        Message message = messageMapper.toMessage(spec, chat.getId(), time);
        messageRepository.save(message);

        if (chat.receiveMessage(message, chatBotProperties.getRobotName())) {
            eventPublisher.publishEvent(new MessageReceivedEvent(chat, message));
        }
    }

    /**
     * 根据聊天ID获取所有消息
     *
     * @param chatId
     * @return
     */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<Message> findAllMessagesByChatId(Long chatId) {
        return messageRepository.findByChatIdOrderByTimeAsc(chatId);
    }

    /**
     * 根据聊天ID获取聊天
     * @param chatId
     * @return
     */
    public Chat findById(Long chatId) {
        return chatRepository.findById(chatId).orElseThrow(() -> new RuntimeException("Chat not found"));
    }
}
