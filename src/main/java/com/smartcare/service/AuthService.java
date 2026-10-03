package com.smartcare.service;

import com.smartcare.enums.UserRole;
import com.smartcare.model.User;
import java.util.List;

/**
 * Service interface for handling User Authentication and User Account management.
 */
public interface AuthService {
    User authenticate(String username, String password);
    User registerUser(String username, String rawPassword, String fullName, String email, String phone, UserRole role);
    boolean changePassword(int userId, String oldPassword, String newPassword);
    List<User> getAllUsers();
    boolean deactivateUser(int userId);
    void logout();
}
