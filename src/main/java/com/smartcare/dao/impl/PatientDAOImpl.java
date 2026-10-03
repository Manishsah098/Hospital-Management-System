package com.smartcare.dao.impl;

import com.smartcare.dao.PatientDAO;
import com.smartcare.enums.Gender;
import com.smartcare.exception.DatabaseException;
import com.smartcare.model.Patient;
import com.smartcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PatientDAOImpl implements PatientDAO {
    private static final Logger LOGGER = Logger.getLogger(PatientDAOImpl.class.getName());

    @Override
    public Optional<Patient> findById(int patientId) {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching patient ID: " + patientId, e);
            throw new DatabaseException("Failed to find patient: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByCode(String patientCode) {
        String sql = "SELECT * FROM patients WHERE patient_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, patientCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching patient code: " + patientCode, e);
            throw new DatabaseException("Failed to find patient by code: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByPhone(String phone) {
        String sql = "SELECT * FROM patients WHERE phone = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching patient by phone: " + phone, e);
            throw new DatabaseException("Failed to find patient by phone: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Patient> findAll() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY patient_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToPatient(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all patients", e);
            throw new DatabaseException("Failed to retrieve patients list: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Patient> search(String keyword) {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE patient_code LIKE ? OR full_name LIKE ? OR phone LIKE ? OR email LIKE ? ORDER BY patient_id DESC";
        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPatient(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching patients with keyword: " + keyword, e);
            throw new DatabaseException("Failed to search patients: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean create(Patient p) {
        String sql = "INSERT INTO patients (patient_code, full_name, date_of_birth, gender, blood_group, phone, email, address, emergency_contact_name, emergency_contact_phone, allergies) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getPatientCode());
            stmt.setString(2, p.getFullName());
            stmt.setDate(3, Date.valueOf(p.getDateOfBirth()));
            stmt.setString(4, p.getGender().name());
            stmt.setString(5, p.getBloodGroup());
            stmt.setString(6, p.getPhone());
            stmt.setString(7, p.getEmail());
            stmt.setString(8, p.getAddress());
            stmt.setString(9, p.getEmergencyContactName());
            stmt.setString(10, p.getEmergencyContactPhone());
            stmt.setString(11, p.getAllergies());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        p.setPatientId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting patient record", e);
            throw new DatabaseException("Failed to create patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Patient p) {
        String sql = "UPDATE patients SET full_name = ?, date_of_birth = ?, gender = ?, blood_group = ?, " +
                     "phone = ?, email = ?, address = ?, emergency_contact_name = ?, emergency_contact_phone = ?, allergies = ? " +
                     "WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getFullName());
            stmt.setDate(2, Date.valueOf(p.getDateOfBirth()));
            stmt.setString(3, p.getGender().name());
            stmt.setString(4, p.getBloodGroup());
            stmt.setString(5, p.getPhone());
            stmt.setString(6, p.getEmail());
            stmt.setString(7, p.getAddress());
            stmt.setString(8, p.getEmergencyContactName());
            stmt.setString(9, p.getEmergencyContactPhone());
            stmt.setString(10, p.getAllergies());
            stmt.setInt(11, p.getPatientId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating patient ID: " + p.getPatientId(), e);
            throw new DatabaseException("Failed to update patient: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int patientId) {
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting patient ID: " + patientId, e);
            throw new DatabaseException("Failed to delete patient: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateNextPatientCode() {
        String sql = "SELECT MAX(patient_id) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int nextId = 1;
            if (rs.next()) {
                nextId = rs.getInt(1) + 1;
            }
            return String.format("PAT-%04d", 1000 + nextId);
        } catch (SQLException e) {
            return "PAT-" + System.currentTimeMillis() % 10000;
        }
    }

    @Override
    public int getTotalPatientCount() {
        String sql = "SELECT COUNT(*) FROM patients";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting patients: " + e.getMessage());
        }
        return 0;
    }

    private Patient mapResultSetToPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setPatientCode(rs.getString("patient_code"));
        p.setFullName(rs.getString("full_name"));

        Date dob = rs.getDate("date_of_birth");
        if (dob != null) p.setDateOfBirth(dob.toLocalDate());

        p.setGender(Gender.fromString(rs.getString("gender")));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setPhone(rs.getString("phone"));
        p.setEmail(rs.getString("email"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContactName(rs.getString("emergency_contact_name"));
        p.setEmergencyContactPhone(rs.getString("emergency_contact_phone"));
        p.setAllergies(rs.getString("allergies"));

        Timestamp regAt = rs.getTimestamp("registered_at");
        if (regAt != null) p.setRegisteredAt(regAt.toLocalDateTime());

        Timestamp upAt = rs.getTimestamp("updated_at");
        if (upAt != null) p.setUpdatedAt(upAt.toLocalDateTime());

        return p;
    }
}
