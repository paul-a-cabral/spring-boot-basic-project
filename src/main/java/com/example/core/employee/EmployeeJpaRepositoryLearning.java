package com.example.core.employee;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Learning-only repository to demonstrate Spring Data JPA derived queries and @Query.
 *
 * <p>This exists alongside EmployeeDAO/EmployeeCustomImpl on purpose.
 */
public interface EmployeeJpaRepositoryLearning extends JpaRepository<EmployeeEntity, Long> {

  // Derived query method: Spring Data parses the name and builds the query automatically.
  List<EmployeeEntity> findBySalaryGreaterThanEqualOrderByIdAsc(Double salary);

  // Equivalent explicit JPQL query.
  @Query("SELECT e FROM Employee e WHERE e.salary >= :salary ORDER BY e.id ASC")
  List<EmployeeEntity> findBySalaryAtLeastOrderedById(@Param("salary") Double salary);
}
