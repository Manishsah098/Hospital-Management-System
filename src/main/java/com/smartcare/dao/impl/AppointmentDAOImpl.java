package com.smartcare.dao.impl;

import com.smartcare.dao.AppointmentDAO;
import com.smartcare.enums.AppointmentStatus;
import com.smartcare.exception.DatabaseException;
import com.smartcare.model.Appointment;
import com.smartcare.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppointmentDAOImpl implements AppointmentDAO {
    private static final Logger LOGGER = Logger.getLogger(AppointmentDAOImpl.class.getName());

    private static final String BASE_QUERY =
            "SELECT a.*, p.full_name AS patient_name, p.patient_code AS patient_code, " +
            "d.full_name AS doctor_name, d.specialization AS doctor_specialization " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors d ON a.doctor_id = d.doctor_id ";

    @Override
    public Optional<Appointment> findById(int appointmentId) {
        String sql = BASE_QUERY + "WHERE a.appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointment ID: " + appointmentId, e);
            throw new DatabaseException("Failed to find appointment: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_QUERY + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToAppointment(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all appointments", e);
            throw new DatabaseException("Failed to retrieve appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointments for patient: " + patientId, e);
            throw new DatabaseException("Failed to get patient appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByDoctorId(int doctorId) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE a.doctor_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointments for doctor: " + doctorId, e);
            throw new DatabaseException("Failed to get doctor appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findByDate(LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE a.appointment_date = ? ORDER BY a.appointment_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointments for date: " + date, e);
            throw new DatabaseException("Failed to get appointments by date: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findTodayAppointments() {
        return findByDate(LocalDate.now());
    }

    @Override
    public List<Appointment> findByStatus(AppointmentStatus status) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE a.status = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding appointments by status: " + status, e);
            throw new DatabaseException("Failed to get appointments by status: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> search(String keyword) {
        List<Appointment> list = new ArrayList<>();
        String sql = BASE_QUERY +
                "WHERE p.full_name LIKE ? OR p.patient_code LIKE ? OR d.full_name LIKE ? OR a.reason_for_visit LIKE ? " +
                "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching appointments with keyword: " + keyword, e);
            throw new DatabaseException("Failed to search appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean hasDoctorConflict(int doctorId, LocalDate date, LocalTime time, Integer excludeAppointmentId) {
        // Checks if doctor already has an active appointment within +/- 15 minutes
        String sql = "SELECT appointment_id FROM appointments " +
                "WHERE doctor_id = ? AND appointment_date = ? AND status IN ('SCHEDULED', 'CONFIRMED') " +
                "AND ABS(TIMESTAMPDIFF(MINUTE, appointment_time, ?)) < 15 ";
        if (excludeAppointmentId != null) {
            sql += "AND appointment_id != " + excludeAppointmentId;
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doctorId);
            stmt.setDate(2, Date.valueOf(date));
            stmt.setTime(3, Time.valueOf(time));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next(); // true if conflict found
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking doctor schedule conflict", e);
            throw new DatabaseException("Failed to verify doctor availability: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean create(Appointment a) {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, reason_for_visit, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, a.getPatientId());
            stmt.setInt(2, a.getDoctorId());
            stmt.setDate(3, Date.valueOf(a.getAppointmentDate()));
            stmt.setTime(4, Time.valueOf(a.getAppointmentTime()));
            stmt.setString(5, a.getStatus().name());
            stmt.setString(6, a.getReasonForVisit());
            stmt.setString(7, a.getNotes());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) a.setAppointmentId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error booking appointment", e);
            throw new DatabaseException("Failed to book appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Appointment a) {
        String sql = "UPDATE appointments SET patient_id = ?, doctor_id = ?, appointment_date = ?, appointment_time = ?, " +
                     "status = ?, reason_for_visit = ?, notes = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getPatientId());
            stmt.setInt(2, a.getDoctorId());
            stmt.setDate(3, Date.valueOf(a.getAppointmentDate()));
            stmt.setTime(4, Time.valueOf(a.getAppointmentTime()));
            stmt.setString(5, a.getStatus().name());
            stmt.setString(6, a.getReasonForVisit());
            stmt.setString(7, a.getNotes());
            stmt.setInt(8, a.getAppointmentId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating appointment ID: " + a.getAppointmentId(), e);
            throw new DatabaseException("Failed to update appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int appointmentId, AppointmentStatus newStatus) {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setInt(2, appointmentId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating appointment status", e);
            throw new DatabaseException("Failed to update appointment status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean cancel(int appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }

    @Override
    public int getTodayAppointmentsCount() {
        String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_date = CURRENT_DATE AND status != 'CANCELLED'";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting today's appointments: " + e.getMessage());
        }
        return 0;
    }

    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setAppointmentId(rs.getInt("appointment_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setPatientCode(rs.getString("patient_code"));
        a.setDoctorId(rs.getInt("doctor_id"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setDoctorSpecialization(rs.getString("doctor_specialization"));

        Date date = rs.getDate("appointment_date");
        if (date != null) a.setAppointmentDate(date.toLocalDate());

        Time time = rs.getTime("appointment_time");
        if (time != null) a.setAppointmentTime(time.toLocalTime());

        a.setStatus(AppointmentStatus.fromString(rs.getString("status")));
        a.setReasonForVisit(rs.getString("reason_for_visit"));
        a.setNotes(rs.getString("notes"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) a.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) a.setUpdatedAt(updatedAt.toLocalDateTime());

        return a;
    }
}
