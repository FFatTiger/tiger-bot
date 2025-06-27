package com.fffattiger.wechatbot.application.handler.cmd;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;


import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.annotation.Resource;

@Service
public class StatusCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private ChatApplicationService chatApplicationService;

    @Resource
    private ApplicationContext applicationContext;

    @Value("${info.app.version:unknown}")
    private String appVersion;


    @Override
    public String getCommandName() {
        return "状态";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        Long chatId = context.getMessage().getChatId();
        Chat chat = chatApplicationService.findById(chatId);

        if (chat == null) {
            context.replyText("获取当前聊天状态失败。");
            return;
        }

        AiSpecification aiSpec = chat.getAiSpecification();

        // 运行时间
        long startupTimestamp = applicationContext.getStartupDate();
        Double startupHours = (System.currentTimeMillis() - startupTimestamp) / (1000 * 60 * 60.0);
        
        String statusReport = String.format(
            "======== 状态报告 ========\n" +
            "· 当前版本: %s\n" +
            "· 运行时间: %s\n" +
            "· 机器人状态: %s\n" +
            "· 当前模型: %s\n" +
            "· 当前角色: %s\n" +
            "========================",
            appVersion,
            startupHours + "小时",
            chat.isListened() ? "✅ 运行中" : "❌ 已暂停",
            aiSpec.aiModelId(),
            aiSpec.aiRoleId()
        );

        context.replyText(statusReport);
    }

    @Override
    public String getDescription() {
        return "/状态 - 查看机器人运行状态";
    }
}