package com.fffattiger.wechatbot.infrastructure.external.wxauto;

import java.io.File;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Service;

@Service
public interface WxAuto {

        /**
         * 向指定的联系人或群聊发送文本消息。
         * 
         * @param toWho 接收消息的联系人昵称、备注名或群聊名称。
         * @param text  要发送的文本内容。
         * @return 发送结果。
         */
        ResultSpecification<String> sendText(String toWho, String text);

        /**
         * 向指定的联系人或群聊发送文件。
         * 
         * @param toWho    接收文件的联系人昵称、备注名或群聊名称。
         * @param filePath 要发送的文件的本地绝对路径。
         * @return 发送结果。
         */
        ResultSpecification<String> sendFile(String toWho, String filePath);

        /**
         * 向指定的联系人或群聊发送文件。
         * 
         * @param toWho 接收文件的联系人昵称、备注名或群聊名称。
         * @param file  要发送的文件。
         * @return 发送结果。
         */
        ResultSpecification<String> sendFileByUpload(String toWho, File file);

        /**
         * 添加一个聊天对象（联系人或群聊）到监听列表，之后该对象的新消息会被推送到客户端。
         * 根据新的wxauto文档，使用nickname参数，系统会自动处理回调。
         * 
         * @param nickname 要监听的联系人昵称、备注名或群聊名称。
         * @return 添加结果。
         */
        ResultSpecification<String> addListenChat(String nickname);

        /**
         * 移除一个聊天对象的监听
         * 
         * @param nickname 要移除监听的联系人昵称、备注名或群聊名称。
         * @return 移除结果。
         */
        ResultSpecification<String> removeListenChat(String nickname);

        /**
         * 停止所有监听
         * 
         * @return 停止结果。
         */
        ResultSpecification<String> stopAllListening();

        /**
         * 开始所有监听
         *
         * @return 开始结果
         */
        ResultSpecification<String> startListening();

        /**
         * 打开一个聊天窗口。
         * 
         * @param who 要打开的聊天窗口的联系人昵称、备注名或群聊名称。
         * @return 打开结果。
         */
        ResultSpecification<String> chatWith(String who);

        /**
         * 获取当前监听的聊天对象
         * 
         * @return 当前监听的聊天对象
         */
        List<String> getListeners();

        /**
         * 初始化
         */
        void init();

        @JsonIgnoreProperties(ignoreUnknown = true)
        record WechatMessageSpecification(
                        @JsonProperty("event_type") String eventType,
                        @JsonProperty("message") String message,
                        @JsonProperty("timestamp") Long timestamp,
                        @JsonProperty("data") List<ChatSpecification> data) {
                @JsonIgnoreProperties(ignoreUnknown = true)
                public record ChatSpecification(
                                @JsonProperty("chat_name") String chatName,
                                @JsonProperty("chat_type") String chatType,
                                @JsonProperty("messages") List<MessageSpecification> messageSpecifications) {
                        @JsonIgnoreProperties(ignoreUnknown = true)
                        public record MessageSpecification(
                                        @JsonProperty("type") MessageType type,
                                        @JsonProperty("attr") MessageAttr attr,
                                        @JsonProperty("content") String content,
                                        @JsonProperty("sender") String sender,
                                        @JsonProperty("info") Object info,
                                        @JsonProperty("id") String id,
                                        @JsonProperty("hash") String hash) {
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
                        @JsonProperty("nickname") String nickname) {
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        record RemoveListenChatSpecification(
                        @JsonProperty("nickname") String nickname) {
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