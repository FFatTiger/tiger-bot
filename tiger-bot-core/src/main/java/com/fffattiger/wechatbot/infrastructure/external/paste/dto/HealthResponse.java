package com.fffattiger.wechatbot.infrastructure.external.paste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 健康检查响应
 */
@Data
public class HealthResponse {
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("timestamp")
    private String timestamp;

    public HealthResponse() {}

    public HealthResponse(String status, String timestamp) {
        this.status = status;
        this.timestamp = timestamp;
    }

} 