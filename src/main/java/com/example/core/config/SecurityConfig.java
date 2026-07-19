package com.example.core.config;

import com.example.core.security.JwtAuthenticationFilter;
import com.example.core.security.JwtService;
import com.example.core.security.SecurityProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(SecurityProperties.class)
@EnableMethodSecurity // Essential: This turns on the @PreAuthorize annotations in your controller
public class SecurityConfig {

  @Bean
  @ConditionalOnProperty(prefix = "app.security", name = "authentication", havingValue = "JWT")
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      JwtService jwtService, UserDetailsService userDetailsService) {
    return new JwtAuthenticationFilter(jwtService, userDetailsService);
  }

  @Bean
  public AuthenticationEntryPoint customAuthenticationEntryPoint(ObjectMapper objectMapper) {
    return new CustomAuthenticationEntryPoint(objectMapper);
  }

  @Bean
  public AccessDeniedHandler customAccessDeniedHandler(ObjectMapper objectMapper) {
    return new CustomAccessDeniedHandler(objectMapper);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      AuthenticationEntryPoint customAuthenticationEntryPoint,
      AccessDeniedHandler customAccessDeniedHandler,
      ObjectProvider<JwtAuthenticationFilter> jwtAuthenticationFilterProvider,
      SecurityProperties securityProperties)
      throws Exception {
    http.csrf(csrf -> csrf.disable()) // Disabled for stateless/testing ease
        // Allow same-origin frame options so the H2 console layout renders
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
        .exceptionHandling(
            exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler))
        .authorizeHttpRequests(
            auth ->
                auth
                    // 1. Group all public endpoints
                    .requestMatchers(
                        "/h2-console/**", "/api/tasks/get-something", "/api/auth/authorities")
                    .permitAll()

                    // 2. Group all authenticated endpoints 🚀
                    .requestMatchers("/api/tasks/**", "/api/employees/**")
                    .authenticated()

                    // 3. Catch-all public access for any other /api/** endpoints & fallback
                    .requestMatchers("/api/**")
                    .permitAll()
                    .anyRequest()
                    .permitAll());

    switch (securityProperties.getAuthentication()) {
      case "BASIC" -> http.httpBasic(Customizer.withDefaults());

      case "JWT" -> {
        JwtAuthenticationFilter jwtAuthenticationFilter =
            jwtAuthenticationFilterProvider.getIfAvailable();
        if (jwtAuthenticationFilter == null) {
          throw new IllegalStateException(
              "JWT authentication mode requires JwtAuthenticationFilter bean");
        }
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
      }
    }

    return http.build();
  }

  @Bean
  AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {

    return configuration.getAuthenticationManager();
  }
}
