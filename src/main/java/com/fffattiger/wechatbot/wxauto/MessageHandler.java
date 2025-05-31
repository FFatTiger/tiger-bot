package com.fffattiger.wechatbot.wxauto;

import java.util.List;

import org.springframework.core.Ordered;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息回调接口
 */
public interface MessageHandler extends Ordered {

    /**
     * 处理微信消息
     * 
     * @param context 上下文
     * @param chain   责任链传递
     * @return 处理结果
     */
    boolean handle(MessageHandlerContext context, MessageHandlerChain chain);

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record BatchedSanitizedWechatMessages(
            @JsonProperty("event_type") String eventType,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") Long timestamp,
            @JsonProperty("data") List<Chat> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Chat(
                @JsonProperty("chat_name") String chatName,
                @JsonProperty("messages") List<Message> messages) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public record Message(
                    @JsonProperty("type") MessageType type,
                    @JsonProperty("content") String content,
                    @JsonProperty("sender") String sender,
                    @JsonProperty("info") List<String> info,
                    @JsonProperty("id") String id,
                    @JsonProperty("time") String time,
                    @JsonProperty("sender_remark") String senderRemark) {
            };
        };
    };

    public record ApiResponse<T>(
            boolean success,
            String message,
            String requestId,
            T data) {
    };

    public record SseEvent(
            @JsonProperty("event_type") String eventType,
            Object data,
            Long timestamp) {
    };


    public record SendTextRequest(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("text_content") String textContent) {
    };

    public record SendFileByPathRequest(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("filepath") String filepath) {
    };

    public record SendFileByUrlRequest(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("file_url") String fileUrl,
            @JsonProperty("filename") String filename) {
    };

    public record AddListenChatRequest(
            String who,
            boolean savepic,
            boolean savevoice,
            boolean parseLinks) {
    };

    public record ChatWithRequest(
            String who) {
    };

    public record VoiceCallRequest(
            @JsonProperty("user_id") String userId) {
    };

    public record RobotNameResponse(
            @JsonProperty("robot_name") String robotName) {
    };
}