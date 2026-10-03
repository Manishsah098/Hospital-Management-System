package com.smartcare.dao;

import com.smartcare.model.MedicalRecord;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Medical Records operations.
 */
public interface MedicalRecordDAO {
    Optional<MedicalRecord> findById(int recordId);
    List<MedicalRecord> findAll();
    List<MedicalRecord> findByPatientId(int patientId);
    List<MedicalRecord> findByDoctorId(int doctorId);
    List<MedicalRecord> search(String keyword);
    boolean create(MedicalRecord record);
    boolean update(MedicalRecord record);
    boolean delete(int recordId);
}
