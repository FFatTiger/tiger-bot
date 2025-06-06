package com.fffattiger.wechatbot.infrastructure.external.chatlog;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import com.fffattiger.wechatbot.shared.properties.ChatBotProperties;
import com.fffattiger.wechatbot.shared.util.WeChatMessageProcessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChatLogClient {


    private final RestClient restClient;
    private final ChatBotProperties properties;

    public ChatLogClient(RestClient.Builder restClientBuilder, ChatBotProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.getChatLogApiUrl())
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader(HttpHeaders.ACCEPT, "*/*")
                .build();
    }

    public String getChatHistory(String talker, String timeRange, Integer limit) {
        try {
            log.info("调用Chatlog API: /api/v1/chatlog, 参数: talker={}, time={}, limit={}", talker, timeRange, limit);

            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri(uriBuilder -> {
                        uriBuilder.path("/api/v1/chatlog");
                        if (talker != null) uriBuilder.queryParam("talker", talker);
                        if (timeRange != null) uriBuilder.queryParam("time", timeRange);
                        if (limit != null) uriBuilder.queryParam("limit", limit);
//                        uriBuilder.queryParam("format", "json");
                        return uriBuilder.build();
                    });

            if (properties.getApiKey() != null && !properties.getApiKey().isEmpty()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey());
            }

            return request
                    .retrieve()
                    .body(String.class);

//            log.debug("API响应内容(前100字符): {}", response != null ? response.substring(0, Math.min(100, response.length())) : "无内容");
//            return WeChatMessageProcessor.processMessages(response);
        } catch (Exception e) {
            log.error("调用chatlog API时出错: {}", e.getMessage(), e);
            return "";
        }
    }

    public String getContacts(Integer limit) {
        return fetchRawContent("/api/v1/contact", limit);
    }

    public String getChatrooms(Integer limit) {
        return fetchRawContent("/api/v1/chatroom", limit);
    }

    public String getSessions(Integer limit) {
        return fetchRawContent("/api/v1/session", limit);
    }

    private String fetchRawContent(String path, Integer limit) {
        try {
            log.info("调用Chatlog API: {}, 参数: limit={}", path, limit);

            RestClient.RequestHeadersSpec<?> request = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("limit", limit != null ? limit : 100)
                            .build());

            if (properties.getApiKey() != null && !properties.getApiKey().isEmpty()) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey());
            }

            String response = request
                    .retrieve()
                    .body(String.class);

            return response;
        } catch (Exception e) {
            log.error("调用Chatlog API时出错: {}", e.getMessage(), e);
            return "";
        }
    }
}
