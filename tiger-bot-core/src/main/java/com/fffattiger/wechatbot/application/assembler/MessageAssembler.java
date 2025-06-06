package com.fffattiger.wechatbot.application.assembler;


import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.message.Message;

@Mapper(componentModel = "spring")
public interface MessageAssembler {

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "chatId", source = "chat.id"),
        @Mapping(target = "sender", source = "message.sender"),
        @Mapping(target = "content", source = "message.content"),
        @Mapping(target = "time", source = "timestamp"),
        @Mapping(target = "type", source = "message.type"),
        @Mapping(target = "info", source = "message.info"),
        @Mapping(target = "senderRemark", source = "message.senderRemark")
    })
    Message toMessage(com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages.Chat.Message message, Chat chat, Long timestamp);


    default LocalDateTime map(Long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.of("+8"));
    }
}
