package com.fffattiger.wechatbot.domain.shared.valueobject;

public record AiSpecification(
    Long aiProviderId,
    Long aiModelId,
    Long aiRoleId
) {
}