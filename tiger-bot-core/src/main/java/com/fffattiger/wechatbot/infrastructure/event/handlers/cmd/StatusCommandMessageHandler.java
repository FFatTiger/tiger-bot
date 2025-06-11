package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;

@Service
public class StatusCommandMessageHandler extends AbstractCommandMessageHandler {

    @Override
    public boolean canHandle(String command) {
        return command.startsWith("/status") || command.startsWith("/s");
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        // 监听对象
        String listeners = context.wx().getListeners().stream().collect(Collectors.joining(", "));
        context.wx().sendText(context.currentChat().chat().getName(), "监听对象: " + listeners);
        
        // TODO 机器人是否开启

        // TODO 今日回复

        // TODO 当前模型

        // TODO 当前角色

        // TODO 运行时间

        // TODO 当前版本
    }

    @Override
    public String description() {
        return "/status 查看状态";
    }
}