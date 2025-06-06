package com.fffattiger.wechatbot.management.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fffattiger.wechatbot.application.dto.ListenerAggregate;
import com.fffattiger.wechatbot.application.service.CommandAuthApplicationService;
import com.fffattiger.wechatbot.application.service.ListenerApplicationService;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.listener.ChatCommandAuth;
import com.fffattiger.wechatbot.domain.user.User;

import lombok.extern.slf4j.Slf4j;

/**
 * 机器人配置控制器
 */
@Controller
@RequestMapping("/management/bot-config")
@Slf4j
public class BotConfigController {

    @Autowired
    private ListenerApplicationService listenerApplicationService;

    @Autowired
    private CommandAuthApplicationService commandAuthApplicationService;

    /**
     * 监听对象配置页面
     */
    @GetMapping("/listeners")
    public String listeners(Model model) {
        log.info("访问监听对象配置页面");
        
        List<ListenerAggregate> listeners = listenerApplicationService.findAll();
        List<Chat> allChats = listenerApplicationService.getAllChats();
        
        model.addAttribute("listeners", listeners);
        model.addAttribute("allChats", allChats);
        
        return "management/bot-config/listeners";
    }

    /**
     * 命令权限配置页面
     */
    @GetMapping("/command-auths")
    public String commandAuths(Model model) {
        log.info("访问命令权限配置页面");
        
        List<ChatCommandAuth> commandAuths = commandAuthApplicationService.getAllCommandAuths();
        List<Chat> allChats = listenerApplicationService.getAllChats();
        List<Command> allCommands = listenerApplicationService.getAllCommands();
        List<User> allUsers = listenerApplicationService.getAllUsers();
        
        model.addAttribute("commandAuths", commandAuths);
        model.addAttribute("allChats", allChats);
        model.addAttribute("allCommands", allCommands);
        model.addAttribute("allUsers", allUsers);
        
        return "management/bot-config/command-auths";
    }

    /**
     * 更新监听器配置
     */
    @PostMapping("/listeners/{id}/update")
    @ResponseBody
    public String updateListener(
            @PathVariable Long id,
            @RequestParam boolean atReplyEnable,
            @RequestParam boolean keywordReplyEnable,
            @RequestParam boolean savePic,
            @RequestParam boolean saveVoice,
            @RequestParam boolean parseLinks,
            @RequestParam(required = false) String keywordReply) {
        
        try {
            List<String> keywords = keywordReply != null && !keywordReply.trim().isEmpty() 
                    ? List.of(keywordReply.split(","))
                    : List.of();
            
            listenerApplicationService.updateListener(id, atReplyEnable, keywordReplyEnable, 
                    savePic, saveVoice, parseLinks, keywords);
            
            log.info("更新监听器配置成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("更新监听器配置失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 创建新监听器
     */
    @PostMapping("/listeners/create")
    @ResponseBody
    public String createListener(
            @RequestParam Long chatId,
            @RequestParam boolean atReplyEnable,
            @RequestParam boolean keywordReplyEnable,
            @RequestParam boolean savePic,
            @RequestParam boolean saveVoice,
            @RequestParam boolean parseLinks,
            @RequestParam(required = false) String keywordReply) {
        
        try {
            List<String> keywords = keywordReply != null && !keywordReply.trim().isEmpty() 
                    ? List.of(keywordReply.split(","))
                    : List.of();
            
            listenerApplicationService.createListener(chatId, atReplyEnable, keywordReplyEnable, 
                    savePic, saveVoice, parseLinks, keywords);
            
            log.info("创建监听器成功: {}", chatId);
            return "success";
        } catch (Exception e) {
            log.error("创建监听器失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除监听器
     */
    @PostMapping("/listeners/{id}/delete")
    @ResponseBody
    public String deleteListener(@PathVariable Long id) {
        try {
            listenerApplicationService.deleteListener(id);
            log.info("删除监听器成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("删除监听器失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 创建命令权限
     */
    @PostMapping("/command-auths/create")
    @ResponseBody
    public String createCommandAuth(
            @RequestParam Long chatId,
            @RequestParam Long commandId,
            @RequestParam(required = false) Long userId) {
        
        try {
            commandAuthApplicationService.createCommandAuth(chatId, commandId, userId);
            log.info("创建命令权限成功: chatId={}, commandId={}, userId={}", chatId, commandId, userId);
            return "success";
        } catch (Exception e) {
            log.error("创建命令权限失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除命令权限
     */
    @PostMapping("/command-auths/{id}/delete")
    @ResponseBody
    public String deleteCommandAuth(@PathVariable Long id) {
        try {
            commandAuthApplicationService.deleteCommandAuth(id);
            log.info("删除命令权限成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("删除命令权限失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新命令权限
     */
    @PostMapping("/command-auths/{id}/update")
    @ResponseBody
    public String updateCommandAuth(
            @PathVariable Long id,
            @RequestParam Long chatId,
            @RequestParam Long commandId,
            @RequestParam(required = false) Long userId) {

        try {
            commandAuthApplicationService.updateCommandAuth(id, chatId, commandId, userId);
            log.info("更新命令权限成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("更新命令权限失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 用户管理页面
     */
    @GetMapping("/users")
    public String users(Model model) {
        log.info("访问用户管理页面");

        List<User> allUsers = listenerApplicationService.getAllUsers();
        model.addAttribute("users", allUsers);

        return "management/bot-config/users";
    }

    /**
     * 创建用户
     */
    @PostMapping("/users/create")
    @ResponseBody
    public String createUser(
            @RequestParam String username,
            @RequestParam(required = false) String remark) {

        try {
            listenerApplicationService.createUser(username, remark);
            log.info("创建用户成功: {}", username);
            return "success";
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新用户
     */
    @PostMapping("/users/{id}/update")
    @ResponseBody
    public String updateUser(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam(required = false) String remark) {

        try {
            listenerApplicationService.updateUser(id, username, remark);
            log.info("更新用户成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/{id}/delete")
    @ResponseBody
    public String deleteUser(@PathVariable Long id) {
        try {
            listenerApplicationService.deleteUser(id);
            log.info("删除用户成功: {}", id);
            return "success";
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 聊天对象管理页面
     */
    @GetMapping("/chats")
    public String chats(Model model) {
        log.info("访问聊天对象管理页面");

        List<Chat> allChats = listenerApplicationService.getAllChats();
        model.addAttribute("chats", allChats);

        return "management/bot-config/chats";
    }

    /**
     * 命令管理页面
     */
    @GetMapping("/commands")
    public String commands(Model model) {
        log.info("访问命令管理页面");

        List<Command> allCommands = listenerApplicationService.getAllCommands();
        model.addAttribute("commands", allCommands);

        return "management/bot-config/commands";
    }
}
