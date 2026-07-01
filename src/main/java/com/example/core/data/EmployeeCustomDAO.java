package com.example.core.data;

import java.util.List;

public interface EmployeeCustomDAO {
    // fetch employee details by salary such that salary is greater than or equal to certain value, and the result should be ordered in increasing order of employee id
    public List<EmployeeEntity> findBySalaryGreaterThanEqualOrderByIdAsc(Double salary);
}
