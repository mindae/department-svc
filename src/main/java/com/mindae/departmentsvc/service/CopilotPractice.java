package com.mindae.departmentsvc.service;

public class CopilotPractice {
    public boolean isValidDepartmentName(String name) {
        return name != null && name.trim().length() >= 3;
    }
}
