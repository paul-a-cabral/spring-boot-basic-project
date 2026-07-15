package com.example.core.config;

import com.example.core.security.AuthenticationMode;
import com.example.core.security.SecurityProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI(SecurityProperties securityProperties) {
    String securitySchemeName = "";
    SecurityScheme securityScheme = null;

    if (AuthenticationMode.JWT == securityProperties.getAuthentication()) {
      securitySchemeName = "bearerAuth";
      securityScheme =
          new SecurityScheme()
              .name(securitySchemeName)
              .type(SecurityScheme.Type.HTTP)
              .scheme("bearer")
              .bearerFormat("JWT");

    } else if (AuthenticationMode.BASIC == securityProperties.getAuthentication()) {
      securitySchemeName = "basicAuth";
      securityScheme =
          new SecurityScheme()
              .name(securitySchemeName)
              .type(SecurityScheme.Type.HTTP)
              .scheme("basic")
              .description("Enter your database username and password");

    } else {
      throw new IllegalStateException(
          "Unknown authentication mode: " + securityProperties.getAuthentication());
    }

    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
  }
}
