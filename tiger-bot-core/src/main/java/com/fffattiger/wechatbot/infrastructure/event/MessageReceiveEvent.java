package com.fffattiger.wechatbot.infrastructure.event;

import org.springframework.context.ApplicationEvent;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto.WechatMessageSpecification;

public class MessageReceiveEvent extends ApplicationEvent {

    private final WechatMessageSpecification message;

    private final WxAuto wxAuto;

    public MessageReceiveEvent(Object source, WechatMessageSpecification message, WxAuto wxAuto) {
        super(source);
        this.message = message;
        this.wxAuto = wxAuto;
    }

    public WechatMessageSpecification getMessage() {
        return message;
    }

    public WxAuto getWxAuto() {
        return wxAuto;
    }
    
}
