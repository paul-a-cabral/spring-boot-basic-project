package com.example.core.security;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
@Value // Generates getters, toString, equals, hashCode, and makes fields final
public class SecurityProperties {

  private String authentication; // Spring Boot binds this via constructor injection!

}
