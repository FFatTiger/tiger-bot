package com.fffattiger.wechatbot.api;

import org.pf4j.ExtensionPoint;

public interface MessageHandlerExtension extends ExtensionPoint, Ordered {

    /**
     * 处理一个传入的消息。
     * <p>
     * 实现者应在此方法内部首先判断消息是否与自己相关。
     * 如果不相关，应立即返回 {@code false}，以便执行链继续。
     * 如果相关，则执行相应逻辑，并根据该逻辑是否应"终结"整个处理流程，返回 {@code true} 或 {@code false}。
     *
     * @param context 消息上下文，提供访问消息内容和执行回复等操作的安全API。
     * @return {@code true} 表示消息已被当前处理器完全处理，应中断执行链；
     *         {@code false} 表示执行链应继续处理下一个处理器。
     */
    boolean handle(MessageHandlerContext context);

    /**
     * 定义处理器的执行顺序。
     * <p>
     * 拥有较小值的处理器会更早被执行。
     *
     * @return order 值，默认为 1000。
     */
    default int getOrder() {
        return 1000;
    }
}