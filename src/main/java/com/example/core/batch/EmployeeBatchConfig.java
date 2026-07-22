package com.example.core.batch;

import com.example.core.batch.dto.EmployeeCsvRecord;
import com.example.core.employee.EmployeeEntity;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EmployeeBatchConfig {

  // 1. READER: Read CSV file lines into EmployeeCsvRecord DTOs
  // 1. Add @StepScope so each step execution creates its own isolated reader
  // instance
  @Bean
  @StepScope
  public FlatFileItemReader<EmployeeCsvRecord> csvEmployeeReader() {
    return new FlatFileItemReaderBuilder<EmployeeCsvRecord>()
        .name("csvEmployeeReader")
        .resource(new ClassPathResource("import-employees.csv"))
        .linesToSkip(1) // Skip Header line
        .delimited()
        .names("name", "salary")
        .targetType(EmployeeCsvRecord.class)
        .build();
  }

  // 2. WRITER: Save entities into H2 database using JPA
  @Bean
  public JpaItemWriter<EmployeeEntity> jpaEmployeeWriter(EntityManagerFactory entityManagerFactory) {
    return new JpaItemWriterBuilder<EmployeeEntity>()
        .entityManagerFactory(entityManagerFactory)
        .build();
  }

  // 3. STEP: Combine Reader -> Processor -> Writer with 5-item chunks
  @Bean
  public Step importEmployeeStep(
      JobRepository jobRepository,
      PlatformTransactionManager transactionManager,
      FlatFileItemReader<EmployeeCsvRecord> reader,
      EmployeeItemProcessor processor,
      JpaItemWriter<EmployeeEntity> writer) {

    return new StepBuilder("importEmployeeStep", jobRepository)
        .<EmployeeCsvRecord, EmployeeEntity>chunk(5, transactionManager)
        .reader(reader)
        .processor(processor)
        .writer(writer)
        .build();
  }

  // 4. JOB: Container holding the execution flow
  @Bean
  public Job importEmployeeJob(
      JobRepository jobRepository,
      Step importEmployeeStep,
      JobStatusListener jobStatusListener) { // 👈 Inject your listener

    return new JobBuilder("importEmployeeJob", jobRepository)
        .incrementer(new RunIdIncrementer())
        .listener(jobStatusListener) // 👈 Register it here
        .start(importEmployeeStep)
        .build();
  }

}