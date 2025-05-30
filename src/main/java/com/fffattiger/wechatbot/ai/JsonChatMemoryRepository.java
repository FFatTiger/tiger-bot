package com.fffattiger.wechatbot.ai;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.*;
import org.springframework.lang.NonNull;

import lombok.extern.slf4j.Slf4j;
import com.fffattiger.wechatbot.util.JsonlHelper;

@Slf4j
public class JsonChatMemoryRepository implements ChatMemoryRepository {

    private final File outputDir;

    public JsonChatMemoryRepository(String outputPath) {
        this.outputDir = new File(outputPath);
        if (!this.outputDir.exists()) {
            this.outputDir.mkdirs();
        }
    }

    @Override
    @NonNull
    public List<String> findConversationIds() {
        File[] files = outputDir.listFiles((dir, name) -> name.endsWith(".jsonl"));
        if (files == null)
            return Collections.emptyList();
        return Arrays.stream(files)
                .map(file -> file.getName().replace(".jsonl", ""))
                .collect(Collectors.toList());
    }

    @Override
    @NonNull
    public List<Message> findByConversationId(@NonNull String conversationId) {
        File file = new File(outputDir, conversationId + ".jsonl");
        try {
            return JsonlHelper.readJsonLines(file, Map.class).stream().map((Function<Map, Message>) map -> {
                String messageType = (String) map.get("messageType");
                String text = (String) map.get("text");

                return switch (MessageType.fromValue(messageType.toLowerCase())) {
                    case USER -> new UserMessage(text);
                    case ASSISTANT -> new AssistantMessage(text);
                    case SYSTEM -> new SystemMessage(text);
                    case TOOL -> new ToolResponseMessage(List.of());
                };
            }).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Failed to read chat memory", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void saveAll(@NonNull String conversationId, @NonNull List<Message> messages) {
        File file = new File(outputDir, conversationId + ".jsonl");
        try {
            JsonlHelper.overwriteJsonLines(file, messages);
        } catch (IOException e) {
            log.error("Failed to save chat memory", e);
            throw new RuntimeException("Failed to save chat memory", e);
        }
    }

    @Override
    public void deleteByConversationId(@NonNull String conversationId) {
        File file = new File(outputDir, conversationId + ".jsonl");
        JsonlHelper.deleteJsonlFile(file);
    }
}