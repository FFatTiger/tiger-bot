package com.fffattiger.wechatbot.management.application.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

/**
 * 消息记录DTO
 * 用于管理界面的消息记录数据传输
 */
public record MessageRecordDto(
    Long id,
    Long chatId,
    String chatName,
    MessageType type,
    String content,
    String sender,
    String senderRemark,
    List<String> info,
    LocalDateTime time,
    String formattedTime,
    String displaySender,
    String typeDisplay,
    boolean isTextMessage
) {

    /**
     * 获取消息类型显示文本
     */
    public String getMessageTypeDisplay() {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case FRIEND -> "好友消息";
            case SELF -> "自己消息";
            case SYS -> "系统消息";
            case TIME -> "时间消息";
            case RECALL -> "撤回消息";
            case UNKNOWN -> "未知消息";
            default -> type.name();
        };
    }

    /**
     * 获取格式化的时间字符串
     */
    public String getFormattedTimeString() {
        if (time == null) {
            return "";
        }
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取显示用的发送者名称
     */
    public String getDisplaySenderName() {
        if (senderRemark != null && !senderRemark.trim().isEmpty()) {
            return senderRemark;
        }
        return sender != null ? sender : "未知";
    }

    /**
     * 获取截断的消息内容（用于列表显示）
     */
    public String getTruncatedContent(int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    /**
     * 检查是否包含关键词
     */
    public boolean containsKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String lowerKeyword = keyword.toLowerCase();
        return (content != null && content.toLowerCase().contains(lowerKeyword)) ||
               (sender != null && sender.toLowerCase().contains(lowerKeyword)) ||
               (senderRemark != null && senderRemark.toLowerCase().contains(lowerKeyword));
    }

    /**
     * 检查是否在指定时间范围内
     */
    public boolean isInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (time == null) {
            return false;
        }
        if (startTime != null && time.isBefore(startTime)) {
            return false;
        }
        if (endTime != null && time.isAfter(endTime)) {
            return false;
        }
        return true;
    }

    /**
     * 创建用于搜索的DTO
     */
    public static MessageRecordDto forSearch(Long chatId, String keyword, 
                                           LocalDateTime startTime, LocalDateTime endTime) {
        return new MessageRecordDto(null, chatId, null, null, keyword, null, null, null,
                                  startTime, null, null, null, false);
    }
}
