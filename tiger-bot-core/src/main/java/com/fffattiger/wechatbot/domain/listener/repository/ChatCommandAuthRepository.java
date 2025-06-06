package com.fffattiger.wechatbot.domain.listener.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;

public interface ChatCommandAuthRepository extends CrudRepository<ChatCommandAuth, Long> {

    List<ChatCommandAuth> findByChatId(Long chatId);

    List<ChatCommandAuth> findByChatIdAndCommandId(Long chatId, Long commandId);
}
