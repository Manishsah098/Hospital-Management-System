package com.smartcare.enums;

/**
 * Enumeration representing Prescription fulfillment statuses.
 */
public enum PrescriptionStatus {
    PENDING("Pending"),
    DISPENSED("Dispensed"),
    CANCELLED("Cancelled");

    private final String displayName;

    PrescriptionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PrescriptionStatus fromString(String text) {
        if (text == null) return PENDING;
        for (PrescriptionStatus s : PrescriptionStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return PENDING;
    }
}
