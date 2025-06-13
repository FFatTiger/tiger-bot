package com.fffattiger.wechatbot.infrastructure.external.wxauto.predicates;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessagePredicate;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageType;

/**
 * 消息类型谓词
 * 用于判断消息是否为指定类型
 */
public class MessageTypePredicate implements MessagePredicate {
    
    private final MessageType messageType;
    
    public MessageTypePredicate(MessageType messageType) {
        this.messageType = messageType;
    }
    
    @Override
    public boolean test(MessageHandlerContext context) {
        if (context.message() == null || context.message().type() == null) {
            return false;
        }
        return context.message().type().equals(messageType);
    }
    
    @Override
    public String toString() {
        return "MessageType(" + messageType + ")";
    }
}
