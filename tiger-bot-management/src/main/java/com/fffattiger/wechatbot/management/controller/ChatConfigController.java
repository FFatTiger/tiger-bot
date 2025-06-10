package com.fffattiger.wechatbot.management.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fffattiger.wechatbot.domain.ai.AiModel;
import com.fffattiger.wechatbot.domain.ai.AiProvider;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.chat.Chat;
import com.fffattiger.wechatbot.management.application.dto.ChatConfigurationDto;
import com.fffattiger.wechatbot.management.application.dto.MessageRecordDto;
import com.fffattiger.wechatbot.management.application.service.ChatManagementApplicationService;
import com.fffattiger.wechatbot.management.application.service.MessageManagementApplicationService;

import lombok.extern.slf4j.Slf4j;

/**
 * 聊天配置控制器
 */
@Controller
@RequestMapping("/management/chat-config")
@Slf4j
public class ChatConfigController {

    @Autowired
    private ChatManagementApplicationService chatManagementApplicationService;

    @Autowired
    private MessageManagementApplicationService messageManagementApplicationService;

    /**
     * 聊天对象管理页面
     */
    @GetMapping("/chats")
    public String chats(Model model) {
        log.info("访问聊天对象管理页面");

        List<ChatConfigurationDto> chats = chatManagementApplicationService.getAllChatConfigurations();
        List<AiProvider> allProviders = chatManagementApplicationService.getAllAvailableProviders();
        List<AiModel> allModels = chatManagementApplicationService.getAllAvailableModels();
        List<AiRole> allRoles = chatManagementApplicationService.getAllAvailableRoles();

        model.addAttribute("chats", chats);
        model.addAttribute("allProviders", allProviders);
        model.addAttribute("allModels", allModels);
        model.addAttribute("allRoles", allRoles);
        model.addAttribute("pageTitle", "聊天对象管理");

        return "management/chat-config/chats";
    }

    /**
     * 消息记录管理页面
     */
    @GetMapping("/messages")
    public String messages(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "20") int size,
                          @RequestParam(required = false) Long chatId,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String startTime,
                          @RequestParam(required = false) String endTime) {
        log.info("访问消息记录管理页面");

        Pageable pageable = PageRequest.of(page, size);
        Page<MessageRecordDto> messages;

        // 如果有搜索条件，使用搜索功能
        if (chatId != null || (keyword != null && !keyword.trim().isEmpty()) || 
            (startTime != null && !startTime.trim().isEmpty()) || 
            (endTime != null && !endTime.trim().isEmpty())) {
            
            LocalDateTime start = parseDateTime(startTime);
            LocalDateTime end = parseDateTime(endTime);
            
            List<MessageRecordDto> searchResults = messageManagementApplicationService
                    .searchMessageRecords(chatId, keyword, start, end);
            
            // 手动分页
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, searchResults.size());
            List<MessageRecordDto> pageContent = searchResults.subList(startIndex, endIndex);
            
            messages = new org.springframework.data.domain.PageImpl<>(
                pageContent, pageable, searchResults.size());
        } else {
            messages = messageManagementApplicationService.getAllMessageRecords(pageable);
        }

        List<Chat> allChats = messageManagementApplicationService.getAllAvailableChats();

        model.addAttribute("messages", messages);
        model.addAttribute("allChats", allChats);
        model.addAttribute("currentChatId", chatId);
        model.addAttribute("currentKeyword", keyword);
        model.addAttribute("currentStartTime", startTime);
        model.addAttribute("currentEndTime", endTime);
        model.addAttribute("pageTitle", "消息记录管理");

        return "management/chat-config/messages";
    }

    /**
     * 创建聊天对象
     */
    @PostMapping("/chats/create")
    @ResponseBody
    public String createChat(@RequestParam String name,
                           @RequestParam boolean groupFlag,
                           @RequestParam(required = false) Long aiProviderId,
                           @RequestParam(required = false) Long aiModelId,
                           @RequestParam(required = false) Long aiRoleId) {
        try {
            ChatConfigurationDto dto = ChatConfigurationDto.forCreate(
                name, groupFlag, aiProviderId, aiModelId, aiRoleId);
            chatManagementApplicationService.createChatConfiguration(dto);
            return "success";
        } catch (Exception e) {
            log.error("创建聊天对象失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 更新聊天AI配置
     */
    @PostMapping("/chats/{id}/update-ai")
    @ResponseBody
    public String updateChatAi(@PathVariable Long id,
                             @RequestParam Long aiProviderId,
                             @RequestParam Long aiModelId,
                             @RequestParam Long aiRoleId) {
        try {
            ChatConfigurationDto dto = ChatConfigurationDto.forUpdateAi(
                id, aiProviderId, aiModelId, aiRoleId);
            chatManagementApplicationService.updateChatAiConfiguration(id, dto);
            return "success";
        } catch (Exception e) {
            log.error("更新聊天AI配置失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 删除聊天对象
     */
    @PostMapping("/chats/{id}/delete")
    @ResponseBody
    public String deleteChat(@PathVariable Long id) {
        try {
            chatManagementApplicationService.deleteChatConfiguration(id);
            return "success";
        } catch (Exception e) {
            log.error("删除聊天对象失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 批量配置AI设置
     */
    @PostMapping("/chats/batch-update-ai")
    @ResponseBody
    public String batchUpdateAi(@RequestParam String chatIds,
                              @RequestParam Long aiProviderId,
                              @RequestParam Long aiModelId,
                              @RequestParam Long aiRoleId) {
        try {
            List<Long> ids = Arrays.stream(chatIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();
            
            ChatConfigurationDto dto = ChatConfigurationDto.forUpdateAi(
                null, aiProviderId, aiModelId, aiRoleId);
            chatManagementApplicationService.batchUpdateAiConfiguration(ids, dto);
            return "success";
        } catch (Exception e) {
            log.error("批量更新AI配置失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 根据提供商ID获取模型列表
     */
    @GetMapping("/models-by-provider")
    @ResponseBody
    public List<AiModel> getModelsByProvider(@RequestParam Long providerId) {
        return chatManagementApplicationService.getModelsByProviderId(providerId);
    }

    /**
     * 删除消息记录
     */
    @PostMapping("/messages/{id}/delete")
    @ResponseBody
    public String deleteMessage(@PathVariable Long id) {
        try {
            messageManagementApplicationService.deleteMessageRecord(id);
            return "success";
        } catch (Exception e) {
            log.error("删除消息记录失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 清理聊天历史消息
     */
    @PostMapping("/messages/cleanup-chat")
    @ResponseBody
    public String cleanupChatMessages(@RequestParam Long chatId) {
        try {
            messageManagementApplicationService.cleanupChatMessages(chatId);
            return "success";
        } catch (Exception e) {
            log.error("清理聊天历史消息失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 清理历史消息
     */
    @PostMapping("/messages/cleanup-before")
    @ResponseBody
    public String cleanupMessagesBeforeTime(@RequestParam String beforeTime) {
        try {
            LocalDateTime dateTime = parseDateTime(beforeTime);
            if (dateTime == null) {
                return "error: 时间格式无效";
            }
            messageManagementApplicationService.cleanupMessagesBeforeTime(dateTime);
            return "success";
        } catch (Exception e) {
            log.error("清理历史消息失败", e);
            return "error: " + e.getMessage();
        }
    }

    /**
     * 导出聊天记录
     */
    @GetMapping("/messages/export")
    @ResponseBody
    public void exportMessages(
            @RequestParam(required = false) Long chatId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            LocalDateTime start = parseDateTime(startTime);
            LocalDateTime end = parseDateTime(endTime);

            List<MessageRecordDto> messages = messageManagementApplicationService
                    .exportChatRecords(chatId, start, end);

            // 设置响应头
            response.setContentType("text/csv; charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=chat_records_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

            // 写入CSV内容
            try (var writer = response.getWriter()) {
                writer.write("\uFEFF"); // UTF-8 BOM
                writer.write("时间,聊天对象,发送者,类型,内容\n");

                for (MessageRecordDto message : messages) {
                    writer.write(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                        message.getFormattedTimeString(),
                        message.chatName() != null ? message.chatName() : "",
                        message.getDisplaySenderName(),
                        message.getMessageTypeDisplay(),
                        message.content() != null ? message.content().replace("\"", "\"\"") : ""
                    ));
                }
            }
        } catch (Exception e) {
            log.error("导出聊天记录失败", e);
            response.setStatus(500);
        }
    }

    /**
     * 解析日期时间字符串
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException ex) {
                log.warn("无法解析日期时间: {}", dateTimeStr);
                return null;
            }
        }
    }
}
