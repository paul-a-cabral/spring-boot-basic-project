package com.example.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.authentication=BASIC")
class TaskControllerBasicModeTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Autowired private MockMvc mockMvc;

  @Test
  void meReturnsUnauthorizedWhenNotAuthenticatedInBasicMode() throws Exception {
    mockMvc.perform(get("/api/tasks/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void meReturnsUsernameWhenAuthenticatedInBasicMode() throws Exception {
    mockMvc
        .perform(get("/api/tasks/me").with(user("basic-user").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string("basic-user"));
  }

  @Test
  void bearerTokenIsNotAppliedInBasicMode() throws Exception {
    mockMvc
        .perform(get("/api/tasks/me").header("Authorization", "Bearer any-token"))
        .andExpect(status().isUnauthorized());
  }
}
