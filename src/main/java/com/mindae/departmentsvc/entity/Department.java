package com.mindae.departmentsvc.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Department {

    @Id
    private Long id;
    private String name;

    protected Department() {
    }

    public Department(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
