package com.fffattiger.wechatbot.domain.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fffattiger.wechatbot.domain.chat.Chat;

/**
 * 聊天群组仓储接口
 */
public interface ChatRepository extends JpaRepository<Chat, Long> {

    /**
     * 获取所有监听的聊天
     * @return
     */
    List<Chat> findByListenerEnableIsTrue();

    /**
     * 根据名称获取聊天
     * @param chatName
     * @return
     */
    Optional<Chat> findByName(String name);
} 