package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.dto.MessageProcessingData;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.Listener;
import com.fffattiger.wechatbot.domain.listener.repository.ChatCommandAuthRepository;
import com.fffattiger.wechatbot.domain.listener.repository.ListenerRepository;
import com.fffattiger.wechatbot.domain.listener.service.ListenerDomainService;
import com.fffattiger.wechatbot.domain.user.User;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 监听器应用服务
 * 专门负责监听器相关的应用逻辑
 */
@Service
@Slf4j
public class ListenerApplicationService {

    @Resource
    private ListenerRepository listenerRepository;

    @Resource
    private ChatCommandAuthRepository chatCommandAuthRepository;

    @Resource
    private ListenerDomainService listenerDomainService;

    @Resource
    private ChatApplicationService chatApplicationService;

    @Resource
    private CommandApplicationService commandApplicationService;

    @Resource
    private UserApplicationService userApplicationService;

    public MessageProcessingData getMessageProcessingData(String chatName) {
        log.debug("查找聊天的消息处理数据: 聊天名称={}", chatName);

        // 使用聊天应用服务获取聊天对象
        Chat chat = chatApplicationService.getChatByName(chatName);
        if (chat == null) {
            log.warn("未找到聊天对象: 聊天名称={}", chatName);
            return null;
        }

        if (!chat.canReceiveMessage()) {
            log.warn("聊天对象无法接收消息: 聊天名称={}, 聊天ID={}", chatName, chat.getId());
            return null;
        }

        Listener listener = listenerRepository.findByChatId(chat.getId());
        if (listener == null) {
            log.warn("未找到监听器配置: 聊天名称={}, 聊天ID={}", chatName, chat.getId());
            return null;
        }

        // 使用领域对象验证监听器配置
        if (!listener.isValidConfiguration()) {
            log.warn("监听器配置无效: 聊天名称={}, 监听器ID={}", chatName, listener.getId());
            return null;
        }

        log.debug("找到有效的监听器配置: 聊天名称={}, 监听器ID={}", chatName, listener.getId());
        return buildListenerData(listener, chat);
    }

    private MessageProcessingData buildListenerData(Listener listener, Chat chat) {
        log.debug("构建监听器数据: 聊天={}, 监听器ID={}", chat.getName(), listener.getId());

        List<ChatCommandAuth> commandAuths = chatCommandAuthRepository.findByChatId(chat.getId());
        log.debug("找到命令权限配置: 聊天={}, 权限数量={}", chat.getName(), commandAuths.size());

        if (commandAuths.isEmpty()) {
            log.debug("无命令权限配置，返回空权限列表: 聊天={}", chat.getName());
            return new MessageProcessingData(listener, chat, List.of());
        }

        List<MessageProcessingData.ChatCommandAuthWithCommandAndUser> validCommandAuths =
                buildValidCommandAuths(commandAuths, chat, listener);

        log.debug("监听器数据构建完成: 聊天={}, 有效权限数量={}",
                chat.getName(), validCommandAuths.size());
        return new MessageProcessingData(listener, chat, validCommandAuths);
    }

    private List<MessageProcessingData.ChatCommandAuthWithCommandAndUser> buildValidCommandAuths(
            List<ChatCommandAuth> commandAuths, Chat chat, Listener listener) {

        List<MessageProcessingData.ChatCommandAuthWithCommandAndUser> result = new ArrayList<>();

        for (ChatCommandAuth commandAuth : commandAuths) {

            if (!commandAuth.isValidAuth()) {
                log.warn("Invalid command auth found: {}", commandAuth.getId());
                continue;
            }

            try {
                Command command = commandApplicationService.getCommandById(commandAuth.getCommandId());

                if (!command.isValidCommand()) {
                    log.warn("Invalid command found: {}", command.getId());
                    continue;
                }

                User user = null;
                if (!commandAuth.isGlobalPermission()) {
                    user = userApplicationService.getUserById(commandAuth.getUserId());

                    if (!user.isValidUser()) {
                        log.warn("Invalid user found: {}", user.getId());
                        continue;
                    }
                }

                if (listenerDomainService.validateCommandPermission(listener, command, user, chat)) {
                    result.add(new MessageProcessingData.ChatCommandAuthWithCommandAndUser(commandAuth, command, user));
                } else {
                    log.warn("Command permission validation failed for command: {}", command.getId());
                }

            } catch (Exception e) {
                log.error("Error processing command auth: {}", commandAuth.getId(), e);
                // 继续处理其他权限，不因单个错误中断
            }
        }

        return result;
    }

    public List<MessageProcessingData> findAll() {
        Iterable<Listener> all = listenerRepository.findAll();
        List<MessageProcessingData> listenerAggregates = new ArrayList<>();

        for (Listener listener : all) {
            try {
                if (!listener.isValidConfiguration()) {
                    log.warn("Skipping invalid listener configuration: {}", listener.getId());
                    continue;
                }

                Chat chat = chatApplicationService.getChatById(listener.getChatId());

                if (!chat.canReceiveMessage()) {
                    log.warn("Skipping chat that cannot receive messages: {}", chat.getId());
                    continue;
                }

                MessageProcessingData aggregate = buildListenerData(listener, chat);
                listenerAggregates.add(aggregate);

            } catch (Exception e) {
                log.error("Error building listener aggregate for listener: {}", listener.getId(), e);
                // 继续处理其他监听器，不因单个错误中断
            }
        }

        return listenerAggregates;
    }

    /**
     * 更新监听器配置
     */
    public void updateListener(Long listenerId, boolean atReplyEnable, boolean keywordReplyEnable,
                              boolean savePic, boolean saveVoice, boolean parseLinks, List<String> keywordReply) {
        Listener existingListener = listenerRepository.findById(listenerId)
                .orElseThrow(() -> new RuntimeException("Listener not found"));

        Listener updatedListener = new Listener(
                existingListener.getId(),
                existingListener.getChatId(),
                atReplyEnable,
                keywordReplyEnable,
                savePic,
                saveVoice,
                parseLinks,
                keywordReply
        );

        listenerRepository.save(updatedListener);
        
    }

    /**
     * 创建新的监听器
     */
    public void createListener(Long chatId, Long creatorId, boolean atReplyEnable, boolean keywordReplyEnable,
                              boolean savePic, boolean saveVoice, boolean parseLinks, List<String> keywordReply) {
        // 使用应用服务加载聚合
        Chat chat = chatApplicationService.getChatById(chatId);
        User creator = userApplicationService.getUserById(creatorId);

        // 使用领域服务验证业务规则
        if (!listenerDomainService.canCreateListener(chat, creator)) {
            throw new RuntimeException("无权限创建监听器");
        }

        Listener newListener = new Listener(
                null,
                chatId,
                atReplyEnable,
                keywordReplyEnable,
                savePic,
                saveVoice,
                parseLinks,
                keywordReply
        );

        listenerRepository.save(newListener);
        
    }

    /**
     * 删除监听器
     */
    public void deleteListener(Long listenerId) {
        listenerRepository.deleteById(listenerId);
        
    }
}
