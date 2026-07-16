package com.example.core.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

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
}
