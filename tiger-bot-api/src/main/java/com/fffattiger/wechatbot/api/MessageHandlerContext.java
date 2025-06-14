package com.fffattiger.wechatbot.api;

import java.io.File;

/**
 * 消息处理器上下文接口。
 * <p>
 * 为 MessageHandler 提供访问当前消息、发送者、聊天环境以及执行回复操作的能力。
 * 这是一个安全的接口，旨在提供面向意图的API，而非暴露底层实现。
 */
public interface MessageHandlerContext {


    /**
     * 设置当前正在处理的消息。
     *
     * @param message 消息对象。
     */
    void setMessage(Message message);

    /**
     * 获取当前正在处理的消息的详细信息。
     *
     * @return 当前消息对象，永不为 null。
     */
    Message getMessage();

    /**
     * 设置机器人的名称。
     *
     * @param robotName 机器人的名称。
     */
    void setRobotName(String robotName);

    /**
     * 获取机器人的名称。
     * 
     * @return 机器人的名称。
     */
    String getRobotName();

    /**
     * 设置当前消息是否是群聊消息。
     * 
     * @param isGroupChat 是否是群聊消息。
     */
    void setIsGroupChat(boolean isGroupChat);

    /**
     * 获取当前消息是否是群聊消息。
     * 
     * @return 是否是群聊消息。
     */
    boolean isGroupChat();

    /**
     * 在当前聊天中回复一条文本消息。
     * <p>
     * 这是一个便捷方法，会自动将消息发送到来源聊天（私聊或群聊）。
     *
     * @param text 要发送的文本内容。
     */
    void replyText(String text);

    /**
     * 在指定聊天中回复一条文本消息。
     * <p>
     * 这是一个便捷方法，会自动将消息发送到来源聊天（私聊或群聊）。
     *
     * @param chatName 要发送的聊天名称。
     * @param text 要发送的文本内容。
     */
    void replyText(String chatName, String text);

    /**
     * 在当前聊天中回复一个文件。
     *
     * @param file 要发送的文件。
     */
    void replyFile(File file);
    
    /**
     * 设置上下文中的值。
     *
     * @param key 键。
     * @param value 值。
     */
    void set(String key, Object value);

    /**
     * 获取上下文中的值。
     *
     * @param key 键。
     * @return 值。
     */
    <T> T get(String key);
    
    /**
     * 清除上下文中的值。
     */
    void clear();


}