package com.fffattiger.wechatbot.infrastructure.external.paste.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * 最大上传大小数据
 */
@Data
public class MaxUploadSizeData {
    
    @JsonProperty("max_upload_size")
    private Long maxUploadSize;

    public MaxUploadSizeData() {}

    public MaxUploadSizeData(Long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
    
} 