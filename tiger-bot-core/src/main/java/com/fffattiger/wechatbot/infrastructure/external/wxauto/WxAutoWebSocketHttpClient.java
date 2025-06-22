package com.fffattiger.wechatbot.infrastructure.external.wxauto;

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
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fffattiger.wechatbot.infrastructure.event.WxAutoMessageReceiveEvent;
import com.fffattiger.wechatbot.infrastructure.event.WxAutoConnectedEvent;
import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WxAutoWebSocketHttpClient implements WxAuto {

    private WebSocketClient webSocketClient;
    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatBotProperties chatBotProperties;
    private final OperationTaskManager taskManager;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final List<String> listenerChatNames = new ArrayList<>();

    public WxAutoWebSocketHttpClient(ChatBotProperties chatBotProperties, OperationTaskManager taskManager,
            ApplicationEventPublisher applicationEventPublisher) throws Exception {
        this.chatBotProperties = chatBotProperties;
        this.taskManager = taskManager;
        this.applicationEventPublisher = applicationEventPublisher;

        // 初始化HTTP客户端
        this.webClient = WebClient.builder()
                .baseUrl(chatBotProperties.getWxAutoGatewayHttpUrl())
                .build();

    }

    @Override
    public void init() {
        try {
            initializeWebSocketClient();
        } catch (Exception e) {
            log.error("Error initializing WxAutoWebSocketHttpClient", e);
            throw new RuntimeException("Error initializing WxAutoWebSocketHttpClient", e);
        }
    }

    private void initializeWebSocketClient() throws Exception {
        webSocketClient = new WebSocketClient(new URI(chatBotProperties.getWxAutoGatewayWsUrl())) {
            @Override
            public void onOpen(ServerHandshake handshakedata) {
                log.info("WebSocket连接已建立: 服务器={}, 状态码={}",
                        chatBotProperties.getWxAutoGatewayWsUrl(), handshakedata.getHttpStatus());
                isConnected.set(true);
                applicationEventPublisher.publishEvent(
                        new WxAutoConnectedEvent(WxAutoWebSocketHttpClient.this, WxAutoWebSocketHttpClient.this));
            }

            @Override
            public void onMessage(String message) {
                log.debug("接收到WebSocket消息: 长度={}", message.length());
                handleWebSocketMessage(message);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                log.warn("WebSocket连接关闭: 代码={}, 原因={}, 远程关闭={}", code, reason, remote);
                isConnected.set(false);
                // WebSocket关闭时清空监听列表，因为服务端会自动停止监听
                listenerChatNames.clear();
                log.info("WebSocket连接关闭，已清空本地监听列表");
                // 自动重连
                scheduleReconnect();
            }

            @Override
            public void onError(Exception ex) {
                log.error("WebSocket连接错误: {}", ex.getMessage(), ex);
                isConnected.set(false);
            }
        };

        log.info("开始连接WebSocket服务器: {}", chatBotProperties.getWxAutoGatewayWsUrl());
        webSocketClient.connectBlocking();
        log.info("WebSocket连接建立成功");
    }

    private void scheduleReconnect() {
        log.info("计划在5秒后重新连接WebSocket");
        CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS).execute(() -> {
            try {
                log.info("开始重新连接WebSocket");
                initializeWebSocketClient();
                log.info("WebSocket重连成功");
            } catch (Exception e) {
                log.error("WebSocket重连失败: {}", e.getMessage(), e);
                scheduleReconnect(); // 继续重连
            }
        });
    }

    private void handleWebSocketMessage(String message) {
        try {
            WechatMessageSpecification batchedMessages = objectMapper.readValue(
                    message, WechatMessageSpecification.class);

            String eventType = batchedMessages.eventType();
            log.debug("处理WebSocket消息: 事件类型={}", eventType);

            if ("wechat_messages".equals(eventType)) {
                applicationEventPublisher
                        .publishEvent(new WxAutoMessageReceiveEvent(this, batchedMessages, WxAutoWebSocketHttpClient.this));
            } else if ("connected".equals(eventType)) {
                log.info("WebSocket连接确认: {}", batchedMessages.message());
            } else if ("heartbeat".equals(eventType)) {
                log.debug("接收到心跳消息，发送响应");
                // 发送心跳响应
                sendHeartbeatResponse();
            } else {
                log.debug("未知的WebSocket事件类型: {}", eventType);
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: 消息长度={}, 错误信息={}",
                    message.length(), e.getMessage(), e);
        }
    }

    private void sendHeartbeatResponse() {
        try {
            Map<String, Object> pong = new HashMap<>();
            pong.put("event_type", "pong");
            pong.put("timestamp", System.currentTimeMillis());
            webSocketClient.send(objectMapper.writeValueAsString(pong));
        } catch (Exception e) {
            log.error("发送心跳响应失败: {}", e.getMessage());
        }
    }

    @Override
    public ResultSpecification<String> addListenChat(String nickname) {
        try {
            ResultSpecification<String> resultSpecification = taskManager.submitTask("监听聊天: " + nickname, () -> {
                AddListenChatSpecification request = new AddListenChatSpecification(nickname);

                return webClient
                        .post()
                        .uri("/api/add_listen_chat")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
            if (resultSpecification.success()) {
                listenerChatNames.add(nickname);
                log.info("成功添加监听: {}, 当前监听列表: {}", nickname, listenerChatNames);
            }
            return resultSpecification;
        } catch (Exception e) {
            log.error("添加监听失败: nickname={}, error={}", nickname, e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> removeListenChat(String nickname) {
        try {
            ResultSpecification<String> resultSpecification = taskManager.submitTask("移除监听: " + nickname, () -> {
                RemoveListenChatSpecification request = new RemoveListenChatSpecification(nickname);

                return webClient
                        .post()
                        .uri("/api/remove_listen_chat")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
            if (resultSpecification.success()) {
                listenerChatNames.remove(nickname);
                log.info("成功移除监听: {}, 当前监听列表: {}", nickname, listenerChatNames);
            }
            return resultSpecification;
        } catch (Exception e) {
            log.error("移除监听失败: nickname={}, error={}", nickname, e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> stopAllListening() {
        try {
            ResultSpecification<String> resultSpecification = taskManager.submitTask("停止所有监听", () -> {
                return webClient
                        .post()
                        .uri("/api/stop_all_listening")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
            if (resultSpecification.success()) {
                listenerChatNames.clear();
                log.info("成功停止所有监听，已清空监听列表");
            }
            return resultSpecification;
        } catch (Exception e) {
            log.error("停止所有监听失败: error={}", e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> startListening() {
        try {
            return taskManager.submitTask("开始监听", () -> {
                return webClient
                        .post()
                        .uri("/api/start_listening")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("开始监听失败: error={}", e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> chatWith(String who) {
        try {
            return taskManager.submitTask("与 " + who + " 聊天", () -> {
                ChatWithSpecification request = new ChatWithSpecification(who);

                return webClient
                        .post()
                        .uri("/api/chat_with")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("切换聊天窗口失败: who={}, error={}", who, e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> sendFile(String toWho, String filePath) {
        try {
            return taskManager.submitTask("发送文件给: " + toWho, () -> {
                SendFileByPathSpecification request = new SendFileByPathSpecification(toWho, filePath);

                return webClient
                        .post()
                        .uri("/api/send_file_by_path")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("发送文件失败: toWho={}, filePath={}, error={}", toWho, filePath, e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> sendText(String toWho, String text) {
        log.info("发送文本消息: 接收者={}, 消息长度={}", toWho, text.length());
        log.debug("发送文本消息内容: 接收者={}, 内容={}", toWho, text.length() > 100 ? text.substring(0, 100) + "..." : text);

        try {
            long startTime = System.currentTimeMillis();
            ResultSpecification<String> resultSpecification = taskManager.submitTask("发送文本消息给: " + toWho, () -> {
                SendTextSpecification request = new SendTextSpecification(toWho, text);

                return webClient
                        .post()
                        .uri("/api/send_text_message")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();

            long duration = System.currentTimeMillis() - startTime;
            if (resultSpecification.success()) {
                log.info("文本消息发送成功: 接收者={}, 耗时={}ms", toWho, duration);
            } else {
                log.warn("文本消息发送失败: 接收者={}, 错误={}, 耗时={}ms", toWho, resultSpecification.message(), duration);
            }

            return resultSpecification;
        } catch (Exception e) {
            log.error("文本消息发送异常: 接收者={}, 错误信息={}", toWho, e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    public ResultSpecification<String> sendFileByUrl(String toWho, String fileUrl, String filename) {
        try {
            return taskManager.submitTask("通过URL发送文件给: " + toWho, () -> {
                SendFileByUrlSpecification request = new SendFileByUrlSpecification(toWho, fileUrl, filename);

                return webClient
                        .post()
                        .uri("/api/send_file_by_url")
                        .bodyValue(request)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("通过URL发送文件失败: toWho={}, fileUrl={}, error={}", toWho, fileUrl, e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
        }
    }

    @Override
    public ResultSpecification<String> sendFileByUpload(String toWho, File file) {
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
                        .bodyToMono(new ParameterizedTypeReference<ResultSpecification<String>>() {
                        })
                        .block(chatBotProperties.getHttpTimeout());
            }).get();
        } catch (Exception e) {
            log.error("上传文件发送失败: toWho={}, file={}, error={}", toWho, file.getName(), e.getMessage(), e);
            return new ResultSpecification<>(false, e.getMessage(), null, null);
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

    @PreDestroy
    public void shutdown() {
        log.info("开始关闭微信自动化客户端");

        // 在关闭前停止所有监听
        try {
            if (isConnected.get()) {
                log.info("停止所有监听...");
                stopAllListening();
            }
        } catch (Exception e) {
            log.warn("关闭时停止监听失败: {}", e.getMessage());
        }

        if (webSocketClient != null && !webSocketClient.isClosed()) {
            log.info("关闭WebSocket连接");
            webSocketClient.close();
        }

        taskManager.shutdown();
        log.info("微信自动化客户端关闭完成");
    }

    @Override
    public List<String> getListeners() {
        return new ArrayList<>(listenerChatNames);
    }
}
