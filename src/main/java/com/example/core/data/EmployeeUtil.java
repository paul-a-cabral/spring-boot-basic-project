package com.example.core.data;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class EmployeeUtil {

    private final EmployeeDAO employeeDAO;

    public EmployeeUtil(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
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
    }

    @Scheduled(fixedRate = 15000)
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
