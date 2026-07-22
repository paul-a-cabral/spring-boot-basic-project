package com.example.core.dto;

import com.example.core.employee.EmployeeEntity;

public record EmployeeCompensationDto(Long id, Double salary, String createdBy) {

  public static EmployeeCompensationDto fromEntityToDto(EmployeeEntity entity) {
    return new EmployeeCompensationDto(entity.getId(), entity.getSalary(), entity.getCreatedBy());
  }
}
