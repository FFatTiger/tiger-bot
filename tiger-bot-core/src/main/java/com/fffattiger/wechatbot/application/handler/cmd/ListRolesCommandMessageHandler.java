// package com.fffattiger.wechatbot.infrastructure.event.handlers.cmd;

// import java.util.List;

// import org.springframework.stereotype.Service;

// import com.fffattiger.wechatbot.api.CommandMessageHandlerExtension;
// import com.fffattiger.wechatbot.api.context.MessageHandlerContext;
// import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
// import com.fffattiger.wechatbot.domain.ai.AiRole;

// import jakarta.annotation.Resource;
// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class ListRolesCommandMessageHandler implements CommandMessageHandlerExtension {

//     @Resource
//     private AiChatApplicationService aiChatApplicationService;

//     @Override
//     public String getCommandName() {
//         return "角色列表";
//     }

//     @Override
//     public void doHandle(String command, String[] args, MessageHandlerContext context) {
//         String chatName = context.getMessage().chatName();
        
//         try {
//             // 获取所有角色
//             List<AiRole> roles = aiChatApplicationService.getAllRoles();
            
//             if (roles.isEmpty()) {
//                 context.replyText(chatName, "暂无可用角色");
//                 return;
//             }
            
//             // 格式化角色列表
//             StringBuilder roleList = new StringBuilder("📋 可用AI角色列表：\n\n");
            
//             for (int i = 0; i < roles.size(); i++) {
//                 AiRole role = roles.get(i);
//                 roleList.append(String.format("%d. %s\n", i + 1, role.name()));
                
//                 // 添加角色描述（从prompt内容中提取简短描述）
//                 String description = extractRoleDescription(role.promptContent());
//                 if (description != null && !description.isEmpty()) {
//                     roleList.append("   ").append(description).append("\n");
//                 }
//                 roleList.append("\n");
//             }
            
//             roleList.append("💡 使用方法：/切换角色 角色名称");
            
//             context.replyText(chatName, roleList.toString());
            
//         } catch (Exception e) {
//             log.error("获取角色列表失败: {}", e.getMessage(), e);
//             context.replyText(chatName, "获取角色列表失败：" + e.getMessage());
//         }
//     }

//     /**
//      * 从角色的prompt内容中提取简短描述
//      */
//     private String extractRoleDescription(String promptContent) {
//         if (promptContent == null || promptContent.isEmpty()) {
//             return "";
//         }
        
//         // 尝试从description字段提取
//         String[] lines = promptContent.split("\n");
//         for (String line : lines) {
//             line = line.trim();
//             if (line.startsWith("- description:")) {
//                 return line.substring("- description:".length()).trim();
//             }
//         }
        
//         // 如果没有找到description，返回第一行作为描述
//         for (String line : lines) {
//             line = line.trim();
//             if (!line.isEmpty() && !line.startsWith("#") && !line.startsWith("-")) {
//                 return line.length() > 50 ? line.substring(0, 50) + "..." : line;
//             }
//         }
        
//         return "";
//     }

//     @Override
//     public String getDescription() {
//         return "/角色列表 - 查看所有可用的AI角色";
//     }
// }
