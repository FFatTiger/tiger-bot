package com.fffattiger.wechatbot.application.handler.cmd;

import java.util.List;
import java.util.stream.StreamSupport;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
import com.fffattiger.wechatbot.domain.ai.AiRole;
import com.fffattiger.wechatbot.domain.ai.repository.AiRoleRepository;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ListRolesCommandMessageHandler implements CommandMessageHandlerExtension {

    @Resource
    private AiRoleRepository aiRoleRepository;

    @Override
    public String getCommandName() {
        return "角色列表";
    }

    @Override
    public void doHandle(String command, String[] args, MessageHandlerContext context) {
        
        try {
            Iterable<AiRole> rolesIterable = aiRoleRepository.findAll();
            List<AiRole> roles = StreamSupport.stream(rolesIterable.spliterator(), false)
                                              .toList();
            
            if (roles.isEmpty()) {
                context.replyText("暂无可用角色");
                return;
            }
            
            StringBuilder roleList = new StringBuilder("📋 可用AI角色列表：\n\n");
            
            for (int i = 0; i < roles.size(); i++) {
                AiRole role = roles.get(i);
                roleList.append(String.format("%d. %s\n", i + 1, role.getName()));
                
                String description = extractRoleDescription(role.getPromptContent());
                if (!description.isEmpty()) {
                    roleList.append("   ").append(description).append("\n");
                }
                roleList.append("\n");
            }
            
            roleList.append("💡 使用方法：/切换角色 角色名称");
            
            context.replyText(roleList.toString());
            
        } catch (Exception e) {
            log.error("获取角色列表失败: {}", e.getMessage(), e);
            context.replyText("获取角色列表失败：" + e.getMessage());
        }
    }

    private String extractRoleDescription(String promptContent) {
        if (promptContent == null || promptContent.isEmpty()) {
            return "";
        }
        
        String[] lines = promptContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.toLowerCase().startsWith("description:")) {
                return line.substring("description:".length()).trim();
            }
        }
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("-")) {
                return line.length() > 50 ? line.substring(0, 50) + "..." : line;
            }
        }
        
        return "";
    }

    @Override
    public String getDescription() {
        return "/角色列表 - 查看所有可用的AI角色";
    }
}
