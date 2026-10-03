package com.smartcare.controller;

import com.smartcare.enums.AppointmentStatus;
import com.smartcare.model.Appointment;
import com.smartcare.service.AppointmentService;
import com.smartcare.service.impl.AppointmentServiceImpl;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller layer for Appointment operations in SmartCare.
 * Bridges UI panels with the AppointmentService.
 */
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController() {
        this.service = new AppointmentServiceImpl();
    }

    public Appointment scheduleAppointment(Appointment appt) {
        return service.scheduleAppointment(appt);
    }

    public Appointment updateAppointment(Appointment appt) {
        return service.updateAppointment(appt);
    }

    public boolean updateStatus(int appointmentId, AppointmentStatus status) {
        return service.updateStatus(appointmentId, status);
    }

    public boolean cancelAppointment(int appointmentId) {
        return service.cancelAppointment(appointmentId);
    }

    public Appointment getAppointmentById(int id) {
        return service.getAppointmentById(id);
    }

    public List<Appointment> getAllAppointments() {
        return service.getAllAppointments();
    }

    public List<Appointment> getTodayAppointments() {
        return service.getTodayAppointments();
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return service.getAppointmentsByPatient(patientId);
    }

    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return service.getAppointmentsByDoctor(doctorId);
    }

    public List<Appointment> searchAppointments(String keyword) {
        return service.searchAppointments(keyword);
    }

    public int getTodayCount() {
        return service.getTodayCount();
    }
}
