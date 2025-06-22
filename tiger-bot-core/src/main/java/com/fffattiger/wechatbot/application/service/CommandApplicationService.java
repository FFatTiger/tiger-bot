package com.fffattiger.wechatbot.application.service;

import org.springframework.stereotype.Service;

import com.fffattiger.wechatbot.domain.command.Command;
import com.fffattiger.wechatbot.domain.command.repository.CommandRepository;
import com.fffattiger.wechatbot.domain.permission.service.PermissionDomainService;
import com.fffattiger.wechatbot.domain.user.User;
import com.fffattiger.wechatbot.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandApplicationService {

    private final CommandRepository commandRepository;

    private final PermissionDomainService permissionDomainService;

    private final UserRepository userRepository;

    public Command findByCommand(String command) {
        return commandRepository.findByPattern(command).orElseThrow(() -> new RuntimeException("Command not found"));
    }

    public boolean hasPermission(Command command, String sender, Long chatId) {
        User user = userRepository.findByUsername(sender).orElseThrow(() -> new RuntimeException("User not found"));

        return permissionDomainService.canExecuteCommand(user.getId(), command.getId(), chatId);
    }

    public void registerCommand(Command command) {
        Command existingCommand = commandRepository.findByPattern(command.getPattern()).orElse(null);
        if (existingCommand != null) {
            return;
        }

        commandRepository.save(command);
    }
}
