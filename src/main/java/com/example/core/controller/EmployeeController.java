package com.example.core.controller;

import com.example.core.annotation.OnCreate;
import com.example.core.annotation.OnUpdate;
import com.example.core.annotation.ValidatePathId;
import com.example.core.aspect.LogExecutionTimeAspect;
import com.example.core.dto.EmployeeCompensationDto;
import com.example.core.dto.EmployeeDto;
import com.example.core.service.EmployeeService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

  private static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAspect.class);
  private final EmployeeService employeeService;

  public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('CAN_READ')")
  public List<EmployeeDto> getEmployees() {
    List<EmployeeDto> employees = employeeService.findAll();
    logger.info("called getEmployees() and returned {} employees", employees.size());
    return employees;
  }

  @GetMapping(params = "id")
  @PreAuthorize("hasAuthority('CAN_READ')")
  public EmployeeDto getEmployeeByQueryParam(@RequestParam @NotNull @Min(1) Long id) {
    return employeeService.findById(id);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('CAN_READ')")
  public EmployeeDto getEmployeeByPathVariable(@PathVariable @NotNull @Min(1) Long id) {
    return employeeService.findById(id);
  }

  @GetMapping("/{id}/owned")
  @PreAuthorize("isAuthenticated()")
  @PostAuthorize(
      "hasRole('ADMIN') or @employeeAuthorization.canAccessEmployee(returnObject, authentication)")
  public EmployeeDto getOwnedEmployeeById(@PathVariable @NotNull @Min(1) Long id) {
    return employeeService.findById(id);
  }

  @GetMapping("/{id}/compensation")
  @PreAuthorize("isAuthenticated()")
  @PostAuthorize(
      "hasRole('ADMIN') or @employeeAuthorization.canAccessCompensation(returnObject, authentication)")
  public EmployeeCompensationDto getEmployeeCompensation(
      @PathVariable @NotNull @Min(1) Long id) {
    return employeeService.findCompensationById(id);
  }

  @GetMapping("/audit")
  @PreAuthorize("hasAuthority('CAN_AUDIT')")
  public String auditEmployees() {
    return "Audit results for employees";
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED) // forces a 201 Created instead of 200 OK
  @PreAuthorize("hasAuthority('CAN_WRITE')")
  public EmployeeDto createEmployee(
      @Validated(OnCreate.class) @RequestBody EmployeeDto employeeDto) {
    return employeeService.save(employeeDto);
  }

  @PutMapping("/{id}")
  @ValidatePathId
  @PreAuthorize("hasAuthority('CAN_WRITE') or hasAuthority('CAN_EDIT')")
  public ResponseEntity<EmployeeDto> putEmployee(
      @PathVariable Long id, @Validated(OnUpdate.class) @RequestBody EmployeeDto dto) {
    boolean exists = employeeService.existsById(id);

    dto.setId(id);
    EmployeeDto savedDto = employeeService.saveOrReplace(dto);

    if (exists) {
      return ResponseEntity.ok(savedDto);
    } else {
      return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }
  }

  @PatchMapping("/{id}")
  @ValidatePathId
  @PreAuthorize("hasAuthority('CAN_EDIT')")
  public EmployeeDto patchEmployee(
      @PathVariable @NotNull @Min(1) Long id,
      @Validated(OnUpdate.class) @RequestBody EmployeeDto employeeDto) {

    employeeDto.setId(id); // Ensure the DTO has the correct ID from the path variable
    return employeeService.update(employeeDto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT) // 204 No Content
  @PreAuthorize(
      "hasAuthority('CAN_DELETE') or @employeeAuthorization.canDeleteOwnEmployee(#id, authentication)")
  public void deleteEmployee(@PathVariable Long id) {
    employeeService.delete(id);
  }
}
