package com.fffattiger.wechatbot.infrastructure.event.handlers;

import java.util.Iterator;
import java.util.List;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerChain;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;

import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("unused")
@Slf4j
public class DefaultMessageHandlerChain implements MessageHandlerChain {

    private final List<MessageHandler> handlers;
    private Iterator<MessageHandler> iterator;

    public DefaultMessageHandlerChain(List<MessageHandler> handlers) {
        this.handlers = handlers;
        this.iterator = handlers.iterator();
    }

    @Override
    public boolean handle(MessageHandlerContext context) {
        if (iterator.hasNext()) {
            MessageHandler nextHandler = iterator.next();
            String handlerName = nextHandler.getClass().getSimpleName();
            String chatName = context.currentChat() != null ? context.currentChat().chat().getName() : "未知";
            String sender = context.message() != null ? context.message().sender() : "未知";

            try {
                log.debug("执行消息处理器: handler={}, 聊天={}, 发送者={}",
                        handlerName, chatName, sender);

                long startTime = System.currentTimeMillis();
                boolean result = nextHandler.handle(context, this);
                long duration = System.currentTimeMillis() - startTime;

                log.debug("消息处理器执行完成: handler={}, 聊天={}, 发送者={}, 结果={}, 耗时={}ms",
                        handlerName, chatName, sender, result ? "已处理" : "继续传递", duration);

                return result;
            } catch (Exception e) {
                log.error("消息处理器执行异常: handler={}, 聊天={}, 发送者={}, 错误信息={}",
                        handlerName, chatName, sender, e.getMessage(), e);
                return false;
            }
        }

        log.debug("所有消息处理器已执行完毕，消息未被处理");
        return false;
    }
    
}
