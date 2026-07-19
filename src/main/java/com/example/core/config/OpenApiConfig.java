package com.example.core.config;

import com.example.core.security.AuthenticationMode;
import com.example.core.security.SecurityProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI(SecurityProperties securityProperties) {
    String securitySchemeName = "";
    SecurityScheme securityScheme = null;

    AuthenticationMode authenticationMode = securityProperties.getAuthentication();

    switch (authenticationMode) {
      case JWT -> {
        securitySchemeName = "bearerAuth";
        securityScheme =
            new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");
      }
      case BASIC -> {
        securitySchemeName = "basicAuth";
        securityScheme =
            new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic")
                .description("Enter your database username and password");
      }
    }

    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme))
        .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
  }

  @Bean
  public OperationCustomizer addGlobalDetailedQueryParam() {
    return (operation, handlerMethod) -> {
      Parameter detailedParam =
          new Parameter()
              .name("detailed")
              .in("query")
              .required(false)
              .description("If set to true, triggers detailed error outputs.")
              .schema(new BooleanSchema());

      operation.addParametersItem(detailedParam);
      return operation;
    };
  }
}
