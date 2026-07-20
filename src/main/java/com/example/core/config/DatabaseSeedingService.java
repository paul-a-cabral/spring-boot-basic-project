package com.example.core.config;

import com.example.core.annotation.LogExecutionTime;
import com.example.core.employee.EmployeeDAO;
import com.example.core.employee.EmployeeEntity;
import com.example.core.entity.Role;
import com.example.core.entity.User;
import com.example.core.repository.UserRepository;
import com.example.core.service.RoleSeedService;
import com.example.service.CourseCacheWarmupService;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DatabaseSeedingService {

  private final UserRepository userRepository;
  private final RoleSeedService roleSeedService;
  private final CourseCacheWarmupService courseCacheWarmupService;
  private final EmployeeDAO employeeDAO;
  private final PasswordEncoder passwordEncoder;

  public DatabaseSeedingService(
      UserRepository userRepository,
      RoleSeedService roleSeedService,
      CourseCacheWarmupService courseCacheWarmupService,
      EmployeeDAO employeeDAO,
      PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.roleSeedService = roleSeedService;
    this.courseCacheWarmupService = courseCacheWarmupService;
    this.employeeDAO = employeeDAO;
    this.passwordEncoder = passwordEncoder;
  }

  @LogExecutionTime
  @EventListener(classes = ApplicationReadyEvent.class)
  public void seedInitialData() {
    // Check if the database is already populated to prevent duplicate insertions on restart.
    if (userRepository.count() == 0) {

      Stream.of(
              new UserRecord("admin", "ADMIN"),
              new UserRecord("auditor", "AUDITOR"),
              new UserRecord("user", "USER"),
              new UserRecord("superuser", "SUPERUSER"),
              new UserRecord("guest", "GUEST"))
          .forEach(
              userRecord -> {
                Role role = roleSeedService.getRequiredRoleByCode(userRecord.roleCode());

                User user = new User();
                user.setUsername(userRecord.username());
                user.setPassword(passwordEncoder.encode(userRecord.username()));
                user.setRole(role);
                userRepository.save(user);
              });

      System.out.println(">> Database successfully seeded with default security accounts!");
    } else {
      System.out.println(">> Database already contains users. Skipping initialization.");
    }

    long existingDefaultEmployees =
      employeeDAO.findAll().stream()
        .filter(employee -> employee.getName() != null)
        .filter(employee -> employee.getName().startsWith("Employee-"))
        .count();

    if (existingDefaultEmployees < 10) {
      List<String> canWriteUsernames = resolveCanWriteUsernames();
      long missingDefaultEmployees = 10 - existingDefaultEmployees;

      List<EmployeeEntity> employees =
        IntStream.rangeClosed((int) existingDefaultEmployees + 1, (int) (existingDefaultEmployees + missingDefaultEmployees))
          .boxed()
              .map(
                  i ->
                      EmployeeEntity.builder()
                          .name("Employee-" + i)
                          .salary(50000.0 + (i * 1000))
                          .createdBy(randomUsername(canWriteUsernames))
                          .build())
              .toList();

      employees.forEach(employeeDAO::save);
      System.out.println(">> Database successfully seeded missing default employee records!");
    } else {
      System.out.println(
          ">> Database already contains default employee records. Skipping employee initialization.");
    }

    courseCacheWarmupService.warmCourseCache();
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void onApplicationReady() {
    System.out.println(
        ">> Application ready. Current row counts - users: "
            + userRepository.count()
            + ", employees: "
            + employeeDAO.findAll().size());
  }

  private List<String> resolveCanWriteUsernames() {
    List<String> canWriteUsernames =
        userRepository.findAll().stream()
            .filter(user -> user.getRole() != null)
            .filter(user -> user.getRole().getPermissions() != null)
            .filter(
                user ->
                    user.getRole().getPermissions().stream()
                        .anyMatch(permission -> "CAN_WRITE".equals(permission.getCode())))
            .map(User::getUsername)
            .toList();

    if (canWriteUsernames.isEmpty()) {
      throw new IllegalStateException(
          "Cannot seed employees: no user found with CAN_WRITE permission");
    }

    return canWriteUsernames;
  }

  private String randomUsername(List<String> usernames) {
    return usernames.get(ThreadLocalRandom.current().nextInt(usernames.size()));
  }

  private record UserRecord(String username, String roleCode) {}
}
