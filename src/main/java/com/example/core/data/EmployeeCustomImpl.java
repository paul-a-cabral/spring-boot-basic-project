package com.example.core.data;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

public class EmployeeCustomImpl implements EmployeeCustomDAO {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<EmployeeEntity> findBySalaryGreaterThanEqualOrderByIdAsc(Double salary) {
        String query = "SELECT e FROM EmployeeEntity e WHERE e.salary >= :salary ORDER BY e.id ASC";
        return entityManager.createQuery(query, EmployeeEntity.class)
                .setParameter("salary", salary)
                .getResultList();
    }

}
