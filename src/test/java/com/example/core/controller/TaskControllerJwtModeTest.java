package com.example.core.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.authentication=JWT")
class TaskControllerJwtModeTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private JwtService jwtService;
  @MockitoBean private UserDetailsService userDetailsService;

  @Autowired private MockMvc mockMvc;

  @Test
  void meReturnsUnauthorizedWhenNoTokenInJwtMode() throws Exception {
    mockMvc.perform(get("/api/tasks/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void meReturnsUsernameWhenBearerTokenIsValidInJwtMode() throws Exception {
    UserDetails admin = User.withUsername("admin").password("ignored").roles("ADMIN").build();

    given(jwtService.extractUsername("valid-token")).willReturn("admin");
    given(userDetailsService.loadUserByUsername("admin")).willReturn(admin);
    given(jwtService.isTokenValid("valid-token", admin)).willReturn(true);

    mockMvc
        .perform(get("/api/tasks/me").header("Authorization", "Bearer valid-token"))
        .andExpect(status().isOk())
        .andExpect(content().string("admin"));
  }

  @Test
  void meReturnsUnauthorizedWhenBearerTokenIsInvalidInJwtMode() throws Exception {
    UserDetails admin = User.withUsername("admin").password("ignored").roles("ADMIN").build();

    given(jwtService.extractUsername("invalid-token")).willReturn("admin");
    given(userDetailsService.loadUserByUsername("admin")).willReturn(admin);
    given(jwtService.isTokenValid("invalid-token", admin)).willReturn(false);

    mockMvc
        .perform(get("/api/tasks/me").header("Authorization", "Bearer invalid-token"))
        .andExpect(status().isUnauthorized());
  }
}
