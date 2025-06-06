package com.fffattiger.wechatbot.domain.listener.repository;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.listener.Listener;

public interface ListenerRepository extends CrudRepository<Listener, Long> {

    Listener findByChatId(Long id);

}
