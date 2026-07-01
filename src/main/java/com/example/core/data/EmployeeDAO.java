package com.example.core.data;

import java.util.List;

import org.springframework.data.repository.RepositoryDefinition;

@RepositoryDefinition(
        domainClass = EmployeeEntity.class,
        idClass = Long.class
)
public interface EmployeeDAO extends EmployeeCustomDAO {
    // This interface extends the custom implementation to provide additional query methods
    public void save(EmployeeEntity employee);
    public List<EmployeeEntity> findAll();
}
