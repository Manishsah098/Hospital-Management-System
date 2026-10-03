package com.smartcare.security;

import com.smartcare.enums.UserRole;
import com.smartcare.model.User;
import java.time.LocalDateTime;

/**
 * Singleton managing the active authenticated session in the SmartCare application.
 */
public class UserSession {
    private static UserSession instance;

    private User currentUser;
    private LocalDateTime loginTime;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void startSession(User user) {
        this.currentUser = user;
        this.loginTime = LocalDateTime.now();
    }

    public void clearSession() {
        this.currentUser = null;
        this.loginTime = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public UserRole getUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "";
    }

    public String getFullName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public boolean hasRole(UserRole role) {
        return currentUser != null && currentUser.getRole() == role;
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean isDoctor() {
        return hasRole(UserRole.DOCTOR);
    }

    public boolean isReceptionist() {
        return hasRole(UserRole.RECEPTIONIST);
    }

    public boolean isLabTechnician() {
        return hasRole(UserRole.LAB_TECHNICIAN);
    }

    public boolean isPharmacist() {
        return hasRole(UserRole.PHARMACIST);
    }
}
