package com.fffattiger.wechatbot.domain.command;

public record CommandArgs(
    String command,
    String[] args
) {    
}
