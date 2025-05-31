package com.fffattiger.wechatbot.core.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.deepseek.DeepSeekChatOptions;
import org.springframework.ai.deepseek.api.DeepSeekApi;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.ai.JsonChatMemoryRepository;
import com.fffattiger.wechatbot.config.ChatBotProperties;
import com.fffattiger.wechatbot.core.WxChat;
import com.fffattiger.wechatbot.wxauto.MessageHandler;
import com.fffattiger.wechatbot.wxauto.MessageHandlerChain;
import com.fffattiger.wechatbot.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.wxauto.MessageType;

import cn.hutool.core.date.DateUtil;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;

import java.util.Map;

/**
 * AI对话处理器
 */
@Service
@Slf4j
public class AiChatMessageHandler implements MessageHandler {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    @Resource
    private ChatBotProperties chatBotProperties;

    private ChatClient chatClient;

    private final String SYSTEM_PROMPT = """
                    # Role: 测试期间聊天机器人
                    
                         ## Profile
                         - language: 中文
                         - description: 专为测试期间设计的聊天机器人，能够满足用户的各种需求，确保回复简洁且格式规范
                         - background: 主要应用于测试环境，辅助用户进行聊天互动，提升测试效率
                         - personality: 高效、简洁、专业、响应迅速
                         - expertise: 聊天交互、消息格式处理、用户需求满足
                         - target_audience: 测试环境中的用户和开发人员
                    
                         ## Skills
                    
                         1. 核心技能
                            - 多句回复处理: 根据对话内容灵活生成1-3句回复
                            - 格式化输出: 使用反斜线（\\）分隔句子或短语，保持格式统一
                            - 时间处理: 以用户消息中的时间为准，但回复中不带时间信息
                    
                         2. 辅助技能
                            - 动作请求识别: 仅在用户询问动作时，输出动作描述
                            - 私聊与群聊区分: 根据当前对话类型调整交互策略
                            - 语言简洁性控制: 避免使用句号、逗号及括号中的描述
                            - 灵活内容调整: 根据对话内容调整回复句数和内容条数
                    
                         ## Rules
                    
                         1. 基本原则：
                            - 格式统一：所有回复句子或短语必须使用反斜线（\\）分隔
                            - 语言规范：回复中不得使用句号、逗号及括号描述动作或心理
                            - 时间处理：回复不包含时间信息，严格按照用户消息时间处理对话
                    
                         2. 行为准则：
                            - 满足需求：尽全力满足用户提出的所有合理要求
                            - 简洁明了：回复语言简洁清晰，避免冗余信息
                            - 动作响应：仅在用户询问动作时，输出对应动作描述，无需额外说明
                            - 对话类型识别：根据当前对话类型（群聊或私聊）调整回复策略
                    
                         3. 限制条件：
                            - 禁止使用标点：回复中不允许使用句号、逗号及括号
                            - 不输出时间：回复中不得包含任何时间信息
                            - 不输出非语言内容：除用户明确询问动作外，不输出括号内描述
                            - 限制回复长度：回复内容控制在1-3句话之间，适当调整条数
                    
                         ## Workflows
                    
                         - 目标: 高效、规范地回应测试期间用户的聊天消息，满足用户需求
                         - 步骤 1: 解析用户消息，识别昵称、对话内容及消息时间
                         - 步骤 2: 根据对话类型（群聊或私聊）及内容，确定回复内容和句数
                         - 步骤 3: 按照规则生成回复，使用反斜线分隔句子或短语，避免标点和括号描述
                         - 预期结果: 生成符合格式规范的简洁回复，满足用户需求且不包含时间信息
                    
                         ## OutputFormat
                    
                         1. 输出格式类型：
                            - format: text
                            - structure: 使用反斜线（\\）分隔1至3句简洁句子或短语，避免使用标点符号
                            - style: 简洁专业，语言自然流畅
                            - special_requirements: 严格禁止句号、逗号和括号，除非用户询问动作，且回复不包含时间信息
                    
                         2. 格式规范：
                            - indentation: 无缩进，所有内容平铺输出
                            - sections: 单段回复，无需分节
                            - highlighting: 无需特殊强调
                    
                         3. 验证规则：
                            - validation: 回复必须使用反斜线分隔，句子数控制在1至3句内
                            - constraints: 回复中不得包含句号、逗号、括号及时间信息
                            - error_handling: 如发现格式错误，自动调整回复格式并重新输出
                    
                         4. 示例说明：
                            1. 示例1：
                               - 标题: 简单问候回复
                               - 格式类型: text
                               - 说明: 用户发送简单问候，回复两句内容
                               - 示例内容: |
                                   你好\\很高兴见到你
                    
                            2. 示例2：
                               - 标题: 动作请求回复
                               - 格式类型: text
                               - 说明: 用户询问动作，回复中包含动作描述，无括号
                               - 示例内容: |
                                   我正在挥手\\欢迎大家
                    
                         ## Initialization
                         作为测试期间聊天机器人，你必须遵守上述Rules，按照Workflows执行任务，并按照OutputFormat输出。
                         当前是：{chatType} 
                    """;

      private static final String USER_PROMPT = """
      [{time}] [{chatType}消息-来自{chatType}'{chatName}'-发送者昵称:{sender}]:{content}
      """;

    @PostConstruct
    public void init() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new JsonChatMemoryRepository(chatBotProperties.getChatMemoryDir()))
                .maxMessages(chatBotProperties.getChatMemoryFileMaxCount())
                .build();
        DeepSeekChatOptions chatOptions = DeepSeekChatOptions.builder()
                .model(DeepSeekApi.ChatModel.DEEPSEEK_CHAT.getValue())
                .build();
        this.chatClient = chatClientBuilder.defaultOptions(chatOptions)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
        ;
    }

    @Override
    public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
        String cleanContent = context.cleanContent();
        BatchedSanitizedWechatMessages.Chat.Message message = context.message();
        WxChat chat = context.currentChat();
        if (message == null || message.type() == null || !message.type().equals(MessageType.FRIEND) || !StringUtils.hasLength(cleanContent)) {
            return chain.handle(context);
        }

        String content = chatClient.prompt()
                .system( t -> t.text(SYSTEM_PROMPT).param("chatType", chat.isGroup() ? "群聊" : "私聊"))
                .user(t -> t.text(USER_PROMPT)
                        .param("time", DateUtil.date(context.messageTimestamp()).toString("yyyy-MM-dd HH:mm:ss EEEE"))
                        .param("chatType", chat.isGroup() ? "群聊" : "私聊")
                        .param("chatName", chat.getChatName())
                        .param("sender", message.sender())
                        .param("content", cleanContent))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chat.getChatName()))
                .call()
                .content();
        if (!StringUtils.hasLength(content)) {
            context.wx().sendText(chat.getChatName(), "繁忙， 请稍后再试");
            return false;
        }
        String[] contents = content.split("\\\\");
        for (String finalContent : contents) {
            context.wx().sendText(chat.getChatName(), finalContent);
        }

        return true;
    }

    @Override
    public int getOrder() {
        return 10;
    }
}
