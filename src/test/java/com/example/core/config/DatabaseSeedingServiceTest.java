package com.example.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.core.employee.EmployeeDAO;
import com.example.core.employee.EmployeeEntity;
import com.example.core.repository.UserRepository;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DatabaseSeedingServiceTest {

    @Autowired private DatabaseSeedingService databaseSeedingService;

  @Autowired private UserRepository userRepository;

  @Autowired private EmployeeDAO employeeDAO;

  @Test
  void seededEmployeesUseCreatorWithCanWritePermission() {
        databaseSeedingService.seedInitialData();

    Set<String> canWriteUsernames =
        userRepository.findAll().stream()
            .filter(user -> user.getRole() != null)
            .filter(user -> user.getRole().getPermissions() != null)
            .filter(
                user ->
                    user.getRole().getPermissions().stream()
                        .anyMatch(permission -> "CAN_WRITE".equals(permission.getCode())))
            .map(user -> user.getUsername())
            .collect(Collectors.toSet());

    assertThat(canWriteUsernames).isNotEmpty();

    var seededEmployees =
        employeeDAO.findAll().stream()
            .filter(
                employee ->
                    employee.getName() != null && employee.getName().startsWith("Employee-"))
            .toList();

    assertThat(seededEmployees).hasSize(10);
    assertThat(seededEmployees)
        .extracting(EmployeeEntity::getCreatedBy)
        .allSatisfy(createdBy -> assertThat(canWriteUsernames).contains(createdBy));
  }
}
