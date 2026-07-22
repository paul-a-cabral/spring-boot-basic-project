package com.example.core.batch;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.core.security.AuthenticationMode;

import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
public class JobLauncherConfig {

    // 1. Standard Synchronous JobLauncher (Default)
    // @Primary // 👈 Marks this as the default JobLauncher when no @Qualifier is used
    // Active when app.security.authentication=BASIC (or default)
    // @Bean(name = "jobLauncher")
    // @ConditionalOnProperty(
    //         prefix = "app.security",
    //         name = "authentication",
    //         havingValue = AuthenticationMode.BASIC_VALUE,
    //         matchIfMissing = true // 👈 Fallback to synchronous if property is missing
    // )
    // public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
    //     TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
    //     jobLauncher.setJobRepository(jobRepository);
    //     jobLauncher.setTaskExecutor(new SyncTaskExecutor()); // Standard blocking executor
    //     jobLauncher.afterPropertiesSet();
    //     return jobLauncher;
    // }

    // Note: ONLY override and define a custom bean when JWT mode is active!
    // For BASIC mode, Spring Boot's BatchAutoConfiguration automatically creates 
    // the default synchronous jobLauncher bean for you.

    // 2. Custom Asynchronous JobLauncher
    // Active when app.security.authentication=JWT
    // @Bean(name = "jobLauncher")
    @Bean(name = "asyncJobLauncher")
    // @Primary
    @ConditionalOnProperty(
            prefix = "app.security",
            name = "authentication",
            havingValue = AuthenticationMode.JWT_VALUE
    )
    public JobLauncher asyncJobLauncher(JobRepository jobRepository) throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor()); // Async non-blocking executor
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
}