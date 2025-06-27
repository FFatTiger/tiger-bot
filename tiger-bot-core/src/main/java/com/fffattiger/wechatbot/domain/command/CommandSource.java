package com.fffattiger.wechatbot.domain.command;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Embeddable;

@Embeddable
public record CommandSource(
    @Enumerated(EnumType.STRING)
    CommandSourceType sourceType,
    String sourceName,
    String sourceId
) {
    
    
    public static CommandSource ofPlugin(String sourceName, String sourceId) {
        return new CommandSource(CommandSourceType.PLUGIN, sourceName, sourceId);
    }

    public static CommandSource ofSystem(String sourceName) {
        return new CommandSource(CommandSourceType.SYSTEM, sourceName, null);
    }
}
