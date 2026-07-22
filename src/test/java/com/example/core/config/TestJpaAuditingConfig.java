package com.example.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;

/**
 * Test-specific JPA Auditing Configuration.
 * Provides a no-op AuditorAware bean for use in @WebMvcTest and other test slices.
 * This avoids the "JPA metamodel must not be empty" error when @EnableJpaAuditing
 * is active but no JPA entities are loaded.
 */
@Configuration
@Profile("test")
public class TestJpaAuditingConfig {

  @Bean
  public AuditorAware<?> auditorAware() {
    return () -> java.util.Optional.empty();
  }
}
