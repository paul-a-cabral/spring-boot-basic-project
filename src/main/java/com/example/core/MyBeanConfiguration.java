package com.example.core;

import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class MyBeanConfiguration {
    // This class can be used to define additional bean configurations if needed.

    @Bean
    public MySingletonBean mySingletonBean() {
        return new MySingletonBean();
    }

    @Bean
    @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public MyPrototypeBean myPrototypeBean() {
        return new MyPrototypeBean();
    }
}