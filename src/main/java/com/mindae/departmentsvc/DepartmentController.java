package com.mindae.departmentsvc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DepartmentController {

    private static final Logger log =
            LoggerFactory.getLogger(DepartmentController.class);

    @GetMapping("/departments")
    ResponseEntity<DepartmentResponse> getDepartments() {
        log.info("fetching departments details");
        return ResponseEntity
                .ok()
                .body(new DepartmentResponse(100L, "Finance"));
    }
}
