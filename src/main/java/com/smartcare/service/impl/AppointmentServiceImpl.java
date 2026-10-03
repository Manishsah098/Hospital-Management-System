package com.smartcare.service.impl;

import com.smartcare.dao.AppointmentDAO;
import com.smartcare.dao.DoctorDAO;
import com.smartcare.dao.PatientDAO;
import com.smartcare.dao.impl.AppointmentDAOImpl;
import com.smartcare.dao.impl.DoctorDAOImpl;
import com.smartcare.dao.impl.PatientDAOImpl;
import com.smartcare.enums.AppointmentStatus;
import com.smartcare.exception.AppointmentException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.Appointment;
import com.smartcare.service.AppointmentService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentDAO appointmentDAO;
    private final PatientDAO patientDAO;
    private final DoctorDAO doctorDAO;

    public AppointmentServiceImpl() {
        this.appointmentDAO = new AppointmentDAOImpl();
        this.patientDAO = new PatientDAOImpl();
        this.doctorDAO = new DoctorDAOImpl();
    }

    public AppointmentServiceImpl(AppointmentDAO appointmentDAO, PatientDAO patientDAO, DoctorDAO doctorDAO) {
        this.appointmentDAO = appointmentDAO;
        this.patientDAO = patientDAO;
        this.doctorDAO = doctorDAO;
    }

    @Override
    public Appointment scheduleAppointment(Appointment appt) {
        validateAppointmentData(appt);

        // Verify patient and doctor exist
        patientDAO.findById(appt.getPatientId())
                .orElseThrow(() -> new ValidationException("Patient record does not exist"));
        doctorDAO.findById(appt.getDoctorId())
                .orElseThrow(() -> new ValidationException("Doctor record does not exist"));

        // Check for doctor conflict
        boolean hasConflict = appointmentDAO.hasDoctorConflict(
                appt.getDoctorId(), appt.getAppointmentDate(), appt.getAppointmentTime(), null
        );
        if (hasConflict) {
            throw new AppointmentException("The selected doctor already has an appointment booked around " +
                    appt.getAppointmentTime() + " on " + appt.getAppointmentDate() + ". Please select another time slot.");
        }

        appt.setStatus(AppointmentStatus.SCHEDULED);
        boolean created = appointmentDAO.create(appt);
        if (!created) {
            throw new AppointmentException("Failed to schedule appointment. Please try again.");
        }
        return appt;
    }

    @Override
    public Appointment updateAppointment(Appointment appt) {
        if (appt.getAppointmentId() <= 0) {
            throw new ValidationException("Invalid appointment ID");
        }
        validateAppointmentData(appt);

        boolean hasConflict = appointmentDAO.hasDoctorConflict(
                appt.getDoctorId(), appt.getAppointmentDate(), appt.getAppointmentTime(), appt.getAppointmentId()
        );
        if (hasConflict) {
            throw new AppointmentException("Rescheduling conflict: Doctor has another appointment at this time.");
        }

        boolean updated = appointmentDAO.update(appt);
        if (!updated) {
            throw new AppointmentException("Failed to update appointment record.");
        }
        return appt;
    }

    @Override
    public boolean updateStatus(int appointmentId, AppointmentStatus status) {
        if (appointmentId <= 0 || status == null) {
            throw new ValidationException("Invalid appointment ID or status");
        }
        return appointmentDAO.updateStatus(appointmentId, status);
    }

    @Override
    public boolean cancelAppointment(int appointmentId) {
        return appointmentDAO.cancel(appointmentId);
    }

    @Override
    public Appointment getAppointmentById(int appointmentId) {
        return appointmentDAO.findById(appointmentId)
                .orElseThrow(() -> new AppointmentException("Appointment #" + appointmentId + " not found."));
    }

    @Override
    public List<Appointment> getAllAppointments() {
        return appointmentDAO.findAll();
    }

    @Override
    public List<Appointment> getTodayAppointments() {
        return appointmentDAO.findTodayAppointments();
    }

    @Override
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        return appointmentDAO.findByPatientId(patientId);
    }

    @Override
    public List<Appointment> getAppointmentsByDoctor(int doctorId) {
        return appointmentDAO.findByDoctorId(doctorId);
    }

    @Override
    public List<Appointment> getAppointmentsByDate(LocalDate date) {
        return appointmentDAO.findByDate(date);
    }

    @Override
    public List<Appointment> searchAppointments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllAppointments();
        }
        return appointmentDAO.search(keyword.trim());
    }

    @Override
    public int getTodayCount() {
        return appointmentDAO.getTodayAppointmentsCount();
    }

    private void validateAppointmentData(Appointment a) {
        if (a == null) {
            throw new ValidationException("Appointment payload cannot be null");
        }
        if (a.getPatientId() <= 0) {
            throw new ValidationException("Please select a valid patient");
        }
        if (a.getDoctorId() <= 0) {
            throw new ValidationException("Please select a valid doctor");
        }
        if (a.getAppointmentDate() == null) {
            throw new ValidationException("Appointment date is required");
        }
        if (a.getAppointmentTime() == null) {
            throw new ValidationException("Appointment time is required");
        }

        // Past datetime check
        LocalDateTime apptDateTime = LocalDateTime.of(a.getAppointmentDate(), a.getAppointmentTime());
        if (apptDateTime.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new ValidationException("Cannot schedule an appointment in the past.");
        }
    }
}
