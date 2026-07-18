package com.example.core.dto;

import com.example.core.annotation.AtLeastOneField;
import com.example.core.annotation.OnCreate;
import com.example.core.annotation.OnUpdate;
import com.example.core.employee.EmployeeEntity;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@AtLeastOneField(groups = {OnUpdate.class}) // 👈 Applied here for updates only!
public class EmployeeDto {

  @Null(
      groups = {OnCreate.class},
      message = "ID must be null when creating or replacing an employee")
  @NotNull(
      groups = {OnUpdate.class},
      message = "ID is required when updating an existing employee")
  private Long id;

  // Note: On OnUpdate, name is no longer strictly @NotBlank on its own.
  // It is only validated if it is actually passed (to prevent blank names).
  @NotBlank(
      groups = {OnCreate.class},
      message = "Name is required when creating or replacinga new employee")
  private String name;

  @Min(
      value = 0,
      groups = {OnCreate.class, OnUpdate.class},
      message = "Salary must be a non-negative value")
  private Double salary;

  @JsonProperty(access = Access.READ_ONLY)
  @Schema(accessMode = Schema.AccessMode.READ_ONLY)
  private String createdBy; // Optional field, can be null

  // for backwards comapatibily of tests
  public EmployeeDto(Long id, String name, Double salary) {
    this.id = id;
    this.name = name;
    this.salary = salary;
  }

  public static EmployeeDto fromEntityToDto(EmployeeEntity entity) {
    return new EmployeeDto(
        entity.getId(), entity.getName(), entity.getSalary(), entity.getCreatedBy());
  }

  public static EmployeeEntity fromDtoToEntity(EmployeeDto dto) {
    return EmployeeEntity.builder()
        .id(dto.getId())
        .name(dto.getName())
        .salary(dto.getSalary())
        .createdBy(dto.getCreatedBy())
        .build();
  }
}
