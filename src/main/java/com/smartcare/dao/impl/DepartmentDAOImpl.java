package com.smartcare.dao.impl;

import com.smartcare.dao.DepartmentDAO;
import com.smartcare.exception.DatabaseException;
import com.smartcare.model.Department;
import com.smartcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DepartmentDAOImpl implements DepartmentDAO {
    private static final Logger LOGGER = Logger.getLogger(DepartmentDAOImpl.class.getName());

    @Override
    public Optional<Department> findById(int departmentId) {
        String sql = "SELECT * FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department by ID: " + departmentId, e);
            throw new DatabaseException("Failed to find department: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Department> findByName(String name) {
        String sql = "SELECT * FROM departments WHERE name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding department by name: " + name, e);
            throw new DatabaseException("Failed to find department: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Department> findAll() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing all departments", e);
            throw new DatabaseException("Failed to retrieve departments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean create(Department department) {
        String sql = "INSERT INTO departments (name, description, head_doctor_name) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, department.getName());
            stmt.setString(2, department.getDescription());
            stmt.setString(3, department.getHeadDoctorName());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) department.setDepartmentId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating department: " + department.getName(), e);
            throw new DatabaseException("Failed to create department: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Department department) {
        String sql = "UPDATE departments SET name = ?, description = ?, head_doctor_name = ? WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, department.getName());
            stmt.setString(2, department.getDescription());
            stmt.setString(3, department.getHeadDoctorName());
            stmt.setInt(4, department.getDepartmentId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating department ID: " + department.getDepartmentId(), e);
            throw new DatabaseException("Failed to update department: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int departmentId) {
        String sql = "DELETE FROM departments WHERE department_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting department ID: " + departmentId, e);
            throw new DatabaseException("Failed to delete department: " + e.getMessage(), e);
        }
    }

    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department dept = new Department();
        dept.setDepartmentId(rs.getInt("department_id"));
        dept.setName(rs.getString("name"));
        dept.setDescription(rs.getString("description"));
        dept.setHeadDoctorName(rs.getString("head_doctor_name"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) dept.setCreatedAt(ts.toLocalDateTime());
        return dept;
    }
}
