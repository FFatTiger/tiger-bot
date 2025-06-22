package com.fffattiger.wechatbot.infrastructure.mapper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fffattiger.wechatbot.domain.chat.Message;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.MessageAttr;
import com.fffattiger.wechatbot.infrastructure.external.wxauto.WxAuto.WechatMessageSpecification.ChatSpecification.MessageSpecification;

/**
 * The interface Message mapper.
 */
@Mapper(componentModel = "spring", imports = {MessageAttr.class})
public interface MessageMapper {

    /**
     * 将外部消息DTO转换为内部领域消息实体.
     *
     * @param spec   the spec
     * @param chatId the chat id
     * @return the message
     */
    @Mapping(target = "id", ignore = true) 
    @Mapping(target = "status", ignore = true) 
    @Mapping(target = "botMessage", ignore = true)
    @Mapping(target = "chatId", source = "chatId") 
    @Mapping(target = "info", source = "spec")
    Message toMessage(MessageSpecification spec, Long chatId, Long time);

    
    default String mapInfo(MessageSpecification spec) {
        if (spec.info() == null) {
            return null;
        }
        
        try {
            return new ObjectMapper().writeValueAsString(spec.info());
        } catch (JsonProcessingException e) {
            // 如果转换失败，返回toString结果
            return spec.info().toString();
        }
    }

    default LocalDateTime mapTime(Long time) {
        return LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.of("+8"));
    }
} 