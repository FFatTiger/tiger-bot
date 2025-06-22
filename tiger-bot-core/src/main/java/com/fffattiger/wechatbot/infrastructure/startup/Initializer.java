package com.fffattiger.wechatbot.infrastructure.startup;


import org.springframework.core.Ordered;

public interface Initializer{

    void init();

    interface OrderedInitializer extends Initializer, Ordered {
    }

}
