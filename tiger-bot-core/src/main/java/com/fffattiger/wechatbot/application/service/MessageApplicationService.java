package com.fffattiger.wechatbot.application.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.message.Message;
import com.fffattiger.wechatbot.domain.message.repository.MessageRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息应用服务
 * 负责消息相关的业务逻辑处理
 */
@Service
@Slf4j
public class MessageApplicationService {

    @Resource
    private MessageRepository messageRepository;

    /**
     * 保存消息
     */
    public void save(Message message) {
        messageRepository.save(message);
    }

    /**
     * 获取所有消息（分页）
     */
    public Page<Message> getAllMessages(Pageable pageable) {
        return messageRepository.findAllByOrderByTimeDesc(pageable);
    }

    /**
     * 根据ID获取消息
     */
    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }

    /**
     * 根据聊天ID获取消息列表
     */
    public List<Message> getMessagesByChatId(Long chatId) {
        return messageRepository.findByChatId(chatId);
    }

    /**
     * 根据聊天ID获取消息列表（分页）
     */
    public Page<Message> getMessagesByChatId(Long chatId, Pageable pageable) {
        return messageRepository.findByChatIdOrderByTimeDesc(chatId, pageable);
    }

    /**
     * 根据发送者获取消息列表
     */
    public List<Message> getMessagesBySender(String sender) {
        return messageRepository.findBySender(sender);
    }

    /**
     * 根据时间范围获取消息列表
     */
    public List<Message> getMessagesByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return messageRepository.findByTimeRange(startTime, endTime);
    }

    /**
     * 根据聊天ID和时间范围获取消息列表
     */
    public List<Message> getMessagesByChatIdAndTimeRange(Long chatId, LocalDateTime startTime, LocalDateTime endTime) {
        return messageRepository.findByChatIdAndTimeRange(chatId, startTime, endTime);
    }

    /**
     * 根据内容关键词搜索消息
     */
    public List<Message> searchMessagesByContent(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return messageRepository.findByContentContaining(keyword.trim());
    }

    /**
     * 根据聊天ID和内容关键词搜索消息
     */
    public List<Message> searchMessagesByChatIdAndContent(Long chatId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getMessagesByChatId(chatId);
        }
        return messageRepository.findByChatIdAndContentContaining(chatId, keyword.trim());
    }

    /**
     * 统计聊天对象的消息数量
     */
    public long countMessagesByChatId(Long chatId) {
        return messageRepository.countByChatId(chatId);
    }

    /**
     * 删除消息
     */
    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new RuntimeException("消息不存在: " + id);
        }
        messageRepository.deleteById(id);
        
    }

    /**
     * 删除聊天对象的所有消息
     */
    public void deleteMessagesByChatId(Long chatId) {
        long count = countMessagesByChatId(chatId);
        messageRepository.deleteByChatId(chatId);
        
    }

    /**
     * 清理指定时间之前的历史消息
     */
    public void cleanupMessagesBeforeTime(LocalDateTime beforeTime) {
        messageRepository.deleteByTimeBefore(beforeTime);
        
    }

    /**
     * 获取消息统计信息
     */
    public MessageStatistics getMessageStatistics() {
        List<Message> allMessages = new ArrayList<>();
        messageRepository.findAll().forEach(allMessages::add);

        long totalCount = allMessages.size();
        long todayCount = allMessages.stream()
                .filter(msg -> msg.getTime().toLocalDate().equals(LocalDateTime.now().toLocalDate()))
                .count();

        return new MessageStatistics(totalCount, todayCount);
    }

    /**
     * 消息统计信息记录
     */
    public record MessageStatistics(long totalCount, long todayCount) {}
}
