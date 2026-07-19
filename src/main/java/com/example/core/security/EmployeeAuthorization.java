package com.example.core.security;

import com.example.core.dto.EmployeeCompensationDto;
import com.example.core.dto.EmployeeDto;
import com.example.core.employee.EmployeeDAO;
import com.example.core.employee.EmployeeEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("employeeAuthorization")
public class EmployeeAuthorization {

  private final EmployeeDAO employeeDAO;

  public EmployeeAuthorization(EmployeeDAO employeeDAO) {
    this.employeeDAO = employeeDAO;
  }

  public boolean canDeleteOwnEmployee(Long employeeId, Authentication authentication) {
    if (employeeId == null || authentication == null || authentication.getName() == null) {
      return false;
    }

    return employeeDAO
        .findById(employeeId)
        .map(EmployeeEntity::getCreatedBy)
        .filter(createdBy -> createdBy != null && createdBy.equals(authentication.getName()))
        .isPresent();
  }

  public boolean canAccessEmployee(EmployeeDto employee, Authentication authentication) {
    if (employee == null || authentication == null || authentication.getName() == null) {
      return false;
    }

    String createdBy = employee.getCreatedBy();
    return createdBy != null && createdBy.equals(authentication.getName());
  }

  public boolean canAccessCompensation(
      EmployeeCompensationDto compensation, Authentication authentication) {
    if (compensation == null || authentication == null || authentication.getName() == null) {
      return false;
    }

    String createdBy = compensation.getCreatedBy();
    return createdBy != null && createdBy.equals(authentication.getName());
  }
}
