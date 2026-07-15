package com.example.core.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

  // 1. Open to anyone (no login required)
  @GetMapping("/something")
  public String getSomething() {
    return "Public Data: Anyone can see this!";
  }

  // 2. Requires any authenticated user (login required)
  @PostMapping("/do-something")
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
  @PreAuthorize("hasAuthority('AUDIT')")
  public String doAuditTask() {
    return "Success: Audit task executed!";
  }

  // 5. Accessible to admins or the specific username 'superuser'
  @PostMapping("/admin-or-superuser-task")
  @PreAuthorize("hasRole('ADMIN') or authentication.name == 'superuser'")
  public String doAdminOrSuperuserTask() {
    return "Success: Admin or superuser task executed!";
  }
}
