package com.example.core;

import org.springframework.stereotype.Component;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class MyBeanUser {

    @Lookup
    public MyPrototypeBean getPrototypeBean() {
        // Spring will override this method to return a new instance of MyPrototypeBean each time it's called
        return null;
    }

    @Lookup
    public MySingletonBean getSingletonBean() {
        // Spring will override this method to return the singleton instance of MySingletonBean
        return null;
    }

    private final MyBeanInterface myPrototypeBean;
    private final MyBeanInterface mySingletonBean;

    public MyBeanUser(MyBeanInterface myPrototypeBean, MyBeanInterface mySingletonBean) {
        this.myPrototypeBean = myPrototypeBean;
        this.mySingletonBean = mySingletonBean;
    }

    // @Scheduled(fixedRate = 12000)
    public void doWork1() {
        System.out.println("----------------------------------------------------");
        System.out.println("1. Instantiating and using MyPrototypeBean..");
        // Get a new instance of MyPrototypeBean using the @Lookup method
        // Note: Each call to getPrototypeBean() will return a new instance of MyPrototypeBean
        MyPrototypeBean newPrototypeBean = getPrototypeBean();
        newPrototypeBean.performTask();

        System.out.println("2. Instantiating and using MySingletonBean..");
        // Get the singleton instance of MySingletonBean using the @Lookup method
        MySingletonBean newSingletonBean = getSingletonBean();
        newSingletonBean.performTask();
    }

    @Scheduled(fixedRate = 12000)
    public void doWork2() {
        System.out.println("----------------------------------------------------");
        System.out.println("1. Using injected MyPrototypeBean..");
        // instancesset the injected MyPrototypeBean instance
        // Creates a new instance of MyPrototypeBean each time the method is called
        myPrototypeBean.performTask();

        System.out.println("2. Using injected MySingletonBean..");
        // Uses the injected MySingletonBean instance
        // Since MySingletonBean is a singleton, the same instance is used each time the method is called
        mySingletonBean.performTask();
    }

}
