package com.example.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Essential: This turns on the @PreAuthorize annotations in your controller
public class SecurityConfig {

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()) // Disabled for stateless/testing ease
        // Allow same-origin frame options so the H2 console layout renders
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .authorizeHttpRequests(
            auth ->
                auth
                    // Make the H2 console endpoints public
                    .requestMatchers("/h2-console/**")
                    .permitAll()
                    // Rule A: Explicitly allow the ONE public task endpoint
                    .requestMatchers("/api/tasks/something")
                    .permitAll()

                    // Rule B: Protect ALL other endpoints under /api/tasks/**
                    // This blocks unauthenticated users from things like /api/tasks/do-something
                    .requestMatchers("/api/tasks/**")
                    .authenticated()

                    // Rule C: Catch-all public access for any other /api/** endpoints
                    // This means /api/users, /api/products, etc., are wide open by default
                    .requestMatchers("/api/**")
                    .permitAll()

                    // Rule D: Fallback catch-all (good practice for any other static resources or
                    // index pages)
                    .anyRequest()
                    .permitAll())
        .httpBasic(basic -> {}); // Enables easy testing (via Postman/curl headers)

    return http.build();
  }
}
