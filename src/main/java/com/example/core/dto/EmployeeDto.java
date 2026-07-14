package com.example.core.dto;

import com.example.core.annotation.OnCreate;
import com.example.core.annotation.OnUpdate;
import com.example.core.data.EmployeeEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

// Drops null fields for this class only
// for global settings see application.properties
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeDto {

  @Null(
      groups = {OnCreate.class},
      message = "ID must be null when creating or replacing an employee")
  @NotNull(
      groups = {OnUpdate.class},
      message = "ID is required when updating an existing employee")
  private Long id;

  @NotBlank(
      groups = {OnCreate.class},
      message = "Name is required when creating a new employee")
  private String name;

  @Min(
      value = 0,
      groups = {OnCreate.class, OnUpdate.class},
      message = "Salary must be a non-negative value")
  private Double salary;

  public EmployeeDto() {}

  public EmployeeDto(Long id, String name, Double salary) {
    this.id = id;
    this.name = name;
    this.salary = salary;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Double getSalary() {
    return salary;
  }

  public void setSalary(Double salary) {
    this.salary = salary;
  }

  public static EmployeeDto fromEntityToDto(EmployeeEntity entity) {
    return new EmployeeDto(entity.getId(), entity.getName(), entity.getSalary());
  }

  public static EmployeeEntity fromDtoToEntity(EmployeeDto dto) {
    return new EmployeeEntity(dto.getId(), dto.getName(), dto.getSalary());
  }
}
