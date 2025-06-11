package com.fffattiger.wechatbot.infrastructure.event;

import org.springframework.context.ApplicationEvent;

import com.fffattiger.wechatbot.infrastructure.external.wchat.WxAuto;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages;

public class MessageReceiveEvent extends ApplicationEvent {

    private final BatchedSanitizedWechatMessages message;

    private final WxAuto wxAuto;

    public MessageReceiveEvent(Object source, BatchedSanitizedWechatMessages message, WxAuto wxAuto) {
        super(source);
        this.message = message;
        this.wxAuto = wxAuto;
    }

    public BatchedSanitizedWechatMessages getMessage() {
        return message;
    }

    public WxAuto getWxAuto() {
        return wxAuto;
    }
    
}
