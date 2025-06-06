package com.fffattiger.wechatbot.infrastructure.external.wchat;

import java.util.List;

import org.springframework.core.Ordered;
import org.springframework.data.relational.core.mapping.Table;

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
    record BatchedSanitizedWechatMessages(
            @JsonProperty("event_type") String eventType,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") Long timestamp,
            @JsonProperty("data") List<Chat> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record Chat(
                @JsonProperty("chat_name") String chatName,
                @JsonProperty("messages") List<Message> messages) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            @Table("messages") 
            public record Message(
                    @JsonProperty("type") MessageType type,
                    @JsonProperty("content") String content,
                    @JsonProperty("sender") String sender,
                    @JsonProperty("info") List<String> info,
                    @JsonProperty("id") String id,
                    @JsonProperty("time") String time,
                    @JsonProperty("sender_remark") String senderRemark
                    ) {
            }
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record Result<T>(
            boolean success,
            String message,
            String requestId,
            T data) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record SendTextRequest(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("text_content") String textContent) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record SendFileByPathRequest(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("filepath") String filepath) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record SendFileByUrlRequest(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("file_url") String fileUrl,
            @JsonProperty("filename") String filename) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record AddListenChatRequest(
            String who,
            @JsonProperty("savepic") boolean savePic,
            @JsonProperty("savevoice") boolean saveVoice,
            @JsonProperty("parseLinks") boolean parseLinks) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatWithRequest(
            String who) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record VoiceCallRequest(
            @JsonProperty("user_id") String userId) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record RobotNameResponse(
            @JsonProperty("robot_name") String robotName) {
    }


}