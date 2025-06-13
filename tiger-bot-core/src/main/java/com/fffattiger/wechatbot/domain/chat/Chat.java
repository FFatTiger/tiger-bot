package com.fffattiger.wechatbot.domain.chat;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import lombok.Data;

@Table("chats")
@Data
public class Chat {
    @Id
    private Long id;

    /**
     * 聊天的名称，群名或者私聊名
     */
    private String name;

    /**
     * 是否为群聊
     */
    private boolean groupFlag;

    /**
     * AI配置
     */
    @Embedded.Empty
    private AiSpecification aiSpecification;

    // 构造函数
    public Chat() {}

    public Chat(Long id, String name, boolean groupFlag, AiSpecification aiSpecification) {
        this.id = id;
        this.name = name;
        this.groupFlag = groupFlag;
        this.aiSpecification = aiSpecification;
    }

    // 业务方法
    public boolean canReceiveMessage() {
        return name != null && !name.trim().isEmpty();
    }

    public boolean isGroupChat() {
        return groupFlag;
    }

    public boolean hasAiConfiguration() {
        return aiSpecification != null &&
               aiSpecification.aiProviderId() != null &&
               aiSpecification.aiModelId() != null &&
               aiSpecification.aiRoleId() != null;
    }

    public void updateAiConfiguration(AiSpecification newSpecification) {
        if (newSpecification == null) {
            throw new IllegalArgumentException("AI配置不能为空");
        }
        if (newSpecification.aiProviderId() == null ||
            newSpecification.aiModelId() == null ||
            newSpecification.aiRoleId() == null) {
            throw new IllegalArgumentException("AI配置信息不完整");
        }
        this.aiSpecification = newSpecification;
    }

    public boolean containsMember(String memberName) {
        // 简单的成员检查逻辑，实际可能需要更复杂的实现
        return memberName != null && !memberName.trim().isEmpty();
    }
}
