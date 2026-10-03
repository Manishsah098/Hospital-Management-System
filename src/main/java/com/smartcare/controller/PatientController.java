package com.smartcare.controller;

import com.smartcare.exception.DatabaseException;
import com.smartcare.exception.PatientNotFoundException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.Patient;
import com.smartcare.service.PatientService;
import com.smartcare.service.impl.PatientServiceImpl;

import java.util.List;

public class PatientController {
    private final PatientService patientService;

    public PatientController() {
        this.patientService = new PatientServiceImpl();
    }

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    public Patient registerPatient(Patient patient) throws ValidationException, DatabaseException {
        return patientService.registerPatient(patient);
    }

    public Patient updatePatient(Patient patient) throws ValidationException, PatientNotFoundException, DatabaseException {
        return patientService.updatePatient(patient);
    }

    public boolean deletePatient(int patientId) throws ValidationException, DatabaseException {
        return patientService.deletePatient(patientId);
    }

    public Patient getPatientById(int patientId) throws PatientNotFoundException, DatabaseException {
        return patientService.getPatientById(patientId);
    }

    public Patient getPatientByCode(String patientCode) throws PatientNotFoundException, DatabaseException {
        return patientService.getPatientByCode(patientCode);
    }

    public List<Patient> getAllPatients() throws DatabaseException {
        return patientService.getAllPatients();
    }

    public List<Patient> searchPatients(String keyword) throws DatabaseException {
        return patientService.searchPatients(keyword);
    }

    public String getNextPatientCode() {
        return patientService.generateNextCode();
    }

    public int getTotalPatientsCount() {
        return patientService.getTotalPatientsCount();
    }
}
