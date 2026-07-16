package com.example.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class StudentEmailProperties {

  @Value("${students.email.domain}")
  private String domain;
}
