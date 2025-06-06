package com.fffattiger.wechatbot.domain.chat.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.chat.Chat;

public interface ChatRepository extends CrudRepository<Chat, Long> {

    Optional<Chat> findByName(String chatName);

}
