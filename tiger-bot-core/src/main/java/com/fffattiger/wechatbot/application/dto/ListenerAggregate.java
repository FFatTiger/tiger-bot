package com.fffattiger.wechatbot.application.dto;

import java.util.List;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.Listener;
import com.fffattiger.wechatbot.domain.user.User;

public record ListenerAggregate(
    Listener listener,
    Chat chat,
    List<ChatCommandAuthWithCommandAndUser> commandAuths
) {
    public record ChatCommandAuthWithCommandAndUser(
        ChatCommandAuth auth,
        Command command,
        User user
    ) {}
}
