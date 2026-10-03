package com.smartcare;

import com.smartcare.enums.UserRole;
import com.smartcare.model.User;
import com.smartcare.security.PasswordHasher;
import com.smartcare.security.UserSession;
import com.smartcare.service.impl.AuthServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Basic unit test validation for SmartCare Core Authentication & Security.
 */
public class AuthServiceTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("Running SmartCare Unit Tests: Auth & Security");
        System.out.println("==================================================");

        testPasswordHashing();
        testUserSession();
        testUserRoleEnums();

        System.out.println("==================================================");
        System.out.println("All Core Unit Tests Passed Successfully!");
        System.out.println("==================================================");
    }

    private static void testPasswordHashing() {
        System.out.print("Testing Password Hashing & Verification... ");
        String rawPassword = "Admin@123";
        String salt = PasswordHasher.generateSalt();
        String hash = PasswordHasher.hashPassword(rawPassword, salt);

        assert hash != null && !hash.isEmpty() : "Hash must not be empty";
        assert PasswordHasher.verifyPassword(rawPassword, salt, hash) : "Password verification must succeed";
        assert !PasswordHasher.verifyPassword("WrongPassword", salt, hash) : "Wrong password verification must fail";
        System.out.println("PASSED");
    }

    private static void testUserSession() {
        System.out.print("Testing User Session Context... ");
        User user = new User(1, "admin", "System Administrator", "admin@smartcare.hospital", "9876543210", UserRole.ADMIN);
        
        UserSession session = UserSession.getInstance();
        session.startSession(user);

        assert session.isLoggedIn() : "User should be logged in";
        assert session.isAdmin() : "User should have ADMIN role";
        assert "admin".equals(session.getUsername()) : "Username should match";

        session.clearSession();
        assert !session.isLoggedIn() : "User should be logged out after clearSession";
        System.out.println("PASSED");
    }

    private static void testUserRoleEnums() {
        System.out.print("Testing UserRole Enums & Mapping... ");
        assert UserRole.ADMIN == UserRole.fromString("ADMIN") : "Role parsing failed";
        assert UserRole.DOCTOR == UserRole.fromString("Doctor") : "Role display name parsing failed";
        System.out.println("PASSED");
    }
}
