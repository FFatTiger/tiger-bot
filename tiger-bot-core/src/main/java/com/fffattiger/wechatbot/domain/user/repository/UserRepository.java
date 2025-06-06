package com.fffattiger.wechatbot.domain.user.repository;

import org.springframework.data.repository.CrudRepository;

import com.fffattiger.wechatbot.domain.user.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
