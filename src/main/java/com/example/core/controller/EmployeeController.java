package com.example.core.controller;

import com.example.core.annotation.OnCreate;
import com.example.core.annotation.OnUpdate;
import com.example.core.aspect.LogExecutionTimeAspect;
import com.example.core.data.EmployeeDAO;
import com.example.core.data.EmployeeEntity;
import com.example.core.dto.EmployeeDto;
import com.example.core.exception.EmployeeNotFoundException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
@Validated
public class EmployeeController {

  private static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAspect.class);
  private final EmployeeDAO employeeDAO;

  public EmployeeController(EmployeeDAO employeeDAO) {
    this.employeeDAO = employeeDAO;
  }

  @GetMapping
  public List<EmployeeDto> getEmployees() {
    List<EmployeeEntity> employees = employeeDAO.findAll();
    logger.info("Retrieved {} employees", employees.size());
    return employees.stream().map(EmployeeDto::fromEntityToDto).toList();
  }

  @GetMapping(params = "id")
  public EmployeeDto getEmployeeByQueryParam(@RequestParam @NotNull @Min(1) Long id) {
    return EmployeeDto.fromEntityToDto(
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found")));
  }

  @GetMapping("/{id}")
  public EmployeeDto getEmployeeByPathVariable(@PathVariable @NotNull @Min(1) Long id) {
    return EmployeeDto.fromEntityToDto(
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found")));
  }

  @GetMapping("/search")
  public String searchEmployees() {
    return "Search results for employees";
  }

  @PostMapping
  public EmployeeDto createEmployee(
      @Validated(OnCreate.class) @RequestBody EmployeeDto employeeDto) {
    EmployeeEntity savedEmployee = employeeDAO.save(EmployeeDto.fromDtoToEntity(employeeDto));
    return EmployeeDto.fromEntityToDto(savedEmployee);
  }

  @PutMapping("/{id}")
  public EmployeeDto replaceEmployee(
      @PathVariable @NotNull Long id,
      @Validated(OnCreate.class) @RequestBody EmployeeDto employeeDto) {

    EmployeeEntity existingEmployee =
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

    EmployeeEntity replacementEmployee = EmployeeDto.fromDtoToEntity(employeeDto);
    replacementEmployee.setId(existingEmployee.getId());
    EmployeeEntity updatedEmployee = employeeDAO.save(replacementEmployee);

    return EmployeeDto.fromEntityToDto(updatedEmployee);
  }

  @PatchMapping("/{id}")
  public EmployeeDto patchEmployee(
      @PathVariable @NotNull @Min(1) Long id,
      @Validated(OnUpdate.class) @RequestBody EmployeeDto employeeDto) {

    EmployeeEntity existingEmployee =
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

    if (employeeDto.getName() != null) {
      existingEmployee.setName(employeeDto.getName());
    }

    if (employeeDto.getSalary() != null) {
      existingEmployee.setSalary(employeeDto.getSalary());
    }

    EmployeeEntity patchedEmployee = employeeDAO.save(existingEmployee);
    return EmployeeDto.fromEntityToDto(patchedEmployee);
  }
}
