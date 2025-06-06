package com.fffattiger.wechatbot.infrastructure.external.wchat;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

public interface MessageHandlerContext {

    /**
     * 设置上下文变量
     * @param key 变量名
     * @param value 变量值
     */
    void set(String key, Object value);

    /**
     * 获取上下文变量
     * @param key 变量名
     * @return 变量值
     */
    <T> T get(String key);

    /**
     * 获取微信消息
     * @return 微信消息
     */
    Message message();

    /**
     * 设置微信消息
     * @param message 微信消息
     */
    void setMessage(Message message);

    /** 
     * 获取微信消息时间戳
     * @return 微信消息时间戳
     */
    Long messageTimestamp();

    /**
     * 设置微信消息时间戳
     * @param messageTimestamp 微信消息时间戳
     */
    void setMessageTimestamp(Long messageTimestamp);

    /**
     * 经过处理的微信消息，不含@robotName
     * @return 经过处理的微信消息
     */
    String cleanContent();

    /**
     * 设置经过处理的微信消息
     * @param cleanContent 经过处理的微信消息
     */
    void setCleanContent(String cleanContent);

    /**
     * 设置微信客户端
     * @param wxAuto 微信客户端
     */
    void setWxAuto(WxAuto wxAuto);

    /**
     * 获取微信客户端
     * @return 微信客户端
     */
    WxAuto wx();

    /** 
     * 设置当前聊天对象
     * @param currentChat 当前聊天对象
     */
    void setCurrentChat(ListenerAggregate currentChat);

    /**
     * 获取当前聊天对象
     * @return 当前聊天对象
     */
    ListenerAggregate currentChat();

    /**
     * 全局配置
     */
    ChatBotProperties chatBotProperties();

    /**
     * 设置全局配置 
     * @param chatBotProperties 全局配置
     */
    void setChatBotProperties(ChatBotProperties chatBotProperties);
    

    /**
     * 清除上下文变量
     */
    void clear();
}
