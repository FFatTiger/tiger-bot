package com.fffattiger.wechatbot.core.handler;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.fffattiger.wechatbot.core.WxChat;
import com.fffattiger.wechatbot.wxauto.MessageHandler;
import com.fffattiger.wechatbot.wxauto.MessageHandlerChain;
import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.wxauto.MessageType;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GroupMessageHandler implements MessageHandler {

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        WxChat chat = context.currentChat();
        BatchedSanitizedWechatMessages.Chat.Message message = context.message();
        String content = message.content();
        if (!chat.isGroup()) {
            context.setCleanContent(content);
            return chain.handle(context);
        }

        if (message == null || message.type() == null || !message.type().equals(MessageType.FRIEND)
                || !StringUtils.hasLength(content)) {
            return chain.handle(context);
        }

        if (chat.getWxChatConfig().isAtReplyEnable() && !content.startsWith("@" + context.chatBotProperties().getRobotName())) {
            log.info("已开启@回复，未匹配到@回复，跳过");
            return false;
        }
        
        if (chat.getWxChatConfig().isAtReplyEnable()) {
            content = extractNoneAtContent(context, content);
        }


        if (chat.getWxChatConfig().isKeywordReplyEnable()) {
            List<String> keywordReply = chat.getWxChatConfig().getKeywordReply();
            for (String keyword : keywordReply) {
                if (content.contains(keyword)) {
                    return false;
                }
            }
        }
        context.setCleanContent(content);
        return chain.handle(context);
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