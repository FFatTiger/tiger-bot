package com.fffattiger.wechatbot.infrastructure.event.handlers;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupMessageHandler implements MessageHandler {

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        MessageProcessingData listenerData = context.currentChat();
        WechatMessageSpecification.ChatSpecification.MessageSpecification messageSpecification = context.message();
        String content = messageSpecification.content();
        String botName = context.chatBotProperties().getRobotName();

        if (!listenerData.chat().isGroupChat()) {
            context.setCleanContent(content);
            return chain.handle(context);
        }

        if (messageSpecification == null || messageSpecification.type() == null || !messageSpecification.type().equals(MessageType.FRIEND)
                || !StringUtils.hasLength(content)) {
            return chain.handle(context);
        }

        if (!listenerData.listener().shouldProcessMessage(content, botName, listenerData.chat().isGroupChat())) {
            return false;
        }

        String cleanContent = listenerData.listener().extractCleanContent(content, botName);
        context.setCleanContent(cleanContent);

        return chain.handle(context);
    }



    @Override
    public int getOrder() {
        return -10;
    }

}