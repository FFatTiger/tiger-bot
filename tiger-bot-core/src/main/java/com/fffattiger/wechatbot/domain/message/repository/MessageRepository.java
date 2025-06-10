package com.fffattiger.wechatbot.domain.message.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.fffattiger.wechatbot.domain.message.Message;

public interface MessageRepository extends CrudRepository<Message, Long>, PagingAndSortingRepository<Message, Long> {

    /**
     * 根据聊天ID查找消息
     */
    List<Message> findByChatId(Long chatId);

    /**
     * 根据聊天ID分页查找消息
     */
    Page<Message> findByChatIdOrderByTimeDesc(Long chatId, Pageable pageable);

    /**
     * 根据发送者查找消息
     */
    List<Message> findBySender(String sender);

    /**
     * 根据时间范围查找消息
     */
    @Query("SELECT * FROM messages WHERE time >= :startTime AND time <= :endTime ORDER BY time DESC")
    List<Message> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据聊天ID和时间范围查找消息
     */
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND time >= :startTime AND time <= :endTime ORDER BY time DESC")
    List<Message> findByChatIdAndTimeRange(@Param("chatId") Long chatId, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 根据内容关键词搜索消息
     */
    @Query("SELECT * FROM messages WHERE content ILIKE %:keyword% ORDER BY time DESC")
    List<Message> findByContentContaining(@Param("keyword") String keyword);

    /**
     * 根据聊天ID和内容关键词搜索消息
     */
    @Query("SELECT * FROM messages WHERE chat_id = :chatId AND content ILIKE %:keyword% ORDER BY time DESC")
    List<Message> findByChatIdAndContentContaining(@Param("chatId") Long chatId, @Param("keyword") String keyword);

    /**
     * 分页查找所有消息，按时间倒序
     */
    Page<Message> findAllByOrderByTimeDesc(Pageable pageable);

    /**
     * 统计聊天对象的消息数量
     */
    @Query("SELECT COUNT(*) FROM messages WHERE chat_id = :chatId")
    long countByChatId(@Param("chatId") Long chatId);

    /**
     * 删除指定聊天的所有消息
     */
    void deleteByChatId(Long chatId);

    /**
     * 删除指定时间之前的消息
     */
    @Query("DELETE FROM messages WHERE time < :beforeTime")
    void deleteByTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);
}
