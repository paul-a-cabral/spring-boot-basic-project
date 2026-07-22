package com.example.core.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.config.TestJpaAuditingConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BasicBatchController.class)
@ActiveProfiles({"basic", "test"})
@Import({SecurityConfig.class, TestJpaAuditingConfig.class})
class BasicBatchControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private JobLauncher jobLauncher;

  @MockitoBean(name = "importEmployeeJob") // Specify the bean name if there are multiple Job beans
  private Job importEmployeeJob;

  @BeforeEach
  void setUp() throws Exception {
    // Mock JobExecution
    JobExecution jobExecution = new JobExecution(1L);
    jobExecution.setStatus(BatchStatus.COMPLETED);
    jobExecution.setExitStatus(ExitStatus.COMPLETED);

    when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);
  }

  // Core Review:
  // This test targets BASIC mode (@ActiveProfiles({\"basic\", \"test\"})), but
  // authenticates using a Bearer JWT header. In BASIC mode the security config
  // uses HTTP Basic, so this request is likely to be 401. Use
  // httpBasic(...)/@WithMockUser(roles=\"ADMIN\") (and remove JWT token
  // generation) to match BASIC authentication.
  @Test
  void runEmployeeBatchImport_Success() throws Exception {
    String adminToken = createTokenWithRoles("admin", "ADMIN");
    mockMvc
        .perform(
            post("/api/batch/import-employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.jobId").value(1L))
        .andExpect(jsonPath("$.status").value("COMPLETED"))
        .andExpect(jsonPath("$.exitStatus").value("COMPLETED"));
  }

  @Test
  void runEmployeeBatchImport_Unauthorized() throws Exception {
    mockMvc
        .perform(post("/api/batch/import-employees").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void runEmployeeBatchImport_Forbidden_WrongRole() throws Exception {
    String userToken = createTokenWithRoles("user", "USER");
    mockMvc
        .perform(
            post("/api/batch/import-employees")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  // Secret key for signing the JWT tokens (must match the one in
  // application-jwt.properties or JwtService)
  // For testing purposes, this can be a simple base64 encoded string.
  private static final String SECRET_KEY =
      "YmFzZTY0LWVuY29kZWQtc3VwZXItc2VjcmV0LWtleS1mb3Itand0LXNhbXBsZS0yMDI2"; // Replace

  // with
  // your
  // actual
  // secret
  // key

  private String createTokenWithRoles(String username, String... roles) {
    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);
    Date expiryDate = new Date(nowMillis + 3600000); // 1 hour expiration

    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", List.of(roles)); // Add roles as a claim

    return Jwts.builder()
        .claims(claims)
        .subject(username)
        .issuedAt(now)
        .expiration(expiryDate)
        .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY)))
        .compact();
  }
}
