package com.smartcare.enums;

/**
 * Enumeration representing user roles within the SmartCare Hospital System.
 * Enforces Role-Based Access Control (RBAC).
 */
public enum UserRole {
    ADMIN("Administrator", "Full system access & administration"),
    DOCTOR("Doctor", "Manage patients, medical records, and prescriptions"),
    RECEPTIONIST("Receptionist", "Patient registration, appointments, and billing"),
    LAB_TECHNICIAN("Lab Technician", "Laboratory test management and results entry"),
    PHARMACIST("Pharmacist", "Pharmacy inventory and prescription dispensing");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromString(String text) {
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(text) || role.displayName.equalsIgnoreCase(text)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + text);
    }
}
