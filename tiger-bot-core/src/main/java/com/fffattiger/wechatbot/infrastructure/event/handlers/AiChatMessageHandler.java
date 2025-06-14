package com.fffattiger.wechatbot.infrastructure.event.handlers;

import java.util.HashMap;
import java.util.Map;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.*;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.api.Message;
import com.fffattiger.wechatbot.api.MessageHandlerContext;
import com.fffattiger.wechatbot.api.MessageHandlerExtension;
import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import cn.hutool.core.date.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * AI对话处理器
 */
@Service
@Slf4j
public class AiChatMessageHandler implements MessageHandlerExtension {
	@Resource
	private AiChatApplicationService aiChatApplicationService;

	@Resource
	private JdbcChatMemoryRepository chatMemoryRepository;

	@Value("classpath:/prompts/user-prompt.st")
	private org.springframework.core.io.Resource USER_PROMPT_RESOURCE;

	@Override
	public boolean handle(MessageHandlerContext context) {
		String cleanContent = context.getMessage().cleanContent();
		Message message = context.getMessage();
		String chatName = message.chatName();
		String sender = message.senderName();

		// 检查是否为有效的AI聊天消息
		if (message.type() == null || message.type() != MessageType.FRIEND.getValue()
				|| !StringUtils.hasLength(cleanContent)) {
			log.debug("非AI聊天消息，跳过处理: 聊天={}, 发送者={}, 消息类型={}",
					chatName, sender, message.type());
			return false;
		}

		log.info("开始AI聊天处理: 聊天={}, 发送者={}, 消息长度={}",
				chatName, sender, cleanContent.length());

		Map<String, Object> params = new HashMap<>();
		String chatType = context.isGroupChat() ? "群聊" : "私聊";
		params.put("chatType", chatType);

		long startTime = System.currentTimeMillis();
		String content = chat(context, cleanContent, message, params);
		long duration = System.currentTimeMillis() - startTime;

		if (!StringUtils.hasLength(content)) {
			log.warn("AI聊天响应为空: 聊天={}, 发送者={}, 耗时={}ms", chatName, sender, duration);
			context.replyText("繁忙， 请稍后再试");
			return false;
		}

		log.info("AI聊天响应成功: 聊天={}, 发送者={}, 响应长度={}, 耗时={}ms",
				chatName, sender, content.length(), duration);

		// 分割长消息并发送
		String[] contents = content.split("\\\\");
		log.debug("AI响应分割为{}段消息进行发送", contents.length);

		for (int i = 0; i < contents.length; i++) {
			String finalContent = contents[i];
			log.debug("发送AI响应消息段: 聊天={}, 段数={}/{}, 长度={}",
					chatName, i + 1, contents.length, finalContent.length());
			context.replyText(finalContent);
		}

		return true;
	}

	private String chat(MessageHandlerContext context, String cleanContent, Message message,
			Map<String, Object> params) {
		String chatName = message.chatName();
		String sender = message.senderName();
		String conversationId = generateConversationId(chatName, ((AiSpecification) context.get("aiSpecification")).aiRoleId());

		log.debug("构建AI聊天请求: 聊天={}, 发送者={}, 会话ID={}", chatName, sender, conversationId);

		try {
			ChatMemory chatMemory = MessageWindowChatMemory.builder()
					.chatMemoryRepository(chatMemoryRepository)
					.maxMessages(10)
					.build();

			String response = aiChatApplicationService.builder(context.get("aiSpecification"), params)
					.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
					.build()
					.prompt()
					.user(t -> t.text(USER_PROMPT_RESOURCE)
							.param("time",
									DateUtil.date(context.getMessage().timestamp()).toString("yyyy-MM-dd HH:mm:ss EEEE"))
							.param("chatType", context.isGroupChat() ? "群聊" : "私聊")
							.param("chatName", chatName)
							.param("sender", sender)
							.param("content", cleanContent))
					.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
					.call()
					.content();

			log.debug("AI聊天请求成功: 聊天={}, 发送者={}, 响应长度={}",
					chatName, sender, response != null ? response.length() : 0);
			return response;

		} catch (Exception e) {
			log.error("AI聊天请求失败: 聊天={}, 发送者={}, 错误信息={}",
					chatName, sender, e.getMessage(), e);
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
		return 10;
	}
}
