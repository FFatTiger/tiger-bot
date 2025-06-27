package com.fffattiger.wechatbot.infrastructure.external.paste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 创建文本分享请求
 */
@Data
public class CreatePasteRequest {
    
    @JsonProperty("content")
    private String content; // 必填
    
    @JsonProperty("remark")
    private String remark; // 可选
    
    @JsonProperty("slug")
    private String slug; // 可选，自定义短链接
    
    @JsonProperty("password")
    private String password; // 可选，访问密码
    
    @JsonProperty("expiresAt")
    private String expiresAt; // 可选，过期时间
    
    @JsonProperty("maxViews")
    private Integer maxViews; // 可选，最大查看次数

    public CreatePasteRequest() {}

    public CreatePasteRequest(String content) {
        this.content = content;
    }

    public CreatePasteRequest(String content, String remark, String slug, String password, String expiresAt, Integer maxViews) {
        this.content = content;
        this.remark = remark;
        this.slug = slug;
        this.password = password;
        this.expiresAt = expiresAt;
        this.maxViews = maxViews;
    }
} 