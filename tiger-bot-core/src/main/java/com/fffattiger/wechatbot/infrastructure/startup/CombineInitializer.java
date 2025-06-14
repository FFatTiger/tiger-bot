package com.fffattiger.wechatbot.infrastructure.startup;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class CombineInitializer implements Initializer, CommandLineRunner {

    private final List<OrderedInitializer> initializers;


    @Override
    public void init() {
       for (OrderedInitializer initializer : initializers) {
        try {
            log.info("Initializing {} with order {}", initializer.getClass().getSimpleName(), initializer.getOrder());
            initializer.init();
        } catch (Exception e) {
            log.error("Error initializing {}", initializer.getClass().getSimpleName(), e);
            throw new RuntimeException("Error initializing " + initializer.getClass().getSimpleName(), e);
        }
       }
    }


    @Override
    public void run(String... args) throws Exception {
        init();
    }


}
