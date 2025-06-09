package com.fffattiger.wechatbot.infrastructure.external.wchat.predicates;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessagePredicate;

/**
 * 发送者谓词
 * 用于判断消息是否来自指定发送者
 */
public class SenderPredicate implements MessagePredicate {
    
    private final String sender;
    
    public SenderPredicate(String sender) {
        this.sender = sender;
    }
    
    @Override
    public boolean test(MessageHandlerContext context) {
        if (context.message() == null || context.message().sender() == null) {
            return false;
        }
        return sender.equals(context.message().sender());
    }
    
    @Override
    public String toString() {
        return "Sender('" + sender + "')";
    }
}
