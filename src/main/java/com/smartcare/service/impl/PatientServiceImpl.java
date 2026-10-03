package com.smartcare.service.impl;

import com.smartcare.dao.PatientDAO;
import com.smartcare.dao.impl.PatientDAOImpl;
import com.smartcare.exception.PatientNotFoundException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.Patient;
import com.smartcare.service.PatientService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class PatientServiceImpl implements PatientService {
    private final PatientDAO patientDAO;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public PatientServiceImpl() {
        this.patientDAO = new PatientDAOImpl();
    }

    public PatientServiceImpl(PatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    @Override
    public Patient registerPatient(Patient patient) {
        validatePatient(patient);

        if (patient.getPatientCode() == null || patient.getPatientCode().trim().isEmpty()) {
            patient.setPatientCode(patientDAO.generateNextPatientCode());
        } else {
            Optional<Patient> existing = patientDAO.findByCode(patient.getPatientCode().trim());
            if (existing.isPresent()) {
                throw new ValidationException("Patient code already in use: " + patient.getPatientCode());
            }
        }

        boolean created = patientDAO.create(patient);
        if (!created) {
            throw new ValidationException("Could not register patient. Please check your data.");
        }
        return patient;
    }

    @Override
    public Patient updatePatient(Patient patient) {
        if (patient.getPatientId() <= 0) {
            throw new ValidationException("Invalid patient ID");
        }
        validatePatient(patient);

        boolean updated = patientDAO.update(patient);
        if (!updated) {
            throw new PatientNotFoundException("Patient record could not be updated or does not exist.");
        }
        return patient;
    }

    @Override
    public boolean deletePatient(int patientId) {
        if (patientId <= 0) {
            throw new ValidationException("Invalid patient ID");
        }
        return patientDAO.delete(patientId);
    }

    @Override
    public Patient getPatientById(int patientId) {
        return patientDAO.findById(patientId)
                .orElseThrow(() -> new PatientNotFoundException("Patient with ID " + patientId + " not found."));
    }

    @Override
    public Patient getPatientByCode(String patientCode) {
        return patientDAO.findByCode(patientCode)
                .orElseThrow(() -> new PatientNotFoundException("Patient with code " + patientCode + " not found."));
    }

    @Override
    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    @Override
    public List<Patient> searchPatients(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPatients();
        }
        return patientDAO.search(keyword.trim());
    }

    @Override
    public String generateNextCode() {
        return patientDAO.generateNextPatientCode();
    }

    @Override
    public int getTotalPatientsCount() {
        return patientDAO.getTotalPatientCount();
    }

    private void validatePatient(Patient p) {
        if (p == null) {
            throw new ValidationException("Patient data cannot be null");
        }
        if (p.getFullName() == null || p.getFullName().trim().length() < 2) {
            throw new ValidationException("Full Name must be at least 2 characters long");
        }
        if (p.getDateOfBirth() == null || p.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("Date of birth cannot be in the future");
        }
        if (p.getPhone() == null || !PHONE_PATTERN.matcher(p.getPhone().trim()).matches()) {
            throw new ValidationException("Phone number must contain 10-14 digits");
        }
        if (p.getEmail() != null && !p.getEmail().trim().isEmpty() && !EMAIL_PATTERN.matcher(p.getEmail().trim()).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }
}
