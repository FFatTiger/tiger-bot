package com.fffattiger.wechatbot.domain.chat;

import java.util.List;

import com.fffattiger.wechatbot.domain.chat.event.MessageReceivedEvent;
import com.fffattiger.wechatbot.domain.common.AggregateRoot;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.ToString;

/**
 * 聊天群组聚合根
 */
@Entity
@Table(name = "chats")
@Getter
@ToString(callSuper = true)
@Slf4j
public class Chat extends AggregateRoot {
    
    /**
     * 聊天群组的名称，群名或者私聊名
     */
    private String name;

    /**
     * 是否为群聊
     */
    private boolean groupFlag;

    /**
     * AI配置
     */
    @Embedded
    private AiSpecification aiSpecification;
    
    /**
     * 监听器配置
     */
    @Embedded
    private ListenerConfiguration listener;
    

    // 构造函数
    protected Chat() {}

    public Chat(String name, boolean groupFlag, AiSpecification aiSpecification, ListenerConfiguration listenerConfig) {
        this.name = name;
        this.groupFlag = groupFlag;
        this.aiSpecification = aiSpecification;
        this.listener = listenerConfig != null ? listenerConfig : ListenerConfiguration.defaultConfig();
    }
    /**
     * 是否监听
     * @return
     */
    public boolean isListened() {
        return this.listener != null && this.listener.enable();
    }
    
    /**
     * 接受消息
     * @param message
     * @return
     */
    public boolean receiveMessage(Message message, String botName) {
        if (!this.isListened()) {
            return false;
        }

        return this.listener.shouldProcessMessage(message.getContent(), botName, this.groupFlag);
    }

    
}
 