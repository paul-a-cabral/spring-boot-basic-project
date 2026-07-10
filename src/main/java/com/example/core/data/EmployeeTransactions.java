package com.example.core.data;

import com.example.core.data.MyEmployeeException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmployeeTransactions {

    private final EmployeeDAO employeeDAO;

    public EmployeeTransactions(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    // @EventListener(classes = ApplicationReadyEvent.class)
    void performTransaction() {
        System.out.println("[BThereEBThereEThereFOREThere are currently " + employeeDAO.findAll().size()
                + " employee(s) in the db");
        try {
            System.out.println("### Performing a rolled back transaction...");
            withRuntimeException();
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
            // Rollback transaction
            System.out.println("There are currently " + employeeDAO.findAll().size() + " employee(s) in the db");
            System.out.println("Expecting to have 0");
        }
    }

    @Transactional
    void withRuntimeException() {
        // Start
        EmployeeEntity employee1 = new EmployeeEntity();
        employee1.setName("Transaction-Employee-1");
        employee1.setSalary(60000.0);
        saveAndThrowRuntimeException(employee1, false);

        // Simulate an error to trigger rollback

        EmployeeEntity employee2 = new EmployeeEntity();
        employee2.setName("Transaction-Employee-2");
        employee2.setSalary(65000.0);
        saveAndThrowRuntimeException(employee2, true);
    }

    @Transactional
    void withCheckedException() throws MyEmployeeException {
        // Start transaction
        EmployeeEntity employee1 = new EmployeeEntity();
        employee1.setName("Transaction-Employee-11");
        employee1.setSalary(60000.0);
        saveWithConditionalCheckedException(employee1, false);

        // Simulate an error (transactions should commit)

        EmployeeEntity employee2 = new EmployeeEntity();
        employee2.setName("Transaction-Employee-12");
        employee2.setSalary(65000.0);
        saveWithConditionalCheckedException(employee2, true);
    }

    @Transactional(rollbackFor = MyEmployeeException.class)
    void withCheckedExceptionButRollsback() throws MyEmployeeException {
        // Start transaction
        EmployeeEntity employee1 = new EmployeeEntity();
        employee1.setName("Transaction-Employee-11");
        employee1.setSalary(60000.0);
        saveWithConditionalCheckedException(employee1, false);

        // Simulate an error (transactions should commit)

        EmployeeEntity employee2 = new EmployeeEntity();
        employee2.setName("Transaction-Employee-12");
        employee2.setSalary(65000.0);
        saveWithConditionalCheckedException(employee2, true);
    }

    // Will roll back the transaction if the flag is true, otherwise it will commit
    // the transaction
    private void saveAndThrowRuntimeException(EmployeeEntity employee, boolean toThrowException) {
        employeeDAO.save(employee);
        if (toThrowException) {
            throw new RuntimeException("!!! Simulated runtime exception !!!");
        }
    }

    // Unused cchecked-exceptionhecked-exception variant for future employee
    // transaction handling.
    private void saveWithConditionalCheckedException(EmployeeEntity employee, boolean shouldThrow)
            throws MyEmployeeException {
        employeeDAO.save(employee);
        if (shouldThrow) {
            throw new MyEmployeeException("Simulated checked employee exception");
        }
    }
}
