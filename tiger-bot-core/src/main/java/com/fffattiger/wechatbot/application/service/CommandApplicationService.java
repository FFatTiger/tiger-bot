package com.fffattiger.wechatbot.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.shared.valueobject.AiSpecification;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

/**
 * 命令应用服务
 * 专门负责命令相关的应用逻辑
 */
@Service
@Slf4j
public class CommandApplicationService {

    @Resource
    private CommandRepository commandRepository;

    /**
     * 获取所有命令
     */
    public List<Command> getAllCommands() {
        List<Command> commands = new ArrayList<>();
        commandRepository.findAll().forEach(commands::add);
        return commands;
    }

    /**
     * 根据ID获取命令
     */
    public Command getCommandById(Long commandId) {
        return commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found: " + commandId));
    }

    /**
     * 创建命令
     */
    public Command createCommand(String pattern, String description, AiSpecification aiSpecification) {
        Command newCommand = new Command(null, pattern, description, aiSpecification);
        
        // 使用领域对象验证
        if (!newCommand.isValidCommand()) {
            throw new RuntimeException("Invalid command data");
        }
        
        Command savedCommand = commandRepository.save(newCommand);
        log.info("创建命令: {}", savedCommand);
        return savedCommand;
    }

    /**
     * 更新命令
     */
    public Command updateCommand(Long commandId, String pattern, String description, AiSpecification aiSpecification) {
        // 验证命令存在
        commandRepository.findById(commandId)
                .orElseThrow(() -> new RuntimeException("Command not found: " + commandId));

        Command updatedCommand = new Command(commandId, pattern, description, aiSpecification);
        
        // 使用领域对象验证
        if (!updatedCommand.isValidCommand()) {
            throw new RuntimeException("Invalid command data");
        }
        
        Command savedCommand = commandRepository.save(updatedCommand);
        log.info("更新命令: {}", savedCommand);
        return savedCommand;
    }

    /**
     * 删除命令
     */
    public void deleteCommand(Long commandId) {
        // 验证命令存在
        getCommandById(commandId);
        
        commandRepository.deleteById(commandId);
        log.info("删除命令: {}", commandId);
    }

    /**
     * 检查命令是否匹配输入
     */
    public boolean matches(Long commandId, String input) {
        Command command = getCommandById(commandId);
        return command.matches(input);
    }

    /**
     * 检查命令是否需要AI配置
     */
    public boolean requiresAiConfiguration(Long commandId) {
        Command command = getCommandById(commandId);
        return command.requiresAiConfiguration();
    }
}
