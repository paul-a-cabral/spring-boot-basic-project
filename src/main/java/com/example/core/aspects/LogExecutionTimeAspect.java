package com.example.core.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component // Allows Spring to manage this class as a Bean
public class LogExecutionTimeAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAspect.class);

    // This pointcut targets any method annotated with @LogExecutionTime
    @Around("@annotation(com.example.core.annotations.LogExecutionTime)")
    public Object logTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();

        logger.info("\n****(logger.log) Executing method [{}.{}]...", className, methodName);

        // 1. Execute the actual method
        Object proceed = joinPoint.proceed(); 

        long executionTime = System.currentTimeMillis() - startTime;

        // 2. Log the class name, method name, and execution duration
        logger.info("\n****(logger.log) Method [{}.{}] executed in {} ms", 
                joinPoint.getSignature().getDeclaringType().getSimpleName(),
                joinPoint.getSignature().getName(), 
                executionTime);

        // 3. Return the result of the method execution back to the caller
        return proceed;
    }
}
