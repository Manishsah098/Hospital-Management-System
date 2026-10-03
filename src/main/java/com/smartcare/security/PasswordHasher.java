package com.smartcare.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for hashing passwords securely with SHA-256 and unique cryptographic salts.
 */
public class PasswordHasher {
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generate a random cryptographic salt.
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a raw password combined with a salt.
     */
    public static String hashPassword(String password, String salt) {
        if (password == null || salt == null) {
            throw new IllegalArgumentException("Password and salt cannot be null");
        }
        try {
            MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
            String input = salt + ":" + password;
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error initializing password hashing algorithm", e);
        }
    }

    /**
     * Verifies raw password against the stored hash and salt.
     */
    public static boolean verifyPassword(String rawPassword, String salt, String storedHash) {
        if (rawPassword == null || salt == null || storedHash == null) {
            return false;
        }
        String computedHash = hashPassword(rawPassword, salt);
        return MessageDigest.isEqual(
            computedHash.getBytes(StandardCharsets.UTF_8),
            storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
