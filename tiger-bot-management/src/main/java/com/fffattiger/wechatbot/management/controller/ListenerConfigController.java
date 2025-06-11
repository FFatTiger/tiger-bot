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

import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.management.application.dto.ListenerConfigurationDto;
import com.fffattiger.wechatbot.management.application.service.ListenerManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 监听器配置控制器
 */
@Controller
@RequestMapping("/management/bot-config")
@Slf4j
public class ListenerConfigController {

    @Autowired
    private ListenerManagementApplicationService listenerManagementApplicationService;

    /**
     * 监听对象配置页面
     */
    @GetMapping("/listeners")
    public String listeners(Model model) {
        

        List<ListenerConfigurationDto> listeners = listenerManagementApplicationService.getAllListenerConfigurations();
        List<Chat> allChats = listenerManagementApplicationService.getAllAvailableChats();

        model.addAttribute("listeners", listeners);
        model.addAttribute("allChats", allChats);

        return "management/bot-config/listeners";
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

            ListenerConfigurationDto dto = ListenerConfigurationDto.forCreation(
                null, atReplyEnable, keywordReplyEnable, savePic, saveVoice, parseLinks, keywords);

            listenerManagementApplicationService.updateListenerConfiguration(id, dto);

            
            return "success";
        } catch (Exception e) {
            
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

            ListenerConfigurationDto dto = ListenerConfigurationDto.forCreation(
                chatId, atReplyEnable, keywordReplyEnable, savePic, saveVoice, parseLinks, keywords);

            listenerManagementApplicationService.createListenerConfiguration(dto);

            
            return "success";
        } catch (Exception e) {
            
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
            listenerManagementApplicationService.deleteListenerConfiguration(id);
            
            return "success";
        } catch (Exception e) {
            
            return "error: " + e.getMessage();
        }
    }

}
