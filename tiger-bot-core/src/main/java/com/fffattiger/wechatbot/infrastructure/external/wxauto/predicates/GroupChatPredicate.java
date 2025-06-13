package com.fffattiger.wechatbot.infrastructure.external.wxauto.predicates;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessagePredicate;

/**
 * 群聊谓词
 * 用于判断消息是否来自群聊
 */
public class GroupChatPredicate implements MessagePredicate {
    
    @Override
    public boolean test(MessageHandlerContext context) {
        if (context.currentChat() == null || context.currentChat().chat() == null) {
            return false;
        }
        return context.currentChat().chat().isGroupChat();
    }
    
    @Override
    public String toString() {
        return "GroupChat()";
    }
}
