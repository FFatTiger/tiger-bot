package com.fffattiger.wechatbot.api;

public record Message(
    String id,          // 消息唯一ID
    String type,   // 消息类型 (FRIEND, GROUP, etc.)
    String rawContent,  // 原始消息内容
    String cleanContent,// 清理后的消息内容（例如，去除了@信息）
    String senderId,    // 发送者ID
    String senderName,  // 发送者昵称
    Long chatId,      // 聊天ID（私聊时等于senderId，群聊时为群ID）
    String chatName,
    Long timestamp
) {
}