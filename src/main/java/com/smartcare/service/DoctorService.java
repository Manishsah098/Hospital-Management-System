package com.smartcare.service;

import com.smartcare.model.Department;
import com.smartcare.model.Doctor;
import java.util.List;

public interface DoctorService {
    Doctor registerDoctor(Doctor doctor);
    Doctor updateDoctor(Doctor doctor);
    boolean deleteDoctor(int doctorId);
    Doctor getDoctorById(int doctorId);
    List<Doctor> getAllDoctors();
    List<Doctor> searchDoctors(String keyword);
    List<Doctor> getDoctorsByDepartment(int departmentId);
    List<Doctor> getDoctorsBySpecialization(String specialization);
    List<Department> getAllDepartments();
    int getTotalDoctorsCount();
}
