package com.fffattiger.wechatbot.api;

import org.pf4j.ExtensionPoint;

import com.fffattiger.wechatbot.api.handler.MessageHandler;

/**
 * 消息处理器扩展接口。
 * <p>
 * 继承自 MessageHandler，专门用于处理消息。
 * 它定义了消息处理器的特定契约，如处理消息的逻辑。
 */
public interface MessageHandlerExtension extends ExtensionPoint, MessageHandler{

}