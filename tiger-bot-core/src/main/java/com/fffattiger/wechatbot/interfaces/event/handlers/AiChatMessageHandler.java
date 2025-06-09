package com.fffattiger.wechatbot.interfaces.event.handlers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageType;

import cn.hutool.core.date.DateUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * AI对话处理器
 */
@Service
@Slf4j
public class AiChatMessageHandler implements MessageHandler {
	@Resource
	private AiChatApplicationService aiChatApplicationService;

	@Resource
	private JdbcChatMemoryRepository chatMemoryRepository;

	@Value("classpath:/prompts/user-prompt.st")
	private org.springframework.core.io.Resource USER_PROMPT_RESOURCE;

	@Override
	public boolean handle(MessageHandlerContext context, MessageHandlerChain chain) {
		String cleanContent = context.cleanContent();
		BatchedSanitizedWechatMessages.Chat.Message message = context.message();
		MessageProcessingData chat = context.currentChat();
		if (message == null || message.type() == null || !message.type().equals(MessageType.FRIEND)
				|| !StringUtils.hasLength(cleanContent)) {
			return chain.handle(context);
		}
		Map<String, Object> params = new HashMap<>();
		params.put("chatType", chat.chat().isGroupFlag() ? "群聊" : "私聊");

		String content = chat(context, cleanContent, message, chat, params);

		if (!StringUtils.hasLength(content)) {
			context.wx().sendText(chat.chat().getName(), "繁忙， 请稍后再试");
			return false;
		}
		String[] contents = content.split("\\\\");
		for (String finalContent : contents) {
			context.wx().sendText(chat.chat().getName(), finalContent);
		}

		return true;
	}

	private String chat(MessageHandlerContext context, String cleanContent,
			BatchedSanitizedWechatMessages.Chat.Message message, MessageProcessingData chat, Map<String, Object> params) {
		ChatMemory chatMemory = MessageWindowChatMemory.builder()
				.chatMemoryRepository(chatMemoryRepository)
				.maxMessages(10)
				.build();
		return aiChatApplicationService.builder(chat.chat().getAiSpecification(), params)
				.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
				.build()
				.prompt()
				.user(t -> t.text(USER_PROMPT_RESOURCE)
						.param("time", DateUtil.date(context.messageTimestamp()).toString("yyyy-MM-dd HH:mm:ss EEEE"))
						.param("chatType", chat.chat().isGroupFlag() ? "群聊" : "私聊")
						.param("chatName", chat.chat().getName())
						.param("sender", message.sender())
						.param("content", cleanContent))
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chat.chat().getName()))
				.call()
				.content();
	}

	@Override
	public int getOrder() {
		return 10;
	}
}
