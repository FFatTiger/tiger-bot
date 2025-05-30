package com.fffattiger.wechatbot.core;

import java.util.List;

import com.fffattiger.wechatbot.wxauto.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;

/**
 * 聊天记录收集器
 */
public interface ChatHistoryCollector {

    /**
     * 收集聊天记录
     * @param chatName 聊天名称
     * @param message 消息
     */
    void collect(String chatName, Message message);

    /**
     * 查询聊天记录
     * @param chatName 聊天名称
     * @return 聊天记录
     */
    List<Message> query(String chatName, String sender);

}
