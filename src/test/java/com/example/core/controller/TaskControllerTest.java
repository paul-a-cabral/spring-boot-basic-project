package com.example.core.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.security.AuthenticationMode;
import com.example.core.service.AsyncLearningService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.security.authentication=" + AuthenticationMode.BASIC_VALUE)
class TaskControllerTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;
  @MockitoBean private AsyncLearningService asyncLearningService;

  @Autowired private MockMvc mockMvc;

  @Test
  void getSomethingIsPublic() throws Exception {
    mockMvc
        .perform(get("/api/tasks/get-something"))
        .andExpect(status().isOk())
        .andExpect(content().string("Public Data: Anyone can see this!"));
  }

  @Test
  void doSomethingReturnsUnauthorizedWhenNotAuthenticated() throws Exception {
    mockMvc
        .perform(get("/api/tasks/get-something-authenticated"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(401))
        .andExpect(jsonPath("$.error").value("Unauthorized"))
        .andExpect(
            jsonPath("$.message").value("Authentication is required to access this resource"))
        .andExpect(jsonPath("$.path").value("/api/tasks/get-something-authenticated"));
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void doSomethingReturnsOkWhenAuthenticated() throws Exception {
    mockMvc
        .perform(get("/api/tasks/get-something-authenticated"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: You are logged in!"));
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void adminTaskReturnsForbiddenWhenRoleIsNotAdmin() throws Exception {
    mockMvc
        .perform(post("/api/tasks/admin-task"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminTaskReturnsOkWhenRoleIsAdmin() throws Exception {
    mockMvc
        .perform(post("/api/tasks/admin-task"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: Admin task executed!"));
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void auditTaskReturnsForbiddenWhenAuditAuthorityMissing() throws Exception {
    mockMvc
        .perform(post("/api/tasks/audit-task"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error").value("[Core Error Handler] Access denied: Access Denied"));
  }

  @Test
  @WithMockUser(username = "auditor", authorities = "CAN_AUDIT")
  void auditTaskReturnsOkWhenAuditAuthorityPresent() throws Exception {
    mockMvc
        .perform(post("/api/tasks/audit-task"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: Audit task executed!"));
  }

  @Test
  void startAsyncReportReturnsUnauthorizedWhenNotAuthenticated() throws Exception {
    mockMvc.perform(post("/api/tasks/reports/async")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void startAsyncReportReturnsAcceptedWhenAuthenticated() throws Exception {
    given(asyncLearningService.startReportJob("user")).willReturn("job-123");

    mockMvc
        .perform(post("/api/tasks/reports/async"))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.jobId").value("job-123"))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void asyncReportStatusReturnsNotFoundForUnknownJob() throws Exception {
    given(asyncLearningService.getStatus("missing-job")).willReturn("NOT_FOUND");

    mockMvc
        .perform(get("/api/tasks/reports/async/missing-job"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.jobId").value("missing-job"))
        .andExpect(jsonPath("$.status").value("NOT_FOUND"));
  }
}
