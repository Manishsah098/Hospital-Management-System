package com.smartcare.service;

import com.smartcare.model.MedicalRecord;
import java.util.List;

public interface MedicalRecordService {
    MedicalRecord createRecord(MedicalRecord record);
    MedicalRecord updateRecord(MedicalRecord record);
    boolean deleteRecord(int recordId);
    MedicalRecord getRecordById(int recordId);
    List<MedicalRecord> getRecordsByPatient(int patientId);
    List<MedicalRecord> getAllRecords();
    List<MedicalRecord> searchRecords(String keyword);
}
