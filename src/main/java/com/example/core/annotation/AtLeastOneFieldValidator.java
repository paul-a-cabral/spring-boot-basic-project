package com.example.core.annotation;

import com.example.core.dto.EmployeeDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOneFieldValidator implements ConstraintValidator<AtLeastOneField, EmployeeDto> {

  @Override
  public boolean isValid(EmployeeDto dto, ConstraintValidatorContext context) {
    if (dto == null) {
      return true;
    }

    boolean hasName = dto.getName() != null && !dto.getName().isBlank();
    boolean hasSalary = dto.getSalary() != null;

    // Return true if at least one field has a value
    return hasName || hasSalary;
  }
}
