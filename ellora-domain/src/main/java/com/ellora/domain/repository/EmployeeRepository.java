package com.ellora.domain.repository;

import com.ellora.domain.model.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {

    Optional<Employee> findById(Long id);

    List<Employee> findBySalonId(Long salonId);

    List<Employee> findBySalonIdAndActiveTrue(Long salonId);

    Employee save(Employee employee);

    void deleteById(Long id);
}
