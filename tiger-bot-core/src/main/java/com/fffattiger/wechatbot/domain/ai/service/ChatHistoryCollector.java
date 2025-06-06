package com.fffattiger.wechatbot.domain.ai.service;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;

/**
 * 聊天记录收集器
 */
public interface ChatHistoryCollector {

    /**
     * 收集聊天记录
     * @param chatName 聊天名称
     * @param message 消息
     * @param timestamp 消息时间戳
     */
    void collect(String chatName, Message message, Long timestamp);
}
