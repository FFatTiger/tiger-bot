package com.fffattiger.wechatbot.core;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.fffattiger.wechatbot.wxauto.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message;
import com.fffattiger.wechatbot.util.JsonlHelper;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AsyncChatHistoryCollector implements ChatHistoryCollector {

    @Resource
    private ExecutorService executorService;

    private static final String COLLECTOR_DIR = "collector";

    @Override
    public void collect(String chatName, Message message) {
        executorService.execute(() -> {
            try {
                File collectorDir = new File(COLLECTOR_DIR);
                if (!collectorDir.exists()) {
                    collectorDir.mkdirs();
                }
                File file = new File(collectorDir, chatName + ".jsonl");
                JsonlHelper.appendJsonLines(file, List.of(message));
            } catch (Exception e) {
                log.error("collect chat history error", e);
            }
        });
    }

    @Override
    public List<Message> query(String chatName, String sender) {
        File file = new File(COLLECTOR_DIR, chatName + ".jsonl");
        try {
            List<Message> all = JsonlHelper.readJsonLines(file, Message.class);
            return all.stream().filter(message -> sender == null ? true : message.sender().equals(sender))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("query chat history error", e);
            return Collections.emptyList();
        }
    }
}
