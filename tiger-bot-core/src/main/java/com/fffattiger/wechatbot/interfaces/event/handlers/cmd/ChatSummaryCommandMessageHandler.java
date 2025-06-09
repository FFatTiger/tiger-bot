package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;
import com.fffattiger.wechatbot.infrastructure.external.chatlog.ChatLogClient;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.shared.util.MarkdownToImageConverter;

import cn.hutool.core.io.FileUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天总结
 */
@Service
@Slf4j
public class ChatSummaryCommandMessageHandler extends AbstractCommandMessageHandler {

    @Resource
    private AiChatApplicationService aiChatApplicationService;

    @Resource
    private ChatLogClient chatLogClient;

    @Resource
    private CommandRepository commandRepository;

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/总结") || command.startsWith("/summary");
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        if (args.length > 2) {
            context.wx().sendText(context.currentChat().chat().getName(), "命令格式错误，请参考帮助");
            return;
        }

        String dateStr = args[0];
        String chatName = context.currentChat().chat().getName();
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
            log.error("没有找到聊天记录: {}", summaryTargetChatName);
            context.wx().sendText(chatName, "没有找到聊天记录");
            return;
        }

        context.wx().sendText(chatName, "总结中");
        AiSpecification aiSpecification = commandRepository.findByPattern("/总结").getAiSpecification();
        String aiSummary = chat(chatLog, aiSpecification);
        if (!StringUtils.hasLength(aiSummary)) {
            context.wx().sendText(chatName, "服务繁忙，稍后再试");
            return;
        }
        
        File outputFile = renderImageFile(context, aiSummary, summaryTargetChatName);

        context.wx().sendFileByUpload(chatName, outputFile);
    }

    private File renderImageFile(MessageHandlerContext context, String aiSummary, String chatName) {
        String tempFileDir = System.getProperty("user.home") + File.separator
                + context.chatBotProperties().getTempFileDir() + File.separator;

        FileUtil.mkdir(tempFileDir);

        String outputFile = tempFileDir + chatName + "_" + System.currentTimeMillis() + ".png";

        boolean b = MarkdownToImageConverter.convertMarkdownToImage(aiSummary, outputFile, 900, "png");
        if (!b) {
            log.error("生成图片失败");
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
            log.error("服务繁忙，稍后再试", e);
            return null;
        }
    }

    @Override
    public String description() {
        return "/总结 [昨天|今天] [群聊名称] 总结聊天记录";
    }
}
