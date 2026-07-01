package com.example.core;

import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * A simple Spring-managed singleton bean.
 * Spring beans are singleton-scoped by default, so no extra scope annotation is required.
 */
public class MySingletonBean implements MyBeanInterface {
    
    private final AtomicInteger counter = new AtomicInteger(0);

    public MySingletonBean() { 
        System.out.println("#### MySingletonBean constructor called");
    }

    public void performTask() {
        System.out.println("  Performing task in MySingletonBean");
        var incrementedValue  = counter.incrementAndGet();
        System.out.println("  Task performed " + incrementedValue + " times.");
    }

    @PostConstruct
    public void init() {
        System.out.println(">>> MySingletonBean preMySingletonBean logic");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("MySingletonBean cleanup logic");
    }
}