package com.example.core.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.core.config.SecurityConfig;
import com.example.core.config.TestJpaAuditingConfig;
import com.example.core.security.JwtService;
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
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(JwtBatchController.class)
@ActiveProfiles({"jwt", "test"})
@Import({SecurityConfig.class, TestJpaAuditingConfig.class})
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class JwtBatchControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean(name = "asyncJobLauncher")
  private JobLauncher jobLauncher;

  // Specify the bean name if there are multiple Job beans
  @MockitoBean(name = "importEmployeeJob")
  private Job importEmployeeJob;

  @MockitoBean private JobExplorer jobExplorer;

  // ⚠️ Required dependencies for SecurityConfig in JWT mode:
  @MockitoBean private JwtService jwtService;

  // Code Review:
  // In JWT mode the JwtAuthenticationFilter typically relies on
  // JwtService/UserDetailsService to validate the token and build the
  // Authentication. Since both are mocked but not stubbed, authentication will
  // likely fail and this test may return 401/403. Either stub the mocks to accept
  // adminToken and return an ADMIN-authenticated user, or bypass the filter for
  // the test (e.g., @WithMockUser(roles=\"ADMIN\") if your config allows it).
  @MockitoBean private UserDetailsService userDetailsService;

  @BeforeEach
  void setUp() throws Exception {
    // Mock JobExecution to isolate the controller logic
    JobExecution jobExecution = new JobExecution(1L);
    jobExecution.setStatus(BatchStatus.STARTING);
    jobExecution.setExitStatus(ExitStatus.UNKNOWN);

    when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);
  }

  @Test
  void runEmployeeBatchImport_Success() throws Exception {
    String adminToken = createTokenWithRoles("admin", "ADMIN");
    mockMvc
        .perform(
            post("/api/batch/import-employees-async")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.jobId").value(1L))
        .andExpect(jsonPath("$.status").value("STARTING"))
        .andExpect(jsonPath("$.exitStatus").value("UNKNOWN"));
  }

  @Test
  void runEmployeeBatchImport_Unauthorized() throws Exception {
    mockMvc
        .perform(post("/api/batch/import-employees-async").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void runEmployeeBatchImport_Forbidden_WrongRole() throws Exception {
    String userToken = createTokenWithRoles("user", "USER");
    mockMvc
        .perform(
            post("/api/batch/import-employees-async")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isForbidden());
  }

  // Secret key for signing the JWT tokens (must match the one in
  // application-jwt.properties or
  // JwtService)
  private static final String SECRET_KEY =
      "YmFzZTY0LWVuY29kZWQtc3VwZXItc2VjcmV0LWtleS1mb3Itand0LXNhbXBsZS0yMDI2";

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
