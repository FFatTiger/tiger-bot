package com.fffattiger.wechatbot.application.assembler;


import java.time.LocalDateTime;
import java.time.ZoneOffset;

import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageHandler;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.message.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface MessageAssembler {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "chatId", source = "chat.id"),
        @Mapping(target = "sender", source = "messageSpecification.sender"),
        @Mapping(target = "content", source = "messageSpecification.content"),
        @Mapping(target = "time", source = "timestamp"),
        @Mapping(target = "type", source = "messageSpecification.type"),
        @Mapping(target = "info", source = "messageSpecification.info"),
        @Mapping(target = "senderRemark", source = "messageSpecification.senderRemark")
    })
    Message toMessage(MessageHandler.WechatMessageSpecification.ChatSpecification.MessageSpecification messageSpecification, Chat chat, Long timestamp);


    default LocalDateTime map(Long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.of("+8"));
    }
}
