package com.tvscs.bgv.repository;

import com.tvscs.bgv.domain.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e WHERE UPPER(e.employeeId) = UPPER(:employeeId)")
    Optional<Employee> findByEmployeeIdIgnoreCase(String employeeId);

    boolean existsByEmployeeIdIgnoreCase(String employeeId);
}
