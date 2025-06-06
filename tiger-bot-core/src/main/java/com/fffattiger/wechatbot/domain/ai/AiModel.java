package com.fffattiger.wechatbot.domain.ai;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ai_models")
public record AiModel(
    @Id
    Long id,

    Long aiProviderId,
    
    String modelName,

    String description,

    int maxTokens,

    int maxOutputTokens,

    Double temperature,

    Double frequencyPenalty,

    Double presencePenalty,

    Double topK,

    Double topP,

    boolean reasoningFlg,

    boolean streamFlg,

    boolean enabled,
    
    boolean toolCallFlg
) {
}
