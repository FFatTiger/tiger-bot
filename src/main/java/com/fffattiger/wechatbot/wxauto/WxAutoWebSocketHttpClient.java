package com.fffattiger.wechatbot.wxauto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fffattiger.wechatbot.config.ChatBotProperties;
import com.fffattiger.wechatbot.core.WxChat;
import com.fffattiger.wechatbot.core.context.DefaultMessageHandlerContext;
import com.fffattiger.wechatbot.core.handler.DefaultMessageHandlerChain;
import com.fffattiger.wechatbot.core.holder.WxChatHolder;
import com.fffattiger.wechatbot.wxauto.MessageHandler.AddListenChatRequest;
import com.fffattiger.wechatbot.wxauto.MessageHandler.ApiResponse;
import com.fffattiger.wechatbot.wxauto.MessageHandler.BatchedSanitizedWechatMessages;
import com.fffattiger.wechatbot.wxauto.MessageHandler.ChatWithRequest;
import com.fffattiger.wechatbot.wxauto.MessageHandler.RobotNameResponse;
import com.fffattiger.wechatbot.wxauto.MessageHandler.SendFileByPathRequest;
import com.fffattiger.wechatbot.wxauto.MessageHandler.SendFileByUrlRequest;
import com.fffattiger.wechatbot.wxauto.MessageHandler.SendTextRequest;
import com.fffattiger.wechatbot.wxauto.MessageHandler.VoiceCallRequest;

import lombok.extern.slf4j.Slf4j;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import jakarta.annotation.PreDestroy;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class WxAutoWebSocketHttpClient implements WxAuto{

    private WebSocketClient webSocketClient;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatBotProperties chatBotProperties;
    private final OperationTaskManager taskManager;
    private final ExecutorService messageProcessorPool;
    private final String httpBaseUrl;
    
    
    private Disposable sseSubscription;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    public WxAutoWebSocketHttpClient(List<MessageHandler> messageHandlers, ChatBotProperties chatBotProperties, OperationTaskManager taskManager) throws Exception {
        this.chatBotProperties = chatBotProperties;
        this.httpBaseUrl = "http://192.168.31.77:8000"; // HTTP API地址
        this.messageProcessorPool = Executors.newCachedThreadPool();
        this.taskManager = taskManager;

        // 初始化HTTP客户端
        this.webClient = WebClient.builder()
            .baseUrl(httpBaseUrl)
            .build();
        
        // 初始化WebSocket客户端
        initializeWebSocketClient(messageHandlers);
    }

    private void initializeWebSocketClient(List<MessageHandler> messageHandlers) throws Exception {
        webSocketClient = new WebSocketClient(new URI("ws://192.168.31.77:8765")) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("WebSocket连接已建立");
            }

            @Override
            public void onMessage(String message) {
                log.debug("收到WebSocket消息: {}", message);
                handleWebSocketMessage(message, messageHandlers);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("WebSocket连接关闭: {}", reason);
                // 自动重连
                scheduleReconnect(messageHandlers);
            }

            @Override
            public void onError(Exception ex) {
                log.error("WebSocket连接错误: {}", ex.getMessage());
            }
        };
        
        log.info("开始连接WebSocket...");
        webSocketClient.connectBlocking();
    }

    private void scheduleReconnect(List<MessageHandler> messageHandlers) {
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            try {
                log.info("尝试重连WebSocket...");
                initializeWebSocketClient(messageHandlers);
            } catch (Exception e) {
                log.error("重连WebSocket失败", e);
                scheduleReconnect(messageHandlers); // 继续重连
            }
        });
    }

    private void handleWebSocketMessage(String message, List<MessageHandler> messageHandlers) {
        try {
            BatchedSanitizedWechatMessages batchedMessages = objectMapper.readValue(
                message, BatchedSanitizedWechatMessages.class);
            
            if ("wechat_messages".equals(batchedMessages.eventType())) {
                // 处理微信消息
                for (BatchedSanitizedWechatMessages.Chat chat : batchedMessages.data()) {
                    WxChat wxChat = WxChatHolder.getWxChat(chat.chatName());
                    if (wxChat == null) {
                        log.warn("未监听该对象: {}", chat.chatName());
                        continue;
                    }
                    
                    for (BatchedSanitizedWechatMessages.Chat.Message msg : chat.messages()) {
                        messageProcessorPool.submit(() -> {
                            DefaultMessageHandlerContext context = new DefaultMessageHandlerContext();
                            context.setMessage(msg);
                            context.setWxAuto(WxAutoWebSocketHttpClient.this);
                            context.setCurrentChat(wxChat);
                            context.setChatBotProperties(chatBotProperties);
                            context.setMessageTimestamp(batchedMessages.timestamp());
                            new DefaultMessageHandlerChain(messageHandlers).handle(context);
                        });
                    }
                }
            } else if ("connected".equals(batchedMessages.eventType())) {
                log.info("WebSocket连接确认: {}", batchedMessages.message());
            } else if ("heartbeat".equals(batchedMessages.eventType())) {   
                log.debug("收到心跳消息");
                // 发送心跳响应
                sendHeartbeatResponse();
            }
            
        } catch (Exception e) {
            log.error("处理WebSocket消息失败: {}", message, e);
        }
    }

    private void sendHeartbeatResponse() {
        try {
            Map<String, Object> pong = new HashMap<>();
            pong.put("event_type", "pong");
            pong.put("timestamp", System.currentTimeMillis());
            webSocketClient.send(objectMapper.writeValueAsString(pong));
        } catch (Exception e) {
            log.error("发送心跳响应失败", e);
        }
    }


    
    @Override
    public ApiResponse<String> addListenChat(String who, boolean savePic, boolean saveVoice, boolean parseLinks) {
        try {
            return taskManager.submitTask("监听聊天: " + who, () -> {
                AddListenChatRequest request = new AddListenChatRequest(who, savePic, saveVoice, parseLinks);
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/add_listen_chat")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("添加监听聊天失败", e);
            return new ApiResponse<String>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ApiResponse<String> chatWith(String who) {
        try {
            return taskManager.submitTask("与 " + who + " 聊天", () -> {
                ChatWithRequest request = new ChatWithRequest(who);
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/chat_with")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("切换聊天失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ApiResponse<RobotNameResponse> getRobotName() {
        try {
            return taskManager.submitTask("获取机器人名称", () -> {
                ApiResponse<RobotNameResponse> response = webClient
                    .get()
                    .uri("/api/get_robot_name")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<RobotNameResponse>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("获取机器人名称失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ApiResponse<String> sendFile(String toWho, String filePath) {
        try {
            return taskManager.submitTask("发送文件给: " + toWho, () -> {
                SendFileByPathRequest request = new SendFileByPathRequest(toWho, filePath);
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/send_file_by_path")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("发送文件失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ApiResponse<String> sendText(String toWho, String text) {
        try {
            return taskManager.submitTask("发送文本消息给: " + toWho, () -> {
                SendTextRequest request = new SendTextRequest(toWho, text);
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/send_text_message")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("发送文本消息失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ApiResponse<String> voiceCall(String userId) {
        try {
            return taskManager.submitTask("语音通话: " + userId, () -> {
                VoiceCallRequest request = new VoiceCallRequest(userId);
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/voice_call")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("语音通话失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }

    public ApiResponse<String> sendFileByUrl(String toWho, String fileUrl, String filename) {
        try {
            return taskManager.submitTask("通过URL发送文件给: " + toWho, () -> {
                SendFileByUrlRequest request = new SendFileByUrlRequest(toWho, fileUrl, filename);
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/send_file_by_url")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("通过URL发送文件失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }


    @Override
    public ApiResponse<String> sendFileByUpload(String toWho, File file) {
        try {
            return taskManager.submitTask("上传文件发送给: " + toWho, () -> {
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("to_who", toWho);
                builder.part("file", new FileSystemResource(file));
                MultiValueMap<String, HttpEntity<?>> parts = builder.build();
                
                ApiResponse<String> response = webClient
                    .post()
                    .uri("/api/send_file_by_upload")
                    .body(BodyInserters.fromMultipartData(parts))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
                
                return response;
            }).get();
        } catch (Exception e) {
            log.error("上传文件发送失败", e);
            return new ApiResponse<>(false, e.getMessage(), null, null);
        }
    }

    // 健康检查
    public Mono<Object> healthCheck() {
        return webClient
            .get()
            .uri("/api/health")
            .retrieve()
            .bodyToMono(Object.class)
            .timeout(Duration.ofSeconds(5));
    }

    // 获取连接状态
    public boolean isConnected() {
        return isConnected.get();
    }

    // 获取等待中的任务
    public List<String> getPendingTasks() {
        return taskManager.getPendingUiOperationTasks();
    }

    @Override
    public void setMessageHandlerChain(MessageHandlerChain messageHandlerChain) {
        throw new UnsupportedOperationException("Not supported in reactive implementation");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down WxAutoReactiveClient...");
        
        if (sseSubscription != null && !sseSubscription.isDisposed()) {
            sseSubscription.dispose();
        }
        
        taskManager.shutdown();
        messageProcessorPool.shutdown();
        
        try {
            if (!messageProcessorPool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                messageProcessorPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            messageProcessorPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        log.info("WxAutoReactiveClient shutdown complete");
    }
}
