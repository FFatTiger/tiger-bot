package com.fffattiger.wechatbot.domain.ai.repository;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.ai.AiModel;

public interface AiModelRepository extends CrudRepository<AiModel, Long> {
    
}
