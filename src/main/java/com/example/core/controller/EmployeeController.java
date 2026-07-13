package com.example.core.controller;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.core.aspect.LogExecutionTimeAspect;
import com.example.core.data.EmployeeDAO;
import com.example.core.dto.EmployeeDto;
import com.example.core.data.EmployeeEntity;
import com.example.core.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

     private static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAspect.class);
    private final EmployeeDAO employeeDAO;

    public EmployeeController(EmployeeDAO employeeDAO) {
        this.employeeDAO = employeeDAO;
    }

    @GetMapping({"", "/"})
    public List<EmployeeDto> getEmployees() {
        List<EmployeeEntity> employees = employeeDAO.findAll();
        logger.info("Retrieved {} employees", employees.size());
        return employees.stream()
                .map(EmployeeDto::fromEntityToDto)
                .toList();
    }

    @GetMapping(params = "id")
    public EmployeeDto getEmployeeByQueryParam(@RequestParam(required = false) Long id) {

        if (id == null) {
            throw new ResourceNotFoundException(
                    "Employee id is a required query parameter");
        }

        logger.info("As a query parameter, id has value {}", id);

        return EmployeeDto.fromEntityToDto(
                employeeDAO.findById(id).orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found")));
    }

    @GetMapping("/{id}")
    public EmployeeDto getEmployeeByPathVariable(@PathVariable Long id) {

        if (id == null) {
            throw new ResourceNotFoundException(
                    "Employee id is a required path variable");
        }

        logger.info("As path variable, id has value ", id);

        return EmployeeDto.fromEntityToDto(
                employeeDAO.findById(id).orElseThrow(
                        () -> new ResourceNotFoundException("Employee not found")));
    }

    @GetMapping("/search")
    public String searchEmployees() {
        return "Search results for employees";
    }

    @PostMapping
    public EmployeeDto createEmployee(@RequestBody EmployeeDto employeeDto) {
        EmployeeEntity savedEmployee = employeeDAO.save(EmployeeDto.fromDtoToEntity(employeeDto));
        return EmployeeDto.fromEntityToDto(savedEmployee);
    }

}