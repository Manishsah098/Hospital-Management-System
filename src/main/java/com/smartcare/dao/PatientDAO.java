package com.smartcare.dao;

import com.smartcare.model.Patient;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Patient entity operations.
 */
public interface PatientDAO {
    Optional<Patient> findById(int patientId);
    Optional<Patient> findByCode(String patientCode);
    Optional<Patient> findByPhone(String phone);
    List<Patient> findAll();
    List<Patient> search(String keyword);
    boolean create(Patient patient);
    boolean update(Patient patient);
    boolean delete(int patientId);
    String generateNextPatientCode();
    int getTotalPatientCount();
}
