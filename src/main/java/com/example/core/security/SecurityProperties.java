package com.example.core.security;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.security")
@Validated
@Value // Generates getters, toString, equals, hashCode, and makes fields final
public class SecurityProperties {

  @NotNull(
      message =
          "Authentication mode must be specified in application.properties as either BASIC or JWT")
  private AuthenticationMode authentication; // Spring Boot binds this via constructor injection!
}
