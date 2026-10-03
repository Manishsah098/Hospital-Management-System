package com.smartcare.dao;

import com.smartcare.model.Department;
import java.util.List;
import java.util.Optional;

public interface DepartmentDAO {
    Optional<Department> findById(int departmentId);
    Optional<Department> findByName(String name);
    List<Department> findAll();
    boolean create(Department department);
    boolean update(Department department);
    boolean delete(int departmentId);
}
