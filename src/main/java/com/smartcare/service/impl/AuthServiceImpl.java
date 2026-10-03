package com.smartcare.service.impl;

import com.smartcare.dao.UserDAO;
import com.smartcare.dao.impl.UserDAOImpl;
import com.smartcare.enums.UserRole;
import com.smartcare.exception.AuthenticationException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.User;
import com.smartcare.security.PasswordHasher;
import com.smartcare.security.UserSession;
import com.smartcare.service.AuthService;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of AuthService applying business logic and validations.
 */
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public AuthServiceImpl() {
        this.userDAO = new UserDAOImpl();
    }

    public AuthServiceImpl(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }

        Optional<User> userOpt = userDAO.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Invalid username or password");
        }

        User user = userOpt.get();
        if (!user.isActive()) {
            throw new AuthenticationException("Your account is deactivated. Please contact Administrator.");
        }

        boolean passwordValid = PasswordHasher.verifyPassword(password, user.getSalt(), user.getPasswordHash());
        if (!passwordValid) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Store active session
        UserSession.getInstance().startSession(user);
        return user;
    }

    @Override
    public User registerUser(String username, String rawPassword, String fullName, String email, String phone, UserRole role) {
        validateRegistrationFields(username, rawPassword, fullName, email, role);

        if (userDAO.existsByUsername(username.trim())) {
            throw new ValidationException("Username already exists: " + username);
        }

        if (userDAO.existsByEmail(email.trim())) {
            throw new ValidationException("Email is already registered: " + email);
        }

        String salt = PasswordHasher.generateSalt();
        String hashedPassword = PasswordHasher.hashPassword(rawPassword, salt);

        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPasswordHash(hashedPassword);
        newUser.setSalt(salt);
        newUser.setFullName(fullName.trim());
        newUser.setEmail(email.trim());
        newUser.setPhone(phone != null ? phone.trim() : "");
        newUser.setRole(role);
        newUser.setActive(true);

        boolean created = userDAO.create(newUser);
        if (!created) {
            throw new ValidationException("Could not create user account. Please try again.");
        }

        return newUser;
    }

    @Override
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("New password must be at least 6 characters long");
        }

        Optional<User> userOpt = userDAO.findById(userId);
        if (userOpt.isEmpty()) {
            throw new ValidationException("User not found");
        }

        User user = userOpt.get();
        if (!PasswordHasher.verifyPassword(oldPassword, user.getSalt(), user.getPasswordHash())) {
            throw new AuthenticationException("Current password is incorrect");
        }

        String newSalt = PasswordHasher.generateSalt();
        String newHash = PasswordHasher.hashPassword(newPassword, newSalt);

        return userDAO.updatePassword(userId, newHash, newSalt);
    }

    @Override
    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    @Override
    public boolean deactivateUser(int userId) {
        return userDAO.delete(userId);
    }

    @Override
    public void logout() {
        UserSession.getInstance().clearSession();
    }

    private void validateRegistrationFields(String username, String rawPassword, String fullName, String email, UserRole role) {
        if (username == null || username.trim().length() < 3) {
            throw new ValidationException("Username must be at least 3 characters long");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Full name is required");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Invalid email format");
        }
        if (role == null) {
            throw new ValidationException("User role is required");
        }
    }
}
