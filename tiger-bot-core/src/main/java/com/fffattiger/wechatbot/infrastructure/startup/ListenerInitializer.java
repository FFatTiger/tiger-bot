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
public class ListenerInitializer implements OrderedInitializer {
    private final WxAuto wxAuto;
    private final ChatBotProperties chatBotProperties;

    @Override
    public void init() {
        wxAuto.init();

        log.info("获取机器人名称...");
        WxAuto.ResultSpecification<RobotNameSpecification> robotName = wxAuto.getRobotName();
        if (robotName.success()) {
            String botName = robotName.data().robotName();
            chatBotProperties.setRobotName(botName);
            log.info("机器人名称获取成功: {}", botName);
        } else {
            log.error("获取机器人名称失败: {}", robotName.message());
            throw new RuntimeException("获取机器人名称失败 message: " + robotName);
        }
        log.info("微信自动化客户端初始化完成");
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
