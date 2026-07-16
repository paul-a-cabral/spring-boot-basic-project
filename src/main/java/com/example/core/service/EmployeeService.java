package com.example.core.service;

import com.example.core.data.EmployeeDAO;
import com.example.core.data.EmployeeEntity;
import com.example.core.dto.EmployeeDto;
import com.example.core.exception.EmployeeNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {

  private final EmployeeDAO employeeDAO;

  EmployeeService(EmployeeDAO employeeDAO) {
    this.employeeDAO = employeeDAO;
  }

  @Transactional
  public EmployeeDto saveOrReplace(EmployeeDto dto) {
    // Convert the DTO directly to an Entity (with the ID populated from the path)
    EmployeeEntity entity = EmployeeDto.fromDtoToEntity(dto);

    // save() will automatically update if the ID exists, or insert if it doesn't
    EmployeeEntity savedEntity = employeeDAO.save(entity);

    return EmployeeDto.fromEntityToDto(savedEntity);
  }

  @Transactional
  public EmployeeDto update(EmployeeDto dto) {
    // 1. Retrieve the existing entity from the database
    EmployeeEntity entity =
        employeeDAO
            .findById(dto.getId())
            .orElseThrow(
                () -> new EntityNotFoundException("Employee not found with ID: " + dto.getId()));

    // 2. Apply updates from the DTO to the retrieved Entity
    if (dto.getName() != null) {
      entity.setName(dto.getName());
    }
    if (dto.getSalary() != null) {
      entity.setSalary(dto.getSalary());
    }

    // 3. Save it back. Spring Data automatically detects the ID and issues an
    // UPDATE query.
    EmployeeEntity updatedEntity = employeeDAO.save(entity);

    // 4. Return the updated DTO
    return EmployeeDto.fromEntityToDto(updatedEntity);
  }

  @Transactional
  public void delete(Long id) {
    // 1. Fetch the entity (1st query)
    EmployeeEntity entity =
        employeeDAO
            .findById(id)
            .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));
    employeeDAO
        .findById(id)
        .orElseThrow(() -> new EmployeeNotFoundException("Employee not found with ID: " + id));

    // 2. Delete it (2nd query)
    employeeDAO.delete(entity); // Make sure 'delete(EmployeeEntity entity)' is declared in your DAO
  }

  public Collection<? extends GrantedAuthority> getCurrentUserAuthorities() {
    // 1. Retrieve the authentication object from the security context
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    // 2. Safeguard: Check if there is an active authentication and that the user is
    // authenticated
    if (authentication == null || !authentication.isAuthenticated()) {
      return Collections.emptyList();
    }

    // 3. Return the pre-loaded authorities
    return authentication.getAuthorities();
  }
}
