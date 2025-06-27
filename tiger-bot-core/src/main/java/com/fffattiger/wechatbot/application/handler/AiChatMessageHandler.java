package com.fffattiger.wechatbot.application.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.api.dto.Message;
import com.fffattiger.wechatbot.application.service.AiApplicationService;
import com.fffattiger.wechatbot.application.service.ChatApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.ListenerConfiguration;
import com.fffattiger.wechatbot.domain.permission.service.PermissionDomainService;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI对话处理器
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiChatMessageHandler implements MessageHandlerExtension {
	private final AiApplicationService aiApplicationService;
	private final JdbcChatMemoryRepository chatMemoryRepository;
	private final ChatApplicationService chatApplicationService;
	private final PermissionDomainService permissionDomainService;

	@Value("classpath:/prompts/user-prompt.st")
	private org.springframework.core.io.Resource USER_PROMPT_RESOURCE;

	@Override
	public boolean handle(MessageHandlerContext context) {
		Message message = context.getMessage();

		// 0. 基础消息类型检查
		if (!"friend".equals(message.getAttr()) || !StringUtils.hasLength(message.getCleanContent())) {
			return false;
		}

		// 1. 获取领域对象
		Chat chat = chatApplicationService.findById(message.getChatId());
		ListenerConfiguration listenerConfig = chat.getListener();

		// 2. 检查是否满足触发条件（@或关键词）
		boolean shouldProcess = listenerConfig.shouldProcessMessage(
				message.getRawContent(),
				context.getRobotName(),
				context.isGroupChat()
		);

		if (!shouldProcess) {
			return false;
		}

		// // 3. 检查权限
		// if (!permissionDomainService.canAccessChat(Long.parseLong(message.getSenderId()), message.getChatId())) {
		// 	return false; 
		// }
		
		// 4. 执行核心业务 - AI对话
		long startTime = System.currentTimeMillis();
		String response = performAiChat(context, message, chat);
		long duration = System.currentTimeMillis() - startTime;

		if (!StringUtils.hasLength(response)) {
			log.warn("AI聊天响应为空: 聊天={}, 发送者={}, 耗时={}ms", message.getChatName(), message.getSenderName(), duration);
			context.replyText("繁忙， 请稍后再试");
			return true;
		}

		log.info("AI聊天响应成功: 聊天={}, 发送者={}, 响应长度={}, 耗时={}ms",
				message.getChatName(), message.getSenderName(), response.length(), duration);

		// 5. 发送响应
		// 分割长消息并发送
		String[] contents = response.split("\\\\");
        for (String finalContent : contents) {
            context.replyText(finalContent);
        }

		return true; // 成功处理，终止责任链
	}

	private String performAiChat(MessageHandlerContext context, Message message, Chat chat) {
		String cleanContent = chat.getListener().extractCleanContent(message.getCleanContent(), context.getRobotName());
		String conversationId = generateConversationId(message.getChatName(), chat.getAiSpecification().aiRoleId());

		Map<String, Object> params = new HashMap<>();
		params.put("chatType", context.isGroupChat() ? "群聊" : "私聊");

		try {
			ChatMemory chatMemory = MessageWindowChatMemory.builder()
					.chatMemoryRepository(chatMemoryRepository)
					.build();

            return aiApplicationService.builder(chat.getAiSpecification(), params)
                    .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                    .build()
                    .prompt()
                    .user(t -> t.text(USER_PROMPT_RESOURCE)
                            .param("time",
                                    DateUtil.date(message.getTimestamp()).toString("yyyy-MM-dd HH:mm:ss EEEE"))
                            .param("chatType", context.isGroupChat() ? "群聊" : "私聊")
                            .param("chatName", message.getChatName())
                            .param("sender", message.getSenderName())
                            .param("content", cleanContent))
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .call()
                    .content();

		} catch (Exception e) {
			log.error("AI chat failed", e);
			return null;
		}
	}

	/**
	 * 生成会话ID，格式为：roleId_chatName
	 * 这样不同角色的记忆会分开存储，切换角色时可以保留各角色的历史记忆
	 */
	private String generateConversationId(String chatName, Long roleId) {
		return roleId + "_" + chatName;
	}

	@Override
	public int getOrder() {
		return 100; // 调整为一个合适的值，在预处理器之后，在通用处理器之前
	}
}
