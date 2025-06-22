package com.fffattiger.wechatbot.api;


import org.pf4j.ExtensionPoint;

import com.fffattiger.wechatbot.api.handler.CommandMessageHandler;

/**
 * 命令消息处理器扩展接口。
 * <p>
 * 继承自 MessageHandler，专门用于处理格式化的命令消息。
 * 它定义了命令处理器的特定契约，如获取命令名称和描述。
 */
public interface CommandMessageHandlerExtension extends ExtensionPoint, CommandMessageHandler {

}