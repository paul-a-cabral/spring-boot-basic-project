package com.example.core.employee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@Sql(statements = "DELETE FROM Employee", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class EmployeeTransactionsTest {

  @Autowired private EmployeeDAO employeeDAO;

  @Autowired private EmployeeTransactions employeeTransactions;

  @BeforeEach
  void verifyCleanState() {
    assertThat(employeeDAO.findAll()).isEmpty();
  }

  @Test
  void runtimeExceptionRollsBackBothInserts() {
    assertThatThrownBy(() -> employeeTransactions.withRuntimeException())
        .isInstanceOf(RuntimeException.class)
        .hasMessageContaining("Simulated runtime exception");

    assertThat(employeeDAO.findAll()).isEmpty();
  }

  @Test
  void checkedExceptionWithoutRollbackRuleCommits() {
    assertThatThrownBy(() -> employeeTransactions.withCheckedException())
        .isInstanceOf(MyEmployeeException.class)
        .hasMessageContaining("Simulated checked employee exception");

    assertThat(employeeDAO.findAll()).hasSize(2);
  }

  @Test
  void checkedExceptionWithRollbackRuleRollsBack() {
    assertThatThrownBy(() -> employeeTransactions.withCheckedExceptionButRollsback())
        .isInstanceOf(MyEmployeeException.class)
        .hasMessageContaining("Simulated checked employee exception");

    assertThat(employeeDAO.findAll()).isEmpty();
  }
}
