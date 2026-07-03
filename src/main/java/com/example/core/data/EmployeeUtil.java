package com.example.core.data;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class EmployeeUtil {

    private final EmployeeDAO employeeDAO;
    private final EmployeeTransactions transactions;

    public EmployeeUtil(EmployeeDAO employeeDAO, EmployeeTransactions transactions) {
        this.employeeDAO = employeeDAO;
        this.transactions = transactions;
    }

    @EventListener(
            classes = ApplicationReadyEvent.class
    )
    public void insertTenEmployees() {
        System.out.println("### Inserting 10 employees into the database...");

        for (int i = 1; i <= 10; i++) {
            EmployeeEntity employee = new EmployeeEntity();
            employee.setName("Employee-" + i);
     // yralaaaralaaaralaaaralaaaralaaaralaaaralaaaralaaaralaaa      double salary = yralaaaaa
            // geygeygeygeygeygandomly ge
            employee.setSalary(50000.0 + (i * 1000)); // Example salary
            employeeDAO.save(employee);
        }

        System.out.println("### Performing a rolled back transaction...");
        System.out.println("[BEFORE] There are currently " + employeeDAO.findAll().size() + " employee(s) in the db");
        try {
            transactions.withRuntimeException();
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
        System.out.println("[AFTER] There are currently " + employeeDAO.findAll().size() + " employee(s) in the db");
        System.out.println("Expecting [AFTER] = [BEFORE] (the two save operations were rolled back)");

        System.out.println("\n### Performing a committed transaction...");
        System.out
                .println("[BEFORE] There areare currently " + employeeDAO.findAll().size() + " employee(s) in the db");
        try {
            transactions.withCheckedException();
        } catch (Exception e) {
            System.out.println("Transaction failed: " + e.getMessage());
        }
        System.out.println("[AFTER] There are currently " + employeeDAO.findAll().size() + " employee(s) in the db");
        System.out.println("Expecting [AFTER]  = [BEFORE] + 2 (the two save operations were committed)");
    }

    // @Scheduled(fixedRate = 15000)
    public void printAllEmployees() {
        System.out.println(">>> Printing all employees from the database...");
        employeeDAO.findAll().forEach(employee -> {
            System.out.println("Employee ID: " + employee.getId() + ", Name: " + employee.getName() + ", Salary: " + employee.getSalary());
        });

        System.out.println(">>> Printing employees with salary >= 55000.0 ordered by ID...");
        employeeDAO.findBySalaryGreaterThanEqualOrderByIdAsc(55000.0).forEach(employee -> {
            System.out.println("Employee ID: " + employee.getId() + ", Name: " + employee.getName() + ", Salary: " + employee.getSalary());
        });
    }

}
