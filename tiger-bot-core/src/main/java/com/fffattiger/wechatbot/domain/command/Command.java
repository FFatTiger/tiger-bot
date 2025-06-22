package com.fffattiger.wechatbot.domain.command;

import org.springframework.util.AntPathMatcher;

import com.fffattiger.wechatbot.domain.common.AggregateRoot;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;

@Entity
@Table(name = "commands")
@Getter
public class Command extends AggregateRoot {

    /**
     * 命令的正则或名称
     */
    private String pattern;

    /**
     * 命令描述
     */
    private String description;

    /**
     * AI配置
     */
    @Embedded
    private AiSpecification aiSpecification;

    /**
     * 命令参数
     */
    @Transient
    private CommandArgs commandArgs;

    // 构造函数
    protected Command() {}

    public Command(String pattern, String description, AiSpecification aiSpecification) {
        this.pattern = pattern;
        this.description = description;
        this.aiSpecification = aiSpecification;
        
    }


    public CommandArgs extractCommandArgs(String commandWithArgs, String commandPrefix) {
        if (!commandWithArgs.startsWith(commandPrefix)) {
            return null;
        }

        String[] command = commandWithArgs.split(" ");
        String cleanCommand = command[0].replace(commandPrefix, "");

        String[] args = new String[command.length - 1];
        System.arraycopy(command, 1, args, 0, command.length - 1);
        return new CommandArgs(cleanCommand, args);        
    }
}