package com.example.core.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing Configuration. Only activated in non-test profiles to prevent "JPA metamodel must
 * not be empty" errors in @WebMvcTest test slices.
 */
@Configuration
@EnableJpaAuditing
@Profile("!test")
public class JpaAuditingConfig {

  /**
   * Provides a no-op AuditorAware bean that returns empty Optional. This is used when no actual
   * auditor information is available.
   */
  @Bean
  @ConditionalOnMissingBean
  public AuditorAware<?> auditorAware() {
    return () -> java.util.Optional.empty();
  }
}
