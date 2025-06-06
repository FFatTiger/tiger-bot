package com.fffattiger.wechatbot.interfaces.event.handlers;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupMessageHandler implements MessageHandler {

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        ListenerAggregate chat = context.currentChat();
        BatchedSanitizedWechatMessages.Chat.Message message = context.message();
        String content = message.content();
        if (!chat.chat().groupFlag()) { 
            context.setCleanContent(content);
            return chain.handle(context);
        }

        if (message == null || message.type() == null || !message.type().equals(MessageType.FRIEND)
                || !StringUtils.hasLength(content)) {
            return chain.handle(context);
        }

        if (chat.listener().atReplyEnable() && !content.startsWith("@" + context.chatBotProperties().getRobotName())) {
            log.info("已开启@回复，未匹配到@回复，跳过");
            return false;
        }
        
        if (chat.listener().atReplyEnable()) {
            content = extractNoneAtContent(context, content);
        }

        context.setCleanContent(content);
        if (chat.listener().keywordReplyEnable()) {
            List<String> keywordReply = chat.listener().keywordReply();
            for (String keyword : keywordReply) {
                if (content.contains(keyword)) {
                    return chain.handle(context);
                }
            }
        } else {
            return chain.handle(context);
        }
        return false;
    }

    private String extractNoneAtContent(MessageHandlerContext context, String content) {
        String robotName = context.chatBotProperties().getRobotName();
        String realContent = content;
        String atRobot = "@" + robotName;
        int startIndex = content.indexOf(atRobot) + atRobot.length();
        while (startIndex < content.length() && Character.isWhitespace(content.charAt(startIndex))) {
            startIndex++;
        }
        if (startIndex < content.length()) {
            realContent = content.substring(startIndex);
        }

        content = realContent;
        return content;
    }

    @Override
    public int getOrder() {
        return -10;
    }

}