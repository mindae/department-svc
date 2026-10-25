package com.mindae.departmentsvc.repository;

import com.mindae.departmentsvc.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
