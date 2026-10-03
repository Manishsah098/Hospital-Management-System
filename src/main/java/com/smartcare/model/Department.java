package com.smartcare.model;

import java.time.LocalDateTime;

/**
 * Model representing a Medical Department in SmartCare Hospital.
 */
public class Department {
    private int departmentId;
    private String name;
    private String description;
    private String headDoctorName;
    private LocalDateTime createdAt;

    public Department() {}

    public Department(int departmentId, String name, String description, String headDoctorName) {
        this.departmentId = departmentId;
        this.name = name;
        this.description = description;
        this.headDoctorName = headDoctorName;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHeadDoctorName() {
        return headDoctorName;
    }

    public void setHeadDoctorName(String headDoctorName) {
        this.headDoctorName = headDoctorName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}
