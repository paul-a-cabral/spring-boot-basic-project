package com.example.core.controller;

import com.example.core.service.AsyncLearningService;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  private final AsyncLearningService asyncLearningService;

  public TaskController(AsyncLearningService asyncLearningService) {
    this.asyncLearningService = asyncLearningService;
  }

  // 1. Open to anyone (no login required)
  @GetMapping("/get-something")
  public String getSomething() {
    return "Public Data: Anyone can see this!";
  }

  // 2. Requires any authenticated user (login required)
  @GetMapping("/get-something-authenticated")
  @PreAuthorize("isAuthenticated()")
  public String doSomething() {
    return "Success: You are logged in!";
  }

  // 3. Only accessible by users with 'ROLE_ADMIN'
  @PostMapping("/admin-task")
  @PreAuthorize("hasRole('ADMIN')")
  public String doAdminTask() {
    return "Success: Admin task executed!";
  }

  // 4. Only accessible by users with the raw 'AUDIT' authority/privilege
  @PostMapping("/audit-task")
  @PreAuthorize("hasAuthority('CAN_AUDIT')")
  public String doAuditTask() {
    return "Success: Audit task executed!";
  }

  @GetMapping("/me")
  public String me(Authentication authentication) {
    return authentication.getName();
  }

  // Learning requirement: report generation starts in background while API returns immediately.
  @PostMapping("/reports/async")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, String>> startAsyncReport(Authentication authentication) {
    String jobId = asyncLearningService.startReportJob(authentication.getName());
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(Map.of("jobId", jobId, "status", "IN_PROGRESS"));
  }

  @GetMapping("/reports/async/{jobId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Map<String, String>> getAsyncReportStatus(@PathVariable String jobId) {
    String status = asyncLearningService.getStatus(jobId);
    if ("NOT_FOUND".equals(status)) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("jobId", jobId, "status", status));
    }

    return ResponseEntity.ok(Map.of("jobId", jobId, "status", status));
  }
}
