package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.MessageHandlerContext;

@Service
public class StatusCommandMessageHandler implements CommandMessageHandlerExtension {

    @Override
    public String getCommandName() {
        return "状态";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        // // 监听对象
        // String listeners = context.get("wxAuto").getListeners().stream().collect(Collectors.joining(", "));
        // context.replyText(context.getMessage().chatName(), "监听对象: " + listeners);
        
        // TODO 机器人是否开启

        // TODO 今日回复

        // TODO 当前模型

        // TODO 当前角色

        // TODO 运行时间

        // TODO 当前版本
    }

    @Override
    public String getDescription() {
        return "/status 查看状态";
    }
}