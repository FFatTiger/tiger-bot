package com.fffattiger.wechatbot.application.handler.cmd;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class AddListenerCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private ChatApplicationService chatApplicationService;

    @Override
    public String getCommandName() {
        return "增加监听";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        Long chatId = context.getMessage().getChatId();

        try {
            chatApplicationService.startListening(chatId);
            Chat chat = chatApplicationService.findById(chatId);
            log.info("Chat [{}] is now being listened to.", chat.getName());
            context.replyText("✅ 已开始监听当前会话。");
        } catch (Exception e) {
            log.error("Failed to start listening to chat [{}]: {}", chatId, e.getMessage(), e);
            context.replyText("❌ 开始监听失败：" + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "/增加监听 <监听对象> - 增加监听对象";
    }
}