package com.fffattiger.wechatbot.domain.ai.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.ai.AiRole;

public interface AiRoleRepository extends CrudRepository<AiRole, Long> {
    
    Optional<AiRole> findByName(String name);

}
