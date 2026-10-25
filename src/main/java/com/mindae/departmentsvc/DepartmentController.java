package com.mindae.departmentsvc;

import com.mindae.departmentsvc.entity.Department;
import com.mindae.departmentsvc.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class DepartmentController {

    private static final Logger log =
            LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/departments")
    ResponseEntity<DepartmentResponse> getDepartments() {
        log.info("fetching departments details");
        Department department = departmentService.getDepartment(100L);
        return ResponseEntity.ok(
                new DepartmentResponse(department.getId(), department.getName()));
    }

    @GetMapping("/departments/all")
    ResponseEntity<List<DepartmentResponse>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments()
                .stream()
                .map(department -> new DepartmentResponse(
                        department.getId(), department.getName()))
                .toList();
        return ResponseEntity.ok(departments);
    }
}
