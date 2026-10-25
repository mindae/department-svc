package com.mindae.departmentsvc.service;

import com.mindae.departmentsvc.entity.Department;
import com.mindae.departmentsvc.repository.DepartmentRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartmentService {

    private static final long RESERVED_DEPARTMENT_ID = 100L;

    private final DepartmentRepository departmentRepository;
    private final JdbcTemplate jdbcTemplate;

    public DepartmentService(DepartmentRepository departmentRepository,
                             JdbcTemplate jdbcTemplate) {
        this.departmentRepository = departmentRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Seed department " + id + " is missing"));
    }

    public List<Department> getAllDepartments() {
        return departmentRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public Department createDepartment(String name) {
        Long nextId = jdbcTemplate.queryForObject(
                "SELECT next_id FROM department_id_allocator WHERE id = 1 FOR UPDATE",
                Long.class);
        Long highestId = jdbcTemplate.queryForObject(
                "SELECT MAX(id) FROM department", Long.class);
        long id = Math.max(nextId, highestId == null ? 1L : highestId + 1);
        if (id == RESERVED_DEPARTMENT_ID) {
            id++;
        }
        jdbcTemplate.update(
                "UPDATE department_id_allocator SET next_id = ? WHERE id = 1",
                id + 1);
        return departmentRepository.saveAndFlush(new Department(id, name));
    }

    @Transactional
    public Department updateDepartment(Long id, String name) {
        ensureDepartmentCanBeModified(id);
        Department department = findDepartment(id);
        department.setName(name);
        return departmentRepository.saveAndFlush(department);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        ensureDepartmentCanBeModified(id);
        departmentRepository.delete(findDepartment(id));
    }

    private void ensureDepartmentCanBeModified(Long id) {
        if (id == RESERVED_DEPARTMENT_ID) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Department 100 is reserved");
        }
    }

    private Department findDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Department " + id + " was not found"));
    }
}
