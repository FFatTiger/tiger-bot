package com.fffattiger.wechatbot.infrastructure.event.handlers;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.Message;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.domain.listener.Listener;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupMessageHandler implements MessageHandlerExtension {

    @Override
    public boolean handle(MessageHandlerContext context) {
        Message message = context.getMessage();
        String content = message.cleanContent();
        String botName = context.getRobotName();

        if (!context.isGroupChat()) {
            return false;
        }

        if (message.type() == null || !message.type().equals(MessageType.FRIEND.getValue())
                || !StringUtils.hasLength(content)) {
            return false;
        }

        Listener listener = context.get("listener");
        if (!listener.shouldProcessMessage(content, botName, context.isGroupChat())) {
            return true;
        }

        String cleanContent = listener.extractCleanContent(content, botName);
        context.setMessage(new Message(message.id(), message.type(), message.rawContent(), cleanContent, message.senderId(), message.senderName(), message.chatId(), message.chatName(), message.timestamp()));
        return false;
    }




    @Override
    public int getOrder() {
        return -10;
    }

}