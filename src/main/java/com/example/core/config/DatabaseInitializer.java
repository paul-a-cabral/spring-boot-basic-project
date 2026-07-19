package com.example.core.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInitializer {

  @Bean
  CommandLineRunner initDatabase(DatabaseSeedingService databaseSeedingService) {
    return args -> databaseSeedingService.seedInitialData();
  }
}
