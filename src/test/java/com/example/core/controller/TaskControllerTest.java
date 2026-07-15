package com.example.core.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(SecurityConfig.class)
class TaskControllerTest {

  @MockitoBean private JpaMetamodelMappingContext jpaMetamodelMappingContext;

  @Autowired private MockMvc mockMvc;

  @Test
  void getSomethingIsPublic() throws Exception {
    mockMvc
        .perform(get("/api/tasks/something"))
        .andExpect(status().isOk())
        .andExpect(content().string("Public Data: Anyone can see this!"));
  }

  @Test
  void doSomethingReturnsUnauthorizedWhenNotAuthenticated() throws Exception {
    mockMvc.perform(post("/api/tasks/do-something")).andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void doSomethingReturnsOkWhenAuthenticated() throws Exception {
    mockMvc
        .perform(post("/api/tasks/do-something"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: You are logged in!"));
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void adminTaskReturnsForbiddenWhenRoleIsNotAdmin() throws Exception {
    mockMvc.perform(post("/api/tasks/admin-task")).andExpect(status().isForbidden());
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
    mockMvc.perform(post("/api/tasks/audit-task")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "auditor", authorities = "AUDIT")
  void auditTaskReturnsOkWhenAuditAuthorityPresent() throws Exception {
    mockMvc
        .perform(post("/api/tasks/audit-task"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: Audit task executed!"));
  }

  @Test
  void adminOrSuperuserTaskReturnsUnauthorizedWhenNotAuthenticated() throws Exception {
    mockMvc
        .perform(post("/api/tasks/admin-or-superuser-task"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(username = "user", roles = "USER")
  void adminOrSuperuserTaskReturnsForbiddenForNonAdminAndNonSuperuser() throws Exception {
    mockMvc.perform(post("/api/tasks/admin-or-superuser-task")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin", roles = "ADMIN")
  void adminOrSuperuserTaskReturnsOkForAdmin() throws Exception {
    mockMvc
        .perform(post("/api/tasks/admin-or-superuser-task"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: Admin or superuser task executed!"));
  }

  @Test
  @WithMockUser(username = "superuser", roles = "USER")
  void adminOrSuperuserTaskReturnsOkForSuperuserUsername() throws Exception {
    mockMvc
        .perform(post("/api/tasks/admin-or-superuser-task"))
        .andExpect(status().isOk())
        .andExpect(content().string("Success: Admin or superuser task executed!"));
  }
}
