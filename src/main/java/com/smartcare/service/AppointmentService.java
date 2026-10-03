package com.smartcare.service;

import com.smartcare.enums.AppointmentStatus;
import com.smartcare.model.Appointment;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    Appointment scheduleAppointment(Appointment appointment);
    Appointment updateAppointment(Appointment appointment);
    boolean updateStatus(int appointmentId, AppointmentStatus status);
    boolean cancelAppointment(int appointmentId);
    Appointment getAppointmentById(int appointmentId);
    List<Appointment> getAllAppointments();
    List<Appointment> getTodayAppointments();
    List<Appointment> getAppointmentsByPatient(int patientId);
    List<Appointment> getAppointmentsByDoctor(int doctorId);
    List<Appointment> getAppointmentsByDate(LocalDate date);
    List<Appointment> searchAppointments(String keyword);
    int getTodayCount();
}
