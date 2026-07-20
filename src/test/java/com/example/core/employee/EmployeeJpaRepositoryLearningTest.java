package com.example.core.employee;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class EmployeeJpaRepositoryLearningTest {

  @Autowired private EmployeeJpaRepositoryLearning learningRepository;
  @Autowired private EmployeeDAO employeeDAO;

  @Test
  void derivedQueryAndExplicitQueryReturnSameOrderedResult() {
    learningRepository.saveAll(
        List.of(
            EmployeeEntity.builder().name("BelowThreshold").salary(40_000d).build(),
            EmployeeEntity.builder().name("QualifiedA").salary(55_000d).build(),
            EmployeeEntity.builder().name("QualifiedB").salary(70_000d).build()));

    Double minSalary = 50_000d;

    List<EmployeeEntity> derived =
        learningRepository.findBySalaryGreaterThanEqualOrderByIdAsc(minSalary);
    List<EmployeeEntity> explicit = learningRepository.findBySalaryAtLeastOrderedById(minSalary);

    assertThat(derived).hasSize(2);
    assertThat(derived)
        .extracting(EmployeeEntity::getSalary)
        .allMatch(salary -> salary >= minSalary);
    assertThat(derived).extracting(EmployeeEntity::getId).isSorted();

    assertThat(explicit).hasSize(2);
    assertThat(explicit)
        .extracting(EmployeeEntity::getSalary)
        .allMatch(salary -> salary >= minSalary);
    assertThat(explicit).extracting(EmployeeEntity::getId).isSorted();

    assertThat(derived)
        .extracting(EmployeeEntity::getId)
        .containsExactlyElementsOf(explicit.stream().map(EmployeeEntity::getId).toList());
  }

  @Test
  void derivedQueryAndCustomImplementationReturnSameOrderedResult() {
    learningRepository.saveAll(
        List.of(
            EmployeeEntity.builder().name("BelowThreshold").salary(40_000d).build(),
            EmployeeEntity.builder().name("QualifiedA").salary(55_000d).build(),
            EmployeeEntity.builder().name("QualifiedB").salary(70_000d).build()));

    Double minSalary = 50_000d;

    List<EmployeeEntity> derived =
        learningRepository.findBySalaryGreaterThanEqualOrderByIdAsc(minSalary);
    List<EmployeeEntity> custom = employeeDAO.findBySalaryGreaterThanEqualOrderByIdAsc(minSalary);

    assertThat(derived).hasSize(2);
    assertThat(derived)
        .extracting(EmployeeEntity::getSalary)
        .allMatch(salary -> salary >= minSalary);
    assertThat(derived).extracting(EmployeeEntity::getId).isSorted();

    assertThat(custom).hasSize(2);
    assertThat(custom)
        .extracting(EmployeeEntity::getSalary)
        .allMatch(salary -> salary >= minSalary);
    assertThat(custom).extracting(EmployeeEntity::getId).isSorted();

    assertThat(derived)
        .extracting(EmployeeEntity::getId)
        .containsExactlyElementsOf(custom.stream().map(EmployeeEntity::getId).toList());
  }
}
