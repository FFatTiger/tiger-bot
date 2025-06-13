package com.fffattiger.wechatbot.infrastructure.external.wxauto.predicates;

import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessagePredicate;

/**
 * 命令前缀谓词
 * 用于判断消息是否以指定前缀开头
 */
public class CommandPrefixPredicate implements MessagePredicate {
    
    private final String prefix;
    
    public CommandPrefixPredicate(String prefix) {
        this.prefix = prefix;
    }
    
    @Override
    public boolean test(MessageHandlerContext context) {
        String content = context.cleanContent();
        return StringUtils.hasLength(content) && content.startsWith(prefix);
    }
    
    @Override
    public String toString() {
        return "CommandPrefix('" + prefix + "')";
    }
}
