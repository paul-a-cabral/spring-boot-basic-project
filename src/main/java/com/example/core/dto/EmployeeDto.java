package com.example.core.dto;

import com.example.core.data.EmployeeEntity;

public class EmployeeDto {
    private Long id;
    private String name;
    private Double salary;

    public EmployeeDto() {
    }

    public EmployeeDto(Long id, String name, Double salary) {
        this.id = id;
        this.name = name;
        this.salary = salary;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public static EmployeeDto fromEntityToDto(EmployeeEntity entity) {
        return new EmployeeDto(entity.getId(), entity.getName(), entity.getSalary());
    }

    public static EmployeeEntity fromDtoToEntity(EmployeeDto dto) {
        return new EmployeeEntity(dto.getId(), dto.getName(), dto.getSalary());
    }

}

