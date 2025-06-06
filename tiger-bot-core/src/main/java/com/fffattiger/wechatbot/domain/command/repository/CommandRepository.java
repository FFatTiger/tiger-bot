package com.fffattiger.wechatbot.domain.command.repository;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.command.Command;

public interface CommandRepository extends CrudRepository<Command, Long> {

    Command findByPattern(String pattern);
}