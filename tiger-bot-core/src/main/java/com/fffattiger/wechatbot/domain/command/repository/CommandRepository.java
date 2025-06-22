package com.fffattiger.wechatbot.domain.command.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.command.Command;

public interface CommandRepository extends CrudRepository<Command, Long> {

    Optional<Command> findByPattern(String pattern);
}