package com.example.core.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "app.security.authentication=BASIC")
class EmployeeControllerBasicModeIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate restTemplate;

  @Test
  void requestWithoutLoginReturnsUnauthorized() {
    ResponseEntity<String> response =
        restTemplate.getForEntity(baseUrl() + "/api/employees", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void loggedInUserCanAccessEmployees() {
    ResponseEntity<String> response =
        restTemplate
            .withBasicAuth("user", "user")
            .getForEntity(baseUrl() + "/api/employees", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void loggedInGuestIsForbiddenForEmployees() {
    ResponseEntity<String> response =
        restTemplate
            .withBasicAuth("guest", "guest")
            .getForEntity(baseUrl() + "/api/employees", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private String baseUrl() {
    return "http://localhost:" + port;
  }
}