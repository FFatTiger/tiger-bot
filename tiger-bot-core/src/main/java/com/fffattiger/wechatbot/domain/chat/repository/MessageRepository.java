package com.fffattiger.wechatbot.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fffattiger.wechatbot.domain.chat.ChatMessage;

@Repository
public interface MessageRepository extends CrudRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatIdOrderByTimeAsc(Long chatId);

    List<ChatMessage> findByChatIdAndTimeBetweenOrderByTimeAsc(Long chatId, LocalDateTime startTime, LocalDateTime endTime);
} 