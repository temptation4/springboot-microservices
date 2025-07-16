package com.restTemplate.example.service;

import com.restTemplate.example.entity.Department;

import java.util.List;

public interface DepartmentService {

    Department saveDepartment(Department department);

    Department getDepartmentById(Long departmentId);

    List<Department> getDepartment();
}
