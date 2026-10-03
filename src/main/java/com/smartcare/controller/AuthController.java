package com.smartcare.controller;

import com.smartcare.enums.UserRole;
import com.smartcare.exception.AuthenticationException;
import com.smartcare.exception.DatabaseException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.User;
import com.smartcare.service.AuthService;
import com.smartcare.service.impl.AuthServiceImpl;

import java.util.List;

/**
 * Controller mediating authentication interactions between Swing UI and AuthService.
 */
public class AuthController {
    private final AuthService authService;

    public AuthController() {
        this.authService = new AuthServiceImpl();
    }

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public User login(String username, String password) throws AuthenticationException, ValidationException, DatabaseException {
        return authService.authenticate(username, password);
    }

    public User register(String username, String rawPassword, String fullName, String email, String phone, UserRole role)
            throws ValidationException, DatabaseException {
        return authService.registerUser(username, rawPassword, fullName, email, phone, role);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword)
            throws AuthenticationException, ValidationException, DatabaseException {
        return authService.changePassword(userId, oldPassword, newPassword);
    }

    public List<User> getAllUsers() throws DatabaseException {
        return authService.getAllUsers();
    }

    public boolean deactivateUser(int userId) throws DatabaseException {
        return authService.deactivateUser(userId);
    }

    public void logout() {
        authService.logout();
    }
}
