package com.fffattiger.wechatbot.shared.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WeChatMessageProcessor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String processMessages(String jsonData) {
        try {
            List<WeChatMessage> messages = objectMapper.readValue(jsonData,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, WeChatMessage.class));

            return messages.stream()
                    .map(WeChatMessageProcessor::formatMessage)
                    .filter(msg -> !msg.isEmpty())
                    .collect(Collectors.joining("\n\n"));
        } catch (Exception e) {
            e.printStackTrace();
            return "处理消息时发生错误: " + e.getMessage();
        }
    }

    private static String formatMessage(WeChatMessage msg) {
        String timeStr = formatTime(msg.getTime());
        String senderName = msg.getSenderName();
        String content = processMessageContent(msg);

        if (content.isEmpty()) {
            return "";
        }

        return String.format("%s %s\n%s", senderName, timeStr, content);
    }

    private static String formatTime(String timeStr) {
        try {
            // 解析时间戳或ISO时间字符串
            if (timeStr.matches("\\d+")) {
                long timestamp = Long.parseLong(timeStr);
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        ZoneId.systemDefault());
                return dateTime.format(TIME_FORMATTER);
            } else {
                LocalDateTime dateTime = LocalDateTime.parse(timeStr.replace("+08:00", ""));
                return dateTime.format(TIME_FORMATTER);
            }
        } catch (Exception e) {
            return timeStr;
        }
    }

    private static String processMessageContent(WeChatMessage msg) {
        int type = msg.getType();
        int subType = msg.getSubType();

        switch (type) {
            case 1: // 普通文本消息
                return msg.getContent();

            case 3: // 图片消息
                return "[图片]";

            case 34: // 语音消息
                return "[语音]";

            case 43: // 视频消息
                return "[视频]";

            case 47: // 动画表情
                return "[动画表情]";

            case 48: // 位置分享
                return processLocationMessage(msg);

            case 49: // 各种应用消息
                return processAppMessage(msg, subType);

            case 10000: // 系统消息
                return "[系统消息]";

            default:
                return "[未知消息类型]";
        }
    }

    private static String processLocationMessage(WeChatMessage msg) {
        try {
            JsonNode locationNode = objectMapper.readTree(msg.getContent());
            String label = locationNode.path("location").path("@label").asText("");
            String poiname = locationNode.path("location").path("@poiname").asText("");

            if (!poiname.isEmpty()) {
                return "[位置] " + poiname;
            } else if (!label.isEmpty()) {
                return "[位置] " + label;
            } else {
                return "[位置分享]";
            }
        } catch (Exception e) {
            return "[位置分享]";
        }
    }

    private static String processAppMessage(WeChatMessage msg, int subType) {
        JsonNode contents = msg.getContents();

        switch (subType) {
            case 3: // 音乐分享
                return processMusicShare(contents);

            case 4:
            case 5: // 链接分享
                return processLinkShare(contents);

            case 8: // 表情包
                return "[表情包]";

            case 19: // 聊天记录分享
                return processChatRecordShare(contents);

            case 51: // 微信视频号分享
                return processFinderShare(contents);

            case 57: // 回复消息
                return processReplyMessage(msg, contents);

            default:
                return processGenericAppMessage(contents);
        }
    }

    private static String processMusicShare(JsonNode contents) {
        if (contents != null && contents.has("title")) {
            String title = contents.path("title").asText("");
            return "[音乐|" + title + "]";
        }
        return "[音乐分享]";
    }

    private static String processLinkShare(JsonNode contents) {
        if (contents != null && contents.has("title")) {
            String title = contents.path("title").asText("");
            return "[链接|" + title + "]";
        }
        return "[链接分享]";
    }

    private static String processChatRecordShare(JsonNode contents) {
        if (contents != null && contents.has("title")) {
            String title = contents.path("title").asText("");
            return "[聊天记录] " + title;
        }
        return "[聊天记录]";
    }

    private static String processFinderShare(JsonNode contents) {
        if (contents != null && contents.has("title")) {
            String title = contents.path("title").asText("");
            return "[视频号] " + title;
        }
        return "[视频号分享]";
    }

    private static String processReplyMessage(WeChatMessage msg, JsonNode contents) {
        StringBuilder result = new StringBuilder();

        // 处理引用的消息
        if (contents != null && contents.has("refer")) {
            JsonNode refer = contents.path("refer");
            String referSender = refer.path("senderName").asText("");
            String referTime = refer.path("time").asText("");
            String referContent = refer.path("content").asText("");

            result.append(String.format("> %s %s\n", referSender, formatTime(referTime)));
            result.append(String.format("> %s\n", referContent));
        }

        // 处理当前消息内容
        String currentContent = msg.getContent();
        if (currentContent != null && !currentContent.trim().isEmpty()) {
            result.append(currentContent);
        }

        return result.toString();
    }

    private static String processGenericAppMessage(JsonNode contents) {
        if (contents != null && contents.has("title")) {
            String title = contents.path("title").asText("");
            if (!title.isEmpty()) {
                return "[分享] " + title;
            }
        }
        return "[应用消息]";
    }

    // 测试方法
}