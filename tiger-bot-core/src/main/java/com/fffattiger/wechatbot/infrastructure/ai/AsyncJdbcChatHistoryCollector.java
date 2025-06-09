package com.fffattiger.wechatbot.infrastructure.ai;

import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.application.assembler.MessageAssembler;
import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.application.service.ListenerApplicationService;
import com.fffattiger.wechatbot.application.service.MessageApplicationService;
import com.fffattiger.wechatbot.domain.ai.service.ChatHistoryCollector;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AsyncJdbcChatHistoryCollector implements ChatHistoryCollector {

   
    @Resource
    private ExecutorService executorService;

    @Resource
    private MessageApplicationService messageApplicationService;

    @Resource
    private ListenerApplicationService listenerApplicationService;

    @Resource
    private MessageAssembler messageAssembler;

    @Override
    public void collect(String chatName, Message message, Long timestamp) {
        executorService.execute(() -> {
            try {
                MessageProcessingData listenerAggregate = listenerApplicationService.getMessageProcessingData(chatName);
                messageApplicationService.save(messageAssembler.toMessage(message, listenerAggregate.chat(), timestamp));
            } catch (Exception e) {
                log.error("collect chat history error", e);
            }
        });
    }

}
