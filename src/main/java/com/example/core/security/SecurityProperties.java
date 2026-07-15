package com.example.core.security;

// import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
// @Value // Generates getters, toString, equals, hashCode, and makes fields final
public class SecurityProperties {
  private AuthenticationMode authentication; // Spring Boot binds this via constructor injection!

  public AuthenticationMode getAuthentication() {
    return authentication;
  }

  public void setAuthentication(String authentication) {
    this.authentication = AuthenticationMode.valueOf(authentication);
  }
}
