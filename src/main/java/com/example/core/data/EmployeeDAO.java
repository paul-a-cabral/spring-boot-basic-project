package com.example.core.data;

import java.util.List;

import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(
        domainClass = EmployeeEntity.class,
        idClass = Long.class
)
public interface EmployeeDAO extends EmployeeCustomDAO {
    // This interface extends the custom implementation to provide additional query methods
    public EmployeeEntity save(EmployeeEntity employee);
    public List<EmployeeEntity> findAll();
    public java.util.Optional<EmployeeEntity> findById(Long id);
}
