package com.fffattiger.wechatbot.infrastructure.external.wchat;

public interface MessageHandlerChain {
    
    /**
     * 处理微信消息
     * @param context 上下文
     */
    boolean handle(MessageHandlerContext context);
}
