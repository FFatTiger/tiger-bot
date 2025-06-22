package com.fffattiger.wechatbot.domain.chat.repository;

import com.fffattiger.wechatbot.domain.chat.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatIdOrderByTimeAsc(Long chatId);
} 