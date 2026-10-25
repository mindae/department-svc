package com.mindae.departmentsvc.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(@NotBlank @Size(min = 3, max = 255) String name) {

    public DepartmentRequest {
        name = name == null ? null : name.trim();
    }
}
