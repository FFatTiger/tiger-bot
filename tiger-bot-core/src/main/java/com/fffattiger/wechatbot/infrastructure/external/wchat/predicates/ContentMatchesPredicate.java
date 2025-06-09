package com.fffattiger.wechatbot.infrastructure.external.wchat.predicates;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessagePredicate;

/**
 * 内容匹配谓词
 * 用于判断消息内容是否匹配指定的模式
 */
public class ContentMatchesPredicate implements MessagePredicate {
    
    private final Pattern pattern;
    private final String patternString;
    
    public ContentMatchesPredicate(String regex) {
        this.patternString = regex;
        this.pattern = Pattern.compile(regex);
    }
    
    @Override
    public boolean test(MessageHandlerContext context) {
        String content = context.cleanContent();
        if (!StringUtils.hasLength(content)) {
            return false;
        }
        return pattern.matcher(content).matches();
    }
    
    @Override
    public String toString() {
        return "ContentMatches('" + patternString + "')";
    }
}
