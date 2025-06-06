package com.fffattiger.wechatbot.application.service;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.message.Message;
import com.fffattiger.wechatbot.domain.message.repository.MessageRepository;

import jakarta.annotation.Resource;

@Service
public class MessageApplicationService {

    @Resource
    private MessageRepository messageRepository;

    public void save(Message message) {
        messageRepository.save(message);
    }
}
