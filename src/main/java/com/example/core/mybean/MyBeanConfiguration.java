package com.example.core.mybean;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

@Configuration
public class MyBeanConfiguration {
  // This class can be used to define additional bean configurations if needed.

  @Bean
  public MySingletonBean mySingletonBean() {
    return new MySingletonBean();
  }

  @Bean // or specify which method is for PreDestroy: @Bean(destroyMethod = "cleanUp")
  @Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
  public MyPrototypeBean myPrototypeBean() {
    return new MyPrototypeBean();
  }
}
