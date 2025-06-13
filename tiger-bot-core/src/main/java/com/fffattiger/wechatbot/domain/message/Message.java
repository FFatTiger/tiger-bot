package com.fffattiger.wechatbot.domain.message;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

import lombok.Data;

@Table("messages")
@Data
public class Message {
    @Id
    private Long id;

    /**
     * 聊天的id
     */
    private Long chatId;

    /**
     * 消息类型
     */
    private MessageType type;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 发送者
     */
    private String sender;

    /**
     * 消息信息
     */
    private List<String> info;

    /**
     * 消息时间
     */
    private LocalDateTime time;

    /**
     * 发送者备注
     */
    private String senderRemark;

    // 构造函数
    public Message() {}

    public Message(Long id, Long chatId, MessageType type, String content, String sender,
                  List<String> info, LocalDateTime time, String senderRemark) {
        this.id = id;
        this.chatId = chatId;
        this.type = type;
        this.content = content;
        this.sender = sender;
        this.info = info;
        this.time = time;
        this.senderRemark = senderRemark;
    }

    // 业务方法
    public boolean isValidMessage() {
        return content != null && !content.trim().isEmpty() &&
               sender != null && !sender.trim().isEmpty() &&
               chatId != null && type != null;
    }

    public boolean isFromUser(String username) {
        return sender != null && sender.equals(username);
    }

    public boolean containsKeyword(String keyword) {
        return content != null && content.contains(keyword);
    }

    public boolean isTextMessage() {
        return type == MessageType.FRIEND && content != null;
    }

    public String getDisplaySender() {
        if (senderRemark != null && !senderRemark.trim().isEmpty()) {
            return senderRemark;
        }
        return sender;
    }

}
