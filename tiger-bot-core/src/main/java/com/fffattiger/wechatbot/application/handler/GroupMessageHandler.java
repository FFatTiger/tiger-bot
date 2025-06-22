package com.fffattiger.wechatbot.application.handler;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.api.dto.Message;
import com.fffattiger.wechatbot.application.dto.DefaultMessage;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupMessageHandler implements MessageHandlerExtension {

    private final ChatApplicationService chatApplicationService;

    @Override
    public boolean handle(MessageHandlerContext context) {
        Message message = context.getMessage();
        String content = message.getCleanContent();
        String botName = context.getRobotName();

        if (!context.isGroupChat()) {
            return false;
        }

        if (message.getAttr() == null || !"friend".equals(message.getAttr())
                || !StringUtils.hasLength(content)) {
            return false;
        }
        Chat chat = chatApplicationService.findById(message.getChatId());

        if (!chat.isListened() || !chat.getListener().shouldProcessMessage(content, botName, context.isGroupChat())) {
            return false;
        }

        String cleanContent = chat.getListener().extractCleanContent(content, botName);
        ((DefaultMessage) context.getMessage()).updateCleanContent(cleanContent);
        return false;
    }



    @Override
    public int getOrder() {
        return -10;
    }

}