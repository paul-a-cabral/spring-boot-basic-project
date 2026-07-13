package com.example.core.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig class implements WebMvcConfigurer to provide custom configuration for the Spring MVC
 * application. Add custom configurations such as interceptors, formatters, or view resolvers here.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final DetailedParamInterceptor detailedParamInterceptor;

  public WebConfig(DetailedParamInterceptor detailedParamInterceptor) {
    this.detailedParamInterceptor = detailedParamInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry
        .addInterceptor(detailedParamInterceptor)
        //  .addPathPatterns("/api/**"); // Apply to all API endpoints
        .addPathPatterns("/api/employees/**"); // Apply to all EmployeeController endpoints
  }
}
