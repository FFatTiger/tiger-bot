package com.fffattiger.wechatbot.infrastructure.event;

import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.fffattiger.wechatbot.application.service.ChatApplicationService;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WxAutoConnectedListener implements ApplicationListener<WxAutoConnectedEvent> {

    @Resource
    private ChatApplicationService chatApplicationService;

    @Override
    public void onApplicationEvent(@NonNull WxAutoConnectedEvent event) {
        log.info("WeChat连接已建立，开始添加监听的聊天窗口");


        chatApplicationService.findAllListened().forEach(chat -> {
            try {
                var result = event.getWxAuto().addListenChat(chat.getName());
                if (result.success()) {
                    log.info("成功添加监听: {}", chat.getName());
                } else {
                    log.warn("添加监听失败: {}, 错误: {}", chat.getName(), result.message());
                }
            } catch (Exception e) {
                log.error("添加监听异常: {}, 错误: {}", chat.getName(), e.getMessage(), e);
            }
        });

        try {
            log.info("开始通知wx-gateway开始监听...");
            var startResult = event.getWxAuto().startListening();
            if (startResult.success()) {
                log.info("通知wx-gateway开始监听成功");
            } else {
                log.warn("通知wx-gateway开始监听失败: {}", startResult.message());
            }
        } catch (Exception e) {
            log.error("通知wx-gateway开始监听异常", e);
        }

        log.info("监听窗口添加完成");
    }
}
