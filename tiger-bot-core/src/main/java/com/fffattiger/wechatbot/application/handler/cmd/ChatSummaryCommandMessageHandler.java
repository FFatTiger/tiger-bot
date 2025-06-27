package com.fffattiger.wechatbot.application.handler.cmd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.AiApplicationService;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.ChatMessage;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChatSummaryCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private AiApplicationService aiApplicationService;

    @Resource
    private ChatApplicationService chatApplicationService;

    @Override
    public String getCommandName() {
        return "总结";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        if (args.length == 0) {
            context.replyText("命令格式错误，请使用：/总结 [今天|昨天]");
            return;
        }

        LocalDate date;
        if (args[0].contains("今天")) {
            date = LocalDate.now();
        } else if (args[0].contains("昨天")) {
            date = LocalDate.now().minusDays(1);
        } else {
            context.replyText("无法识别的日期，请输入'今天'或'昨天'。");
            return;
        }

        Long chatId = context.getMessage().getChatId();
        List<ChatMessage> messages = chatApplicationService.findMessagesByChatIdAndDate(chatId, date);
        String chatLog = messages.stream()
                .map(message -> message.getSender() + ": " + message.getContent())
                .collect(Collectors.joining("\n"));
        
        if (messages.isEmpty()) {
            context.replyText("没有找到 " + date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 的聊天记录。");
            return;
        }

        context.replyText("正在为您总结，请稍候...");
        
        Chat chat = chatApplicationService.findById(chatId);
        String aiSummary = generateSummary(chatLog, chat.getAiSpecification());

        if (!StringUtils.hasLength(aiSummary)) {
            context.replyText("服务繁忙，无法生成摘要，请稍后再试。");
            return;
        }
        
        context.replyText(aiSummary);
    }

    private String generateSummary(String chatLog, AiSpecification aiSpecification) {
        try {
            return aiApplicationService.builder(aiSpecification, Map.of("chat_history", chatLog))
                    .build()
                    .prompt()
                    .user("请根据以下聊天记录，以markdown格式总结出要点：\n\n" + chatLog)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("Failed to generate chat summary", e);
            return null;
        }
    }

    @Override
    public String getDescription() {
        return "/总结 [昨天|今天] - 总结当天的聊天记录";
    }
}
