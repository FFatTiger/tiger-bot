package com.fffattiger.wechatbot.api;


import org.pf4j.ExtensionPoint;

/**
 * 命令消息处理器接口。
 * <p>
 * 继承自 MessageHandler，专门用于处理格式化的命令消息。
 * 它定义了命令处理器的特定契约，如获取命令名称和描述。
 */
public interface CommandMessageHandlerExtension extends ExtensionPoint {

    /**
     * 返回此处理器对应的命令名（不带前缀）。
     * <p>
     * 例如，对于命令 "!help"，此方法应返回 "help"。
     *
     * @return 命令名。
     */
    String getCommandName();

    /**
     * 获取命令的描述，用于自动生成帮助文档。
     *
     * @return 命令的功能描述。
     */
    String getDescription();

    /**
     * 处理
     * 
     * @param command 命令
     * @param args 命令参数
     * @param context 上下文
     */
    void doHandle(String command, String[] args, MessageHandlerContext context);
}