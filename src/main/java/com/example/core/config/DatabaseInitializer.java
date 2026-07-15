package com.example.core.config;

import com.example.core.entity.User;
import com.example.core.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseInitializer {

  @Bean
  CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return args -> {
      // 1. Check if the database is already populated to prevent duplicate insertions on restart
      if (userRepository.count() == 0) {

        // 2. Create and save the Admin User
        User admin = new User();
        admin.setUsername("admin");
        // Securely encode the raw password "admin123"
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setRole("ROLE_ADMIN");
        admin.setAuthority("WRITE_PRIVILEGE"); // optional extra permission
        userRepository.save(admin);

        // 3. Create and save the Auditor User
        User auditor = new User();
        auditor.setUsername("auditor");
        auditor.setPassword(passwordEncoder.encode("auditor"));
        auditor.setRole("ROLE_USER"); // Regular user role
        auditor.setAuthority("AUDIT"); // This satisfies hasAuthority('AUDIT')
        userRepository.save(auditor);

        // 4. Create and save a basic Regular User (for testing doSomething)
        User regularUser = new User();
        regularUser.setUsername("user");
        regularUser.setPassword(passwordEncoder.encode("user"));
        regularUser.setRole("ROLE_USER");
        userRepository.save(regularUser);

        User superUser = new User();
        superUser.setUsername("superuser");
        superUser.setPassword(passwordEncoder.encode("superuser"));
        userRepository.save(superUser);

        System.out.println(">> Database successfully seeded with default security accounts!");
      } else {
        System.out.println(">> Database already contains users. Skipping initialization.");
      }
    };
  }
}
