package com.example.core.config;

import com.example.core.entity.Role;
import com.example.core.entity.User;
import com.example.core.repository.RoleRepository;
import com.example.core.repository.UserRepository;
import java.util.stream.Stream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseInitializer {

  record UserRecord(String username, String roleCode) {}

  @Bean
  CommandLineRunner initDatabase(
      UserRepository userRepository,
      RoleRepository roleRepository,
      PasswordEncoder passwordEncoder) {
    return args -> {
      // 1. Check if the database is already populated to prevent duplicate insertions on restart
      if (userRepository.count() == 0) {

        Stream<UserRecord> userRecords =
            Stream.of(
            new UserRecord("admin", "ADMIN"),
            new UserRecord("auditor", "AUDITOR"),
            new UserRecord("user", "USER"),
            new UserRecord("superuser", "SUPERUSER"),
            new UserRecord("guest", "GUEST"));

        userRecords.forEach(
            userRecord -> {
            Role role =
              roleRepository
                .findByCode(userRecord.roleCode())
                .orElseThrow(
                  () ->
                    new IllegalStateException(
                      "Missing role seed data for code: " + userRecord.roleCode()));

              User user = new User();
              user.setUsername(userRecord.username());
              user.setPassword(
                  passwordEncoder.encode("password")); // Default password for all users
            user.setRole(role);
              userRepository.save(user);
            });

        System.out.println(">> Database successfully seeded with default security accounts!");
      } else {
        System.out.println(">> Database already contains users. Skipping initialization.");
      }
    };
  }
}
