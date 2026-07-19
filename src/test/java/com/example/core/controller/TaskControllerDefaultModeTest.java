package com.example.core.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.service.AsyncLearningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerDefaultModeTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private AsyncLearningService asyncLearningService;

  @Autowired private MockMvc mockMvc;

  @Test
  void meReturnsUnauthorizedWhenNotAuthenticatedInDefaultMode() throws Exception {
    mockMvc.perform(get("/api/tasks/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void meReturnsUsernameWhenAuthenticatedInDefaultMode() throws Exception {
    mockMvc
        .perform(get("/api/tasks/me").with(user("default-user").roles("USER")))
        .andExpect(status().isOk())
        .andExpect(content().string("default-user"));
  }

  @Test
  void bearerTokenIsNotAppliedInDefaultMode() throws Exception {
    mockMvc
        .perform(get("/api/tasks/me").header("Authorization", "Bearer any-token"))
        .andExpect(status().isUnauthorized());
  }
}
