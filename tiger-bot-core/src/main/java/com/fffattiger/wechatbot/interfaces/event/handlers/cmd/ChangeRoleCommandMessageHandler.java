// package com.fffattiger.wechatbot.interfaces.event.handlers.cmd;

// import org.springframework.stereotype.Service;

// import com.fffattiger.wechatbot.application.service.AiChatApplicationService;
// import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandlerContext;

// @Service
// public class ChangeRoleCommandMessageHandler extends AbstractCommandMessageHandler {

//     private final AiChatApplicationService aiChatApplicationService;

//     public ChangeRoleCommandMessageHandler(AiChatApplicationService aiChatApplicationService) {
//         this.aiChatApplicationService = aiChatApplicationService;
//     }

//     @Override
//     public boolean canHandle(String command) {
//         return command.startsWith("/changeRole");
//     }

//     @Override
//     public void doHandle(String command, String[] args, MessageHandlerContext context) {

//         // 查询新角色

//         // 修改角色

//         // 删除旧memory
        
//     }

//     @Override
//     public String description() {
//         return "/切换角色 角色名称";
//     }
// }
