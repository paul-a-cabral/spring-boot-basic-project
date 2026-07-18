package com.example.core.service;

import com.example.core.dto.EmployeeCompensationDto;
import com.example.core.dto.EmployeeDto;
import com.example.core.employee.EmployeeDAO;
import com.example.core.employee.EmployeeEntity;
import com.example.core.exception.EmployeeNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

  public List<EmployeeDto> findAll() {
    return employeeDAO.findAll().stream().map(EmployeeDto::fromEntityToDto).toList();
  }

  public boolean existsById(Long id) {
    return employeeDAO.existsById(id);
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
    EmployeeEntity entity = findEmployeeEntityOrThrow(dto.getId(), EntityNotFoundException::new);

    // 2. Apply updates from the DTO to the retrieved Entity
    if (dto.getName() != null) {
      entity.setName(dto.getName());
    }
    if (dto.getSalary() != null) {
      entity.setSalary(dto.getSalary());
    }

    // we do not modify the createdBy field on update, as it should remain the same as when the
    // entity was first created

    // 3. Save it back. Spring Data automatically detects the ID and issues an
    // UPDATE query.
    EmployeeEntity updatedEntity = employeeDAO.save(entity);

    // 4. Return the updated DTO
    return EmployeeDto.fromEntityToDto(updatedEntity);
  }

  @Transactional
  public void delete(Long id) {
    EmployeeEntity entity = findEmployeeEntityOrThrow(id, EmployeeNotFoundException::new);
    employeeDAO.delete(entity);
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

  public String getLoggedInUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.isAuthenticated()) {
      return authentication.getName(); // Returns the username of the logged-in user
    }
    return null; // No user is logged in
  }

  @Transactional
  public EmployeeDto save(EmployeeDto dto) {
    EmployeeEntity entity = EmployeeDto.fromDtoToEntity(dto);
    entity.setCreatedBy(getLoggedInUsername()); // Set the createdBy field
    EmployeeEntity savedEntity = employeeDAO.save(entity);
    return EmployeeDto.fromEntityToDto(savedEntity);
  }

  public EmployeeDto findById(Long id) {
    return EmployeeDto.fromEntityToDto(findEmployeeEntityOrThrow(id, EmployeeNotFoundException::new));
  }

  public EmployeeCompensationDto findCompensationById(Long id) {
    return EmployeeCompensationDto.fromEntityToDto(
        findEmployeeEntityOrThrow(id, EmployeeNotFoundException::new));
  }

  private <X extends RuntimeException> EmployeeEntity findEmployeeEntityOrThrow(
      Long id, java.util.function.Function<String, X> exceptionFactory) {
    return employeeDAO
        .findById(id)
        .orElseThrow(() -> exceptionFactory.apply("Employee not found with ID: " + id));
  }
}
