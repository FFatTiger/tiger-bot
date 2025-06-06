package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.chat.repository.ChatRepository;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.listener.Listener;
import com.fffattiger.wechatbot.domain.listener.repository.ChatCommandAuthRepository;
import com.fffattiger.wechatbot.domain.listener.repository.ListenerRepository;
import com.fffattiger.wechatbot.domain.user.User;
import com.fffattiger.wechatbot.domain.user.repository.UserRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ListenerApplicationService {

    @Resource
    private ListenerRepository listenerRepository;

    @Resource
    private ChatRepository chatRepository;

    @Resource
    private ChatCommandAuthRepository chatCommandAuthRepository;

    @Resource
    private CommandRepository commandRepository;

    @Resource
    private UserRepository userRepository;

    public ListenerAggregate getListenerAggregate(String chatName) {
        Chat chat = chatRepository.findByName(chatName).orElseThrow(() -> new RuntimeException("Chat not found"));
        Listener listener = listenerRepository.findByChatId(chat.id());
        return getListenerAggregate(listener);
    }

    private ListenerAggregate getListenerAggregate(Listener listener) {
        Chat chat = chatRepository.findById(listener.chatId())
                .orElseThrow(() -> new RuntimeException("Chat not found"));
        List<ChatCommandAuth> commandAuths = chatCommandAuthRepository.findByChatId(chat.id());
        List<ListenerAggregate.ChatCommandAuthWithCommandAndUser> commandAuthsWithCommandAndUser = new ArrayList<>();
        for (ChatCommandAuth commandAuth : commandAuths) {
            Command command = commandRepository.findById(commandAuth.commandId())
                    .orElseThrow(() -> new RuntimeException("Command not found"));
            User user = userRepository.findById(commandAuth.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            commandAuthsWithCommandAndUser
                    .add(new ListenerAggregate.ChatCommandAuthWithCommandAndUser(commandAuth, command, user));
        }
        return new ListenerAggregate(listener, chat, commandAuthsWithCommandAndUser);
    }

    public List<ListenerAggregate> findAll() {
        Iterable<Listener> all = listenerRepository.findAll();
        List<ListenerAggregate> listenerAggregates = new ArrayList<>();
        all.forEach(listener -> {
            listenerAggregates.add(getListenerAggregate(listener));
        });
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
                existingListener.id(),
                existingListener.chatId(),
                atReplyEnable,
                keywordReplyEnable,
                savePic,
                saveVoice,
                parseLinks,
                keywordReply
        );

        listenerRepository.save(updatedListener);
        log.info("更新监听器配置: {}", updatedListener);
    }

    /**
     * 创建新的监听器
     */
    public void createListener(Long chatId, boolean atReplyEnable, boolean keywordReplyEnable,
                              boolean savePic, boolean saveVoice, boolean parseLinks, List<String> keywordReply) {
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
        log.info("创建新监听器: {}", newListener);
    }

    /**
     * 删除监听器
     */
    public void deleteListener(Long listenerId) {
        listenerRepository.deleteById(listenerId);
        log.info("删除监听器: {}", listenerId);
    }

    /**
     * 获取所有聊天对象
     */
    public List<Chat> getAllChats() {
        List<Chat> chats = new ArrayList<>();
        chatRepository.findAll().forEach(chats::add);
        return chats;
    }

    /**
     * 获取所有命令
     */
    public List<Command> getAllCommands() {
        List<Command> commands = new ArrayList<>();
        commandRepository.findAll().forEach(commands::add);
        return commands;
    }

    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    /**
     * 创建用户
     */
    public void createUser(String username, String remark) {
        User newUser = new User(null, username, remark);
        userRepository.save(newUser);
        log.info("创建用户: {}", newUser);
    }

    /**
     * 更新用户
     */
    public void updateUser(Long userId, String username, String remark) {
        // 验证用户存在
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        User updatedUser = new User(userId, username, remark);
        userRepository.save(updatedUser);
        log.info("更新用户: {}", updatedUser);
    }

    /**
     * 删除用户
     */
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        log.info("删除用户: {}", userId);
    }

}
