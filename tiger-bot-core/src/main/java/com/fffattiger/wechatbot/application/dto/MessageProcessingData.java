package com.fffattiger.wechatbot.application.dto;

import java.util.List;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.Listener;
import com.fffattiger.wechatbot.domain.user.User;

/**
 * 消息处理器专用数据聚合
 * 移除重复的业务方法，让接口层直接使用领域对象的业务方法
 */
public record MessageProcessingData(
    Listener listener,
    Chat chat,
    List<ChatCommandAuthWithCommandAndUser> commandAuths
) {

    public record ChatCommandAuthWithCommandAndUser(
        ChatCommandAuth auth,
        Command command,
        User user  // 可能为null，表示全局权限
    ) {}
}
