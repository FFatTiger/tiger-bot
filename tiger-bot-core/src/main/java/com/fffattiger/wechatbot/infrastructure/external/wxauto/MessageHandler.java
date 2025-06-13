package com.fffattiger.wechatbot.infrastructure.external.wxauto;

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
     * 获取处理条件谓词（新的谓词模式，可选实现）
     * @return 消息谓词，默认返回null表示不使用谓词模式
     */
    default MessagePredicate getPredicate() {
        return null;
    }

    /**
     * 处理消息（不再需要条件判断，新的谓词模式，可选实现）
     * @param context 上下文
     * @param chain 责任链传递
     * @return 处理结果
     */
    default boolean doHandle(MessageHandlerContext context, MessageHandlerChain chain) {
        // 默认实现，子类可以重写
        return chain.handle(context);
    }

    /**
     * 处理微信消息
     *
     * @param context 上下文
     * @param chain   责任链传递
     * @return 处理结果
     */
    boolean handle(MessageHandlerContext context, MessageHandlerChain chain);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record WechatMessageSpecification(
            @JsonProperty("event_type") String eventType,
            @JsonProperty("message") String message,
            @JsonProperty("timestamp") Long timestamp,
            @JsonProperty("data") List<ChatSpecification> data) {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public record ChatSpecification(
                @JsonProperty("chat_name") String chatName,
                @JsonProperty("messages") List<MessageSpecification> messageSpecifications) {
            @JsonIgnoreProperties(ignoreUnknown = true)
            @Table("messages") 
            public record MessageSpecification(
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
    record ResultSpecification<T>(
            boolean success,
            String message,
            String requestId,
            T data) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record SendTextSpecification(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("text_content") String textContent) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record SendFileByPathSpecification(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("filepath") String filepath) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record SendFileByUrlSpecification(
            @JsonProperty("to_who") String toWho,
            @JsonProperty("file_url") String fileUrl,
            @JsonProperty("filename") String filename) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record AddListenChatSpecification(
            String who,
            @JsonProperty("savepic") boolean savePic,
            @JsonProperty("savevoice") boolean saveVoice,
            @JsonProperty("parseLinks") boolean parseLinks) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record ChatWithSpecification(
            String who) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record VoiceCallSpecification(
            @JsonProperty("user_id") String userId) {
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    record RobotNameSpecification(
            @JsonProperty("robot_name") String robotName) {
    }


}