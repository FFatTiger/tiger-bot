package com.fffattiger.wechatbot.domain.ai;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "ai_roles")
public record AiRole(
    @Id
    Long id,

    String name,

    String promptContent,

    String extraMemory,

    String promptType
) {
}
