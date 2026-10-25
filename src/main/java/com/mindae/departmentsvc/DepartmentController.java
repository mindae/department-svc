package com.mindae.departmentsvc;

import com.mindae.departmentsvc.entity.Department;
import com.mindae.departmentsvc.request.DepartmentRequest;
import com.mindae.departmentsvc.service.DepartmentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
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

    @PostMapping("/departments")
    ResponseEntity<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        Department department = departmentService.createDepartment(request.name());
        return ResponseEntity.created(
                        URI.create("/api/v1/departments/" + department.getId()))
                .body(toResponse(department));
    }

    @PutMapping("/departments/{id}")
    ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(toResponse(
                departmentService.updateDepartment(id, request.name())));
    }

    @DeleteMapping("/departments/{id}")
    ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
