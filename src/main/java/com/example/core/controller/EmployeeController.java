package com.example.core.controller;

import com.example.core.annotation.OnCreate;
import com.example.core.annotation.OnUpdate;
import com.example.core.annotation.ValidatePathId;
import com.example.core.aspect.LogExecutionTimeAspect;
import com.example.core.data.EmployeeDAO;
import com.example.core.data.EmployeeEntity;
import com.example.core.dto.EmployeeDto;
import com.example.core.exception.EmployeeNotFoundException;
import com.example.core.service.EmployeeService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
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
  private final EmployeeDAO employeeDAO;
  private final EmployeeService employeeService;

  public EmployeeController(EmployeeDAO employeeDAO, EmployeeService employeeService) {
    this.employeeDAO = employeeDAO;
    this.employeeService = employeeService;
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasAuthority('CAN_READ')")
  public List<EmployeeDto> getEmployees() {
    List<EmployeeEntity> employees = employeeDAO.findAll();
    logger.info("called getEmployees() and returned {} employees", employees.size());
    return employees.stream().map(EmployeeDto::fromEntityToDto).toList();
  }

  @GetMapping(params = "id")
  @PreAuthorize("hasAuthority('CAN_READ')")
  public EmployeeDto getEmployeeByQueryParam(@RequestParam @NotNull @Min(1) Long id) {
    return EmployeeDto.fromEntityToDto(
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found")));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('CAN_READ')")
  public EmployeeDto getEmployeeByPathVariable(@PathVariable @NotNull @Min(1) Long id) {
    return EmployeeDto.fromEntityToDto(
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found")));
  }

  @GetMapping("/audit")
  @PreAuthorize("hasAuthority('CAN_AUDIT')")
  public String auditEmployees() {
    return "Audit results for employees";
  }

  // returns the list of authorities assigned to the logged in user.
  @GetMapping("/authorities")
  public List<String> getAuthorities() {
    List<String> authorities =
        employeeService.getCurrentUserAuthorities().stream()
            .map(GrantedAuthority::getAuthority) // Clean lambda reference
            .toList();

    logger.info("Current user authorities: {}", authorities);
    return authorities;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED) // forces a 201 Created instead of 200 OK
  @PreAuthorize("hasAuthority('CAN_WRITE')")
  public EmployeeDto createEmployee(
      @Validated(OnCreate.class) @RequestBody EmployeeDto employeeDto) {
    EmployeeEntity savedEmployee = employeeDAO.save(EmployeeDto.fromDtoToEntity(employeeDto));
    return EmployeeDto.fromEntityToDto(savedEmployee);
  }

  @PutMapping("/{id}")
  @ValidatePathId
  @PreAuthorize("hasAuthority('CAN_WRITE') or hasAuthority('CAN_EDIT')")
  public ResponseEntity<EmployeeDto> putEmployee(
      @PathVariable Long id, @Validated(OnUpdate.class) @RequestBody EmployeeDto dto) {
    // 1. Check if it exists before saving
    boolean exists = employeeDAO.existsById(id);

    // 2. Perform the replace / create operation
    dto.setId(id);
    EmployeeDto savedDto = employeeService.saveOrReplace(dto);

    // 3. Return 200 OK if updated, or 201 Created if brand new
    if (exists) {
      return ResponseEntity.ok(savedDto); // Status 200
    } else {
      return ResponseEntity.status(HttpStatus.CREATED).body(savedDto); // Status 201
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
  @PreAuthorize("hasAuthority('CAN_DELETE')")
  public void deleteEmployee(@PathVariable Long id) {
    employeeService.delete(id);
  }
}
