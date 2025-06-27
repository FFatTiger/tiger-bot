package com.fffattiger.wechatbot.infrastructure.external.paste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 文本分享数据
 */
public class PasteData {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("slug")
    private String slug;
    
    @JsonProperty("content")
    private String content;
    
    @JsonProperty("remark")
    private String remark;
    
    @JsonProperty("password")
    private String password;
    
    @JsonProperty("expiresAt")
    private String expiresAt;
    
    @JsonProperty("maxViews")
    private Integer maxViews;
    
    @JsonProperty("views")
    private Integer views;
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    @JsonProperty("updatedAt")
    private String updatedAt;

    private String url;

    public PasteData() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Integer getMaxViews() {
        return maxViews;
    }

    public void setMaxViews(Integer maxViews) {
        this.maxViews = maxViews;
    }

    public Integer getViews() {
        return views;
    }

    public void setViews(Integer views) {
        this.views = views;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
} 