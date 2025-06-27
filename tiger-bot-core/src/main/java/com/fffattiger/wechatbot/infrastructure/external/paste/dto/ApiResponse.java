package com.fffattiger.wechatbot.infrastructure.external.paste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * CloudPaste API 统一响应格式
 */
@Data
public class ApiResponse<T> {
    
    @JsonProperty("code")
    private Integer code;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("success")
    private Boolean success;

    public ApiResponse() {}

    public ApiResponse(Integer code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
    }
} 