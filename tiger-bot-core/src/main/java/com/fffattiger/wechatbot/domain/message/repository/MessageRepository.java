package com.fffattiger.wechatbot.domain.message.repository;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.message.Message;


public interface MessageRepository extends CrudRepository<Message, Long> {

}
