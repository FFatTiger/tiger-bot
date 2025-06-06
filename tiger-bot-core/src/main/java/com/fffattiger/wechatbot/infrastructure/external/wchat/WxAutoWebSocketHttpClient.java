package com.fffattiger.wechatbot.infrastructure.external.wchat;

import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.AddListenChatRequest;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.BatchedSanitizedWechatMessages;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.ChatWithRequest;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.Result;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.RobotNameResponse;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.SendFileByPathRequest;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.SendFileByUrlRequest;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.SendTextRequest;
import com.fffattiger.wechatbot.infrastructure.external.wchat.MessageHandler.VoiceCallRequest;
import com.fffattiger.wechatbot.interfaces.event.MessageReceiveEvent;
import com.fffattiger.wechatbot.interfaces.event.WxAutoConnectedEvent;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class WxAutoWebSocketHttpClient implements WxAuto{

    private WebSocketClient webSocketClient;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatBotProperties chatBotProperties;
    private final OperationTaskManager taskManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final List<String> listenerChatNames = new ArrayList<>();

    public WxAutoWebSocketHttpClient(ChatBotProperties chatBotProperties, OperationTaskManager taskManager, ApplicationEventPublisher applicationEventPublisher) throws Exception {
        this.chatBotProperties = chatBotProperties;
        this.taskManager = taskManager;
        this.applicationEventPublisher = applicationEventPublisher;

        // 初始化HTTP客户端
        this.webClient = WebClient.builder()
            .baseUrl(chatBotProperties.getWxAutoGatewayHttpUrl())
            .build();
        
    }


    @PostConstruct
    public void init() throws Exception {
        initializeWebSocketClient();
    }

    private void initializeWebSocketClient() throws Exception {
        webSocketClient = new WebSocketClient(new URI(chatBotProperties.getWxAutoGatewayWsUrl())) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("WebSocket连接已建立");
                applicationEventPublisher.publishEvent(new WxAutoConnectedEvent(WxAutoWebSocketHttpClient.this, WxAutoWebSocketHttpClient.this));
            }

            @Override
            public void onMessage(String message) {
                log.debug("收到WebSocket消息: {}", message);
                handleWebSocketMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.info("WebSocket连接关闭: {}", reason);
                // 自动重连
                scheduleReconnect();
            }

            @Override
            public void onError(Exception ex) {
                log.error("WebSocket连接错误: {}", ex.getMessage());
            }
        };
        
        log.info("开始连接WebSocket...");
        webSocketClient.connectBlocking();
    }

    private void scheduleReconnect() {
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            try {
                log.info("尝试重连WebSocket...");
                initializeWebSocketClient();
            } catch (Exception e) {
                log.error("重连WebSocket失败", e);
                scheduleReconnect(); // 继续重连
            }
        });
    }

    private void handleWebSocketMessage(String message) {
        try {
            BatchedSanitizedWechatMessages batchedMessages = objectMapper.readValue(
                message, BatchedSanitizedWechatMessages.class);
            
            if ("wechat_messages".equals(batchedMessages.eventType())) {
                applicationEventPublisher.publishEvent(new MessageReceiveEvent(this, batchedMessages, WxAutoWebSocketHttpClient.this));
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
    public Result<String> addListenChat(String who, boolean savePic, boolean saveVoice, boolean parseLinks) {
        try {
            Result<String> result = taskManager.submitTask("监听聊天: " + who, () -> {
                            AddListenChatRequest request = new AddListenChatRequest(who, savePic, saveVoice, parseLinks);
            
                            return webClient
                                .post()
                                .uri("/api/add_listen_chat")
                                .bodyValue(request)
                                .retrieve()
                                .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                                .block(chatBotProperties.getHttpTimeout());
                        }).get();
            if (result.success()) {
                listenerChatNames.add(who);
            }
            return result;
        } catch (Exception e) {
            log.error("添加监听聊天失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public Result<String> chatWith(String who) {
        try {
            return taskManager.submitTask("与 " + who + " 聊天", () -> {
                ChatWithRequest request = new ChatWithRequest(who);

                return webClient
                    .post()
                    .uri("/api/chat_with")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("切换聊天失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public Result<RobotNameResponse> getRobotName() {
        try {
            return taskManager.submitTask("获取机器人名称", () -> webClient
                .get()
                .uri("/api/get_robot_name")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Result<RobotNameResponse>>() {})
                .block(chatBotProperties.getHttpTimeout())).get();
        } catch (Exception e) {
            log.error("获取机器人名称失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public Result<String> sendFile(String toWho, String filePath) {
        try {
            return taskManager.submitTask("发送文件给: " + toWho, () -> {
                SendFileByPathRequest request = new SendFileByPathRequest(toWho, filePath);

                return webClient
                    .post()
                    .uri("/api/send_file_by_path")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("发送文件失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public Result<String> sendText(String toWho, String text) {
        try {
            return taskManager.submitTask("发送文本消息给: " + toWho, () -> {
                SendTextRequest request = new SendTextRequest(toWho, text);

                return webClient
                    .post()
                    .uri("/api/send_text_message")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("发送文本消息失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public Result<String> voiceCall(String userId) {
        try {
            return taskManager.submitTask("语音通话: " + userId, () -> {
                VoiceCallRequest request = new VoiceCallRequest(userId);

                return webClient
                    .post()
                    .uri("/api/voice_call")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("语音通话失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }

    public Result<String> sendFileByUrl(String toWho, String fileUrl, String filename) {
        try {
            return taskManager.submitTask("通过URL发送文件给: " + toWho, () -> {
                SendFileByUrlRequest request = new SendFileByUrlRequest(toWho, fileUrl, filename);

                return webClient
                    .post()
                    .uri("/api/send_file_by_url")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("通过URL发送文件失败", e);
            return new Result<>(false, e.getMessage(), null, null);
        }
    }


    @Override
    public Result<String> sendFileByUpload(String toWho, File file) {
        try {
            return taskManager.submitTask("上传文件发送给: " + toWho, () -> {
                MultipartBodyBuilder builder = new MultipartBodyBuilder();
                builder.part("to_who", toWho);
                builder.part("file", new FileSystemResource(file));
                MultiValueMap<String, HttpEntity<?>> parts = builder.build();

                return webClient
                    .post()
                    .uri("/api/send_file_by_upload")
                    .body(BodyInserters.fromMultipartData(parts))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Result<String>>() {})
                    .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("上传文件发送失败", e);
            return new Result<>(false, e.getMessage(), null, null);
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
        log.info("Shutting down WxAutoWebSocketHttpClient...");

        taskManager.shutdown();
    }

    @Override
    public List<String> getListeners() {
        return listenerChatNames;
    }
}
