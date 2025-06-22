package com.fffattiger.wechatbot.infrastructure.startup;

import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto.RobotNameSpecification;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ListenerInitializer implements Initializer.OrderedInitializer {
    private final WxAuto wxAuto;
    private final ChatBotProperties chatBotProperties;

    @Override
    public void init() {
        wxAuto.init();

    }

    @Override
    public int getOrder() {
        return 0;
    }
}
