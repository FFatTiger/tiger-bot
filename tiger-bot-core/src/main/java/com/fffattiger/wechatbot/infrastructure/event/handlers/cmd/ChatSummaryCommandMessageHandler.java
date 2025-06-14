package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;
import com.fffattiger.wechatbot.infrastructure.external.chatlog.ChatLogClient;
import com.fffattiger.wechatbot.shared.util.MarkdownToImageConverter;

import cn.hutool.core.io.FileUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天总结
 */
@Service
@Slf4j
public class ChatSummaryCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private AiChatApplicationService aiChatApplicationService;

    @Resource
    private ChatLogClient chatLogClient;

    @Resource
    private CommandRepository commandRepository;

    @Override
    public String getCommandName() {
        return "总结";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        if (args.length > 2) {
            context.replyText(context.getMessage().chatName(), "命令格式错误，请参考帮助");
            return;
        }

        String dateStr = args[0];
        String chatName = context.getMessage().chatName();
        String summaryTargetChatName = chatName;
        if (args.length > 1) {
            summaryTargetChatName = args[1];
        }

        if (dateStr.contains("今天")) {
            dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (dateStr.contains("昨天")) {
            dateStr = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        String chatLog = chatLogClient.getChatHistory(summaryTargetChatName, dateStr, null);
        if (!StringUtils.hasLength(chatLog)) {
            
            context.replyText(chatName, "没有找到聊天记录");
            return;
        }

        context.replyText(chatName, "总结中");
        AiSpecification aiSpecification = commandRepository.findByPattern("/总结").getAiSpecification();
        String aiSummary = chat(chatLog, aiSpecification);
        if (!StringUtils.hasLength(aiSummary)) {
            context.replyText(chatName, "服务繁忙，稍后再试");
            return;
        }
        
        File outputFile = renderImageFile(context, aiSummary, summaryTargetChatName);

        context.replyFile(outputFile);
    }

    private File renderImageFile(MessageHandlerContext context, String aiSummary, String chatName) {
        String tempFileDir = System.getProperty("user.home") + File.separator
                + context.get("tempFileDir") + File.separator;

        FileUtil.mkdir(tempFileDir);

        String outputFile = tempFileDir + chatName + "_" + System.currentTimeMillis() + ".png";

        boolean b = MarkdownToImageConverter.convertMarkdownToImage(aiSummary, outputFile, 900, "png");
        if (!b) {
            
            return null;
        }
        return new File(outputFile);
    }

    private String chat(String chatLog, AiSpecification aiSpecification) {
        try {
            return aiChatApplicationService.builder(aiSpecification, Map.of("chat_history", chatLog))
                    .build()
                    .prompt()
                    .call()
                    .content();
        } catch (Exception e) {
            
            return null;
        }
    }

    @Override
    public String getDescription() {
        return "/总结 [昨天|今天] [群聊名称] 总结聊天记录";
    }
}
