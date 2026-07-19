package com.example.core.dto;

import com.example.core.employee.EmployeeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCompensationDto {

  private Long id;
  private Double salary;
  private String createdBy;

  public static EmployeeCompensationDto fromEntityToDto(EmployeeEntity entity) {
    return new EmployeeCompensationDto(entity.getId(), entity.getSalary(), entity.getCreatedBy());
  }
}
