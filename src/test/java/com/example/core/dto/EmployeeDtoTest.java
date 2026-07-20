package com.example.core.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.example.core.employee.EmployeeEntity;
import org.junit.jupiter.api.Test;

class EmployeeDtoTest {

  @Test
  void testDefaultConstructor() {
    EmployeeDto dto = new EmployeeDto();
    assertNull(dto.getId());
    assertNull(dto.getName());
    assertNull(dto.getSalary());
  }

  @Test
  void testParameterizedConstructor() {
    Long expectedId = 1L;
    String expectedName = "John Doe";
    Double expectedSalary = 50000.0;

    EmployeeDto dto = new EmployeeDto(expectedId, expectedName, expectedSalary);

    assertEquals(expectedId, dto.getId());
    assertEquals(expectedName, dto.getName());
    assertEquals(expectedSalary, dto.getSalary());
  }

  @Test
  void testSetAndGetId() {
    Long testId = 5L;
    EmployeeDto employeeDto = new EmployeeDto().withId(testId);
    assertEquals(testId, employeeDto.getId());
  }

  @Test
  void testSetAndGetName() {
    String testName = "Jane Smith";
    EmployeeDto employeeDto = new EmployeeDto(null, testName, null);
    assertEquals(testName, employeeDto.getName());
  }

  @Test
  void testSetAndGetSalary() {
    Double testSalary = 75000.0;
    EmployeeDto employeeDto = new EmployeeDto(null, null, testSalary);
    assertEquals(testSalary, employeeDto.getSalary());
  }

  @Test
  void testFromEntityToDto() {
    Long id = 1L;
    String name = "Alice Johnson";
    Double salary = 60000.0;

    EmployeeEntity entity = new EmployeeEntity(id, name, salary);
    EmployeeDto dto = EmployeeDto.fromEntityToDto(entity);

    assertEquals(id, dto.getId());
    assertEquals(name, dto.getName());
    assertEquals(salary, dto.getSalary());
  }

  @Test
  void testFromEntityToDtoWithNullValues() {
    EmployeeEntity entity = new EmployeeEntity(null, null, null);
    EmployeeDto dto = EmployeeDto.fromEntityToDto(entity);

    assertNull(dto.getId());
    assertNull(dto.getName());
    assertNull(dto.getSalary());
  }

  @Test
  void testFromDtoToEntity() {
    Long id = 2L;
    String name = "Bob Wilson";
    Double salary = 55000.0;

    EmployeeDto dto = new EmployeeDto(id, name, salary);
    EmployeeEntity entity = EmployeeDto.fromDtoToEntity(dto);

    assertEquals(id, entity.getId());
    assertEquals(name, entity.getName());
    assertEquals(salary, entity.getSalary());
  }

  @Test
  void testFromDtoToEntityWithNullValues() {
    EmployeeDto dto = new EmployeeDto(null, null, null);
    EmployeeEntity entity = EmployeeDto.fromDtoToEntity(dto);

    assertNull(entity.getId());
    assertNull(entity.getName());
    assertNull(entity.getSalary());
  }

  @Test
  void testBidirectionalConversion() {
    Long originalId = 3L;
    String originalName = "Charlie Brown";
    Double originalSalary = 70000.0;

    EmployeeEntity originalEntity = new EmployeeEntity(originalId, originalName, originalSalary);
    EmployeeDto dto = EmployeeDto.fromEntityToDto(originalEntity);
    EmployeeEntity convertedEntity = EmployeeDto.fromDtoToEntity(dto);

    assertEquals(originalEntity.getId(), convertedEntity.getId());
    assertEquals(originalEntity.getName(), convertedEntity.getName());
    assertEquals(originalEntity.getSalary(), convertedEntity.getSalary());
  }

  @Test
  void testAllFieldsCanBeSet() {
    Long id = 4L;
    String name = "Diana Prince";
    Double salary = 85000.0;

    EmployeeDto employeeDto = new EmployeeDto(id, name, salary);

    assertEquals(id, employeeDto.getId());
    assertEquals(name, employeeDto.getName());
    assertEquals(salary, employeeDto.getSalary());
  }

  @Test
  void testNullSalaryHandling() {
    Long id = 5L;
    String name = "Eve Davis";

    EmployeeDto dto = new EmployeeDto(id, name, null);

    assertEquals(id, dto.getId());
    assertEquals(name, dto.getName());
    assertNull(dto.getSalary());
  }
}
