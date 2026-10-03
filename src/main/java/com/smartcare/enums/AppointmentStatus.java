package com.smartcare.enums;

/**
 * Enumeration representing Appointment lifecycle statuses.
 */
public enum AppointmentStatus {
    SCHEDULED("Scheduled"),
    CONFIRMED("Confirmed"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show");

    private final String displayName;

    AppointmentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AppointmentStatus fromString(String text) {
        if (text == null) return SCHEDULED;
        for (AppointmentStatus s : AppointmentStatus.values()) {
            if (s.name().equalsIgnoreCase(text) || s.displayName.equalsIgnoreCase(text)) {
                return s;
            }
        }
        return SCHEDULED;
    }
}
