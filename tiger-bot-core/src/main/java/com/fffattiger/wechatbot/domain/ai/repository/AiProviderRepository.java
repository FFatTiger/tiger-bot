package com.fffattiger.wechatbot.domain.ai.repository;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.ai.AiProvider;

public interface AiProviderRepository extends CrudRepository<AiProvider, Long> {
    
}
