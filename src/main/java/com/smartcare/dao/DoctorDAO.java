package com.smartcare.dao;

import com.smartcare.model.Doctor;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Doctor entity operations.
 */
public interface DoctorDAO {
    Optional<Doctor> findById(int doctorId);
    Optional<Doctor> findByUserId(int userId);
    Optional<Doctor> findByLicenseNumber(String licenseNumber);
    List<Doctor> findAll();
    List<Doctor> search(String keyword);
    List<Doctor> findByDepartment(int departmentId);
    List<Doctor> findBySpecialization(String specialization);
    boolean create(Doctor doctor);
    boolean update(Doctor doctor);
    boolean delete(int doctorId);
    int getTotalDoctorCount();
}
