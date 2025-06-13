package com.fffattiger.wechatbot.infrastructure.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;

@Getter
public class WxAutoConnectedEvent extends ApplicationEvent {

    private final WxAuto wxAuto;

    public WxAutoConnectedEvent(Object source, WxAuto wxAuto) {
        super(source);
        this.wxAuto = wxAuto;
    }
    
    
}
