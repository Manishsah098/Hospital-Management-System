package com.smartcare.dao.impl;

import com.smartcare.dao.MedicalRecordDAO;
import com.smartcare.exception.DatabaseException;
import com.smartcare.model.MedicalRecord;
import com.smartcare.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MedicalRecordDAOImpl implements MedicalRecordDAO {
    private static final Logger LOGGER = Logger.getLogger(MedicalRecordDAOImpl.class.getName());

    private static final String BASE_QUERY =
            "SELECT m.*, p.full_name AS patient_name, p.patient_code AS patient_code, d.full_name AS doctor_name " +
            "FROM medical_records m " +
            "JOIN patients p ON m.patient_id = p.patient_id " +
            "JOIN doctors d ON m.doctor_id = d.doctor_id ";

    @Override
    public Optional<MedicalRecord> findById(int recordId) {
        String sql = BASE_QUERY + "WHERE m.record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding medical record ID: " + recordId, e);
            throw new DatabaseException("Failed to find medical record: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<MedicalRecord> findAll() {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = BASE_QUERY + "ORDER BY m.recorded_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToMedicalRecord(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all medical records", e);
            throw new DatabaseException("Failed to fetch medical records: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<MedicalRecord> findByPatientId(int patientId) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE m.patient_id = ? ORDER BY m.recorded_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching records for patient: " + patientId, e);
            throw new DatabaseException("Failed to fetch patient history: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<MedicalRecord> findByDoctorId(int doctorId) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE m.doctor_id = ? ORDER BY m.recorded_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching records for doctor: " + doctorId, e);
            throw new DatabaseException("Failed to fetch doctor records: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<MedicalRecord> search(String keyword) {
        List<MedicalRecord> list = new ArrayList<>();
        String sql = BASE_QUERY +
                "WHERE p.full_name LIKE ? OR p.patient_code LIKE ? OR d.full_name LIKE ? OR m.diagnosis LIKE ? OR m.symptoms LIKE ? " +
                "ORDER BY m.recorded_at DESC";
        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            stmt.setString(5, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToMedicalRecord(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching medical records with: " + keyword, e);
            throw new DatabaseException("Failed to search medical records: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean create(MedicalRecord m) {
        String sql = "INSERT INTO medical_records (patient_id, doctor_id, appointment_id, diagnosis, symptoms, treatment_plan, vital_signs, doctor_notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, m.getPatientId());
            stmt.setInt(2, m.getDoctorId());
            if (m.getAppointmentId() != null) stmt.setInt(3, m.getAppointmentId()); else stmt.setNull(3, Types.INTEGER);
            stmt.setString(4, m.getDiagnosis());
            stmt.setString(5, m.getSymptoms());
            stmt.setString(6, m.getTreatmentPlan());
            stmt.setString(7, m.getVitalSigns());
            stmt.setString(8, m.getDoctorNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) m.setRecordId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting medical record", e);
            throw new DatabaseException("Failed to save medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(MedicalRecord m) {
        String sql = "UPDATE medical_records SET diagnosis = ?, symptoms = ?, treatment_plan = ?, vital_signs = ?, doctor_notes = ? " +
                     "WHERE record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getDiagnosis());
            stmt.setString(2, m.getSymptoms());
            stmt.setString(3, m.getTreatmentPlan());
            stmt.setString(4, m.getVitalSigns());
            stmt.setString(5, m.getDoctorNotes());
            stmt.setInt(6, m.getRecordId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating medical record ID: " + m.getRecordId(), e);
            throw new DatabaseException("Failed to update medical record: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int recordId) {
        String sql = "DELETE FROM medical_records WHERE record_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, recordId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting medical record ID: " + recordId, e);
            throw new DatabaseException("Failed to delete medical record: " + e.getMessage(), e);
        }
    }

    private MedicalRecord mapResultSetToMedicalRecord(ResultSet rs) throws SQLException {
        MedicalRecord m = new MedicalRecord();
        m.setRecordId(rs.getInt("record_id"));
        m.setPatientId(rs.getInt("patient_id"));
        m.setPatientName(rs.getString("patient_name"));
        m.setPatientCode(rs.getString("patient_code"));
        m.setDoctorId(rs.getInt("doctor_id"));
        m.setDoctorName(rs.getString("doctor_name"));

        int apptId = rs.getInt("appointment_id");
        if (!rs.wasNull()) m.setAppointmentId(apptId);

        m.setDiagnosis(rs.getString("diagnosis"));
        m.setSymptoms(rs.getString("symptoms"));
        m.setTreatmentPlan(rs.getString("treatment_plan"));
        m.setVitalSigns(rs.getString("vital_signs"));
        m.setDoctorNotes(rs.getString("doctor_notes"));

        Timestamp ts = rs.getTimestamp("recorded_at");
        if (ts != null) m.setRecordedAt(ts.toLocalDateTime());

        return m;
    }
}
