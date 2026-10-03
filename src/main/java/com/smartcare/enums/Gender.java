package com.smartcare.enums;

/**
 * Gender enumeration for Patient demographics.
 */
public enum Gender {
    MALE("Male"),
    FEMALE("Female"),
    OTHER("Other");

    private final String displayName;

    Gender(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Gender fromString(String text) {
        if (text == null) return MALE;
        for (Gender g : Gender.values()) {
            if (g.name().equalsIgnoreCase(text) || g.displayName.equalsIgnoreCase(text)) {
                return g;
            }
        }
        return MALE;
    }
}
