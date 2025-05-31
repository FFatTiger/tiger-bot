package com.fffattiger.wechatbot.core.handler.cmd;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.deepseek.DeepSeekAssistantMessage;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.chatlog.ChatLogClient;
import com.fffattiger.wechatbot.util.MarkdownToImageConverter;
import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;

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
    private DeepSeekChatModel chatModel;

    @Resource
    private ChatLogClient chatLogClient;

    private final PromptTemplate promptTemplate = PromptTemplate.builder()
            .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
            .template("""
                    你现在需要扮演 贴吧嘴臭老哥, 用这样的形式对对话的内容进行总结。不要讲逻辑和道理，直接情绪输出，嘴臭拉满。并总结谁是群聊大王提取经典发言，最后想几个成就给群友颁发：\n\n
                        聊天记录：
                    <chat_history>
                    """)
            .build();

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/总结") || command.startsWith("/summary");
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        if (args.length > 2) {
            context.wx().sendText(context.currentChat().getChatName(), "命令格式错误，请参考帮助");
            return;
        }

        String dateStr = args[0];
        String chatName = context.currentChat().getChatName();
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

        String aiSummary = chat(chatLog);
        if (!StringUtils.hasLength(aiSummary)) {
            context.wx().sendText(chatName, "服务繁忙，稍后再试");
            return;
        }

        // TODO 异常处理
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

    private String chat(String chatLog) {
        try {
            DeepSeekChatOptions promptOptions = DeepSeekChatOptions.builder()
                    .model(DeepSeekApi.ChatModel.DEEPSEEK_REASONER.getValue())
                    .build();
            Prompt prompt = promptTemplate.create(Map.of("chat_history", chatLog), promptOptions);
            ChatResponse response = chatModel.call(prompt);

            DeepSeekAssistantMessage deepSeekAssistantMessage = (DeepSeekAssistantMessage) response.getResult()
                    .getOutput();
            // String reasoningContent = deepSeekAssistantMessage.getReasoningContent();
            String text = deepSeekAssistantMessage.getText();
            return text;
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
