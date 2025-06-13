package com.fffattiger.wechatbot.infrastructure.external.wxauto.predicates;

import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessagePredicate;

/**
 * 有内容谓词
 * 用于判断消息是否有有效内容
 */
public class HasContentPredicate implements MessagePredicate {
    
    @Override
    public boolean test(MessageHandlerContext context) {
        return StringUtils.hasLength(context.cleanContent());
    }
    
    @Override
    public String toString() {
        return "HasContent()";
    }
}
