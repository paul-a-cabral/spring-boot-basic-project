package com.example.core.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "app.security.authentication=JWT")
class EmployeeControllerJwtModeIT {

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
    String token = loginAndGetJwt("user", "user");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl() + "/api/employees", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void loggedInGuestIsForbiddenForEmployees() {
    String token = loginAndGetJwt("guest", "guest");

    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(token);
    HttpEntity<Void> request = new HttpEntity<>(headers);

    ResponseEntity<String> response =
        restTemplate.exchange(baseUrl() + "/api/employees", HttpMethod.GET, request, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private String loginAndGetJwt(String username, String password) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    Map<String, String> payload = Map.of("username", username, "password", password);
    HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

    @SuppressWarnings("unchecked")
    ResponseEntity<Map<String, Object>> response =
        (ResponseEntity<Map<String, Object>>)
            (ResponseEntity<?>)
                restTemplate.postForEntity(baseUrl() + "/api/auth/login", request, Map.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().get("token")).isNotNull();

    return response.getBody().get("token").toString();
  }

  private String baseUrl() {
    return "http://localhost:" + port;
  }
}
