package com.fffattiger.wechatbot.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.user.User;

public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

}
