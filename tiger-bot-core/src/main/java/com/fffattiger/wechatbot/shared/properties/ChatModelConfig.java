package com.fffattiger.wechatbot.shared.properties;

import com.fffattiger.wechatbot.infrastructure.ai.ModelProviders;

import lombok.Data;

@Data
public class ChatModelConfig {

    private ModelProviders provider;

    private String baseUrl;

    private String model;

    private String apiKey;

}
