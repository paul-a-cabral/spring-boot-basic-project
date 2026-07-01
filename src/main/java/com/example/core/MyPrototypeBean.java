package com.example.core;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

public class MyPrototypeBean implements MyBeanInterface {

    private final AtomicInteger counter = new AtomicInteger(0);
    
    public MyPrototypeBean() {
       System.out.println("#### MyPrototypeBean instance created");
    }

    public void performTask() {
        System.out.println("  Performing task in MyPrototypeBean");
        var incrementedValue = counter.incrementAndGet();
        System.out.println("  Task performed " + incrementedValue + " times.");
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> MyPrototypeBean pre-initialization logic");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("MyPrototypeBean cleanup logic");
    }
}
