package com.smartcare.dao.impl;

import com.smartcare.dao.DoctorDAO;
import com.smartcare.exception.DatabaseException;
import com.smartcare.model.Doctor;
import com.smartcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoctorDAOImpl implements DoctorDAO {
    private static final Logger LOGGER = Logger.getLogger(DoctorDAOImpl.class.getName());

    private static final String BASE_QUERY =
            "SELECT d.*, dept.name AS department_name " +
            "FROM doctors d " +
            "LEFT JOIN departments dept ON d.department_id = dept.department_id ";

    @Override
    public Optional<Doctor> findById(int doctorId) {
        String sql = BASE_QUERY + "WHERE d.doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding doctor by ID: " + doctorId, e);
            throw new DatabaseException("Failed to find doctor: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Doctor> findByUserId(int userId) {
        String sql = BASE_QUERY + "WHERE d.user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding doctor by user ID: " + userId, e);
            throw new DatabaseException("Failed to find doctor by user: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Doctor> findByLicenseNumber(String licenseNumber) {
        String sql = BASE_QUERY + "WHERE d.license_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, licenseNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding doctor by license: " + licenseNumber, e);
            throw new DatabaseException("Failed to find doctor by license: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Doctor> findAll() {
        List<Doctor> list = new ArrayList<>();
        String sql = BASE_QUERY + "ORDER BY d.doctor_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToDoctor(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all doctors", e);
            throw new DatabaseException("Failed to retrieve doctors list: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Doctor> search(String keyword) {
        List<Doctor> list = new ArrayList<>();
        String sql = BASE_QUERY +
                "WHERE d.full_name LIKE ? OR d.specialization LIKE ? OR d.license_number LIKE ? OR dept.name LIKE ? " +
                "ORDER BY d.doctor_id DESC";
        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching doctors with keyword: " + keyword, e);
            throw new DatabaseException("Failed to search doctors: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Doctor> findByDepartment(int departmentId) {
        List<Doctor> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE d.department_id = ? ORDER BY d.full_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching doctors by department: " + departmentId, e);
            throw new DatabaseException("Failed to fetch doctors by department: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Doctor> findBySpecialization(String specialization) {
        List<Doctor> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE d.specialization = ? ORDER BY d.full_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, specialization);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching doctors by specialization: " + specialization, e);
            throw new DatabaseException("Failed to fetch doctors: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean create(Doctor d) {
        String sql = "INSERT INTO doctors (user_id, full_name, specialization, department_id, license_number, " +
                     "phone, email, consultation_fee, qualification, experience_years, available_days, is_available) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (d.getUserId() != null) stmt.setInt(1, d.getUserId()); else stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, d.getFullName());
            stmt.setString(3, d.getSpecialization());
            if (d.getDepartmentId() != null) stmt.setInt(4, d.getDepartmentId()); else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, d.getLicenseNumber());
            stmt.setString(6, d.getPhone());
            stmt.setString(7, d.getEmail());
            stmt.setBigDecimal(8, d.getConsultationFee());
            stmt.setString(9, d.getQualification());
            stmt.setInt(10, d.getExperienceYears());
            stmt.setString(11, d.getAvailableDays());
            stmt.setBoolean(12, d.isAvailable());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        d.setDoctorId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting doctor record", e);
            throw new DatabaseException("Failed to create doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Doctor d) {
        String sql = "UPDATE doctors SET full_name = ?, specialization = ?, department_id = ?, license_number = ?, " +
                     "phone = ?, email = ?, consultation_fee = ?, qualification = ?, experience_years = ?, " +
                     "available_days = ?, is_available = ? WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, d.getFullName());
            stmt.setString(2, d.getSpecialization());
            if (d.getDepartmentId() != null) stmt.setInt(3, d.getDepartmentId()); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, d.getLicenseNumber());
            stmt.setString(5, d.getPhone());
            stmt.setString(6, d.getEmail());
            stmt.setBigDecimal(7, d.getConsultationFee());
            stmt.setString(8, d.getQualification());
            stmt.setInt(9, d.getExperienceYears());
            stmt.setString(10, d.getAvailableDays());
            stmt.setBoolean(11, d.isAvailable());
            stmt.setInt(12, d.getDoctorId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating doctor ID: " + d.getDoctorId(), e);
            throw new DatabaseException("Failed to update doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int doctorId) {
        String sql = "UPDATE doctors SET is_available = FALSE WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deactivating doctor ID: " + doctorId, e);
            throw new DatabaseException("Failed to deactivate doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public int getTotalDoctorCount() {
        String sql = "SELECT COUNT(*) FROM doctors WHERE is_available = TRUE";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting doctors: " + e.getMessage());
        }
        return 0;
    }

    private Doctor mapResultSetToDoctor(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        int userId = rs.getInt("user_id");
        if (!rs.wasNull()) d.setUserId(userId);

        d.setFullName(rs.getString("full_name"));
        d.setSpecialization(rs.getString("specialization"));

        int deptId = rs.getInt("department_id");
        if (!rs.wasNull()) d.setDepartmentId(deptId);

        d.setDepartmentName(rs.getString("department_name"));
        d.setLicenseNumber(rs.getString("license_number"));
        d.setPhone(rs.getString("phone"));
        d.setEmail(rs.getString("email"));
        d.setConsultationFee(rs.getBigDecimal("consultation_fee"));
        d.setQualification(rs.getString("qualification"));
        d.setExperienceYears(rs.getInt("experience_years"));
        d.setAvailableDays(rs.getString("available_days"));
        d.setAvailable(rs.getBoolean("is_available"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) d.setCreatedAt(createdAt.toLocalDateTime());

        return d;
    }
}
