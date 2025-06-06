package com.fffattiger.wechatbot.domain.ai;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ai_providers")
public record AiProvider(
    @Id
    Long id,

    String providerType,

    String providerName,

    String apiKey,

    String baseUrl
) {
}
