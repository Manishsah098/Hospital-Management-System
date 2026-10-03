package com.smartcare.dao;

import com.smartcare.enums.AppointmentStatus;
import com.smartcare.model.Appointment;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Appointment entity operations.
 */
public interface AppointmentDAO {
    Optional<Appointment> findById(int appointmentId);
    List<Appointment> findAll();
    List<Appointment> findByPatientId(int patientId);
    List<Appointment> findByDoctorId(int doctorId);
    List<Appointment> findByDate(LocalDate date);
    List<Appointment> findTodayAppointments();
    List<Appointment> findByStatus(AppointmentStatus status);
    List<Appointment> search(String keyword);
    boolean hasDoctorConflict(int doctorId, LocalDate date, LocalTime time, Integer excludeAppointmentId);
    boolean create(Appointment appointment);
    boolean update(Appointment appointment);
    boolean updateStatus(int appointmentId, AppointmentStatus newStatus);
    boolean cancel(int appointmentId);
    int getTodayAppointmentsCount();
}
