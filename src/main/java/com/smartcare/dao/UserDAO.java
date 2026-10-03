package com.smartcare.dao;

import com.smartcare.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for User entity operations.
 */
public interface UserDAO {
    Optional<User> findById(int userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    boolean create(User user);
    boolean update(User user);
    boolean delete(int userId);
    boolean updatePassword(int userId, String newPasswordHash, String newSalt);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
