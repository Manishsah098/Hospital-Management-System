package com.smartcare.service;

import com.smartcare.model.Patient;
import java.util.List;

public interface PatientService {
    Patient registerPatient(Patient patient);
    Patient updatePatient(Patient patient);
    boolean deletePatient(int patientId);
    Patient getPatientById(int patientId);
    Patient getPatientByCode(String patientCode);
    List<Patient> getAllPatients();
    List<Patient> searchPatients(String keyword);
    String generateNextCode();
    int getTotalPatientsCount();
}
