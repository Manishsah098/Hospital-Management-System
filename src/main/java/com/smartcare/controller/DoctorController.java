package com.smartcare.controller;

import com.smartcare.exception.DatabaseException;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.Department;
import com.smartcare.model.Doctor;
import com.smartcare.service.DoctorService;
import com.smartcare.service.impl.DoctorServiceImpl;

import java.util.List;

public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController() {
        this.doctorService = new DoctorServiceImpl();
    }

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    public Doctor registerDoctor(Doctor doctor) throws ValidationException, DatabaseException {
        return doctorService.registerDoctor(doctor);
    }

    public Doctor updateDoctor(Doctor doctor) throws ValidationException, DoctorNotFoundException, DatabaseException {
        return doctorService.updateDoctor(doctor);
    }

    public boolean deleteDoctor(int doctorId) throws ValidationException, DatabaseException {
        return doctorService.deleteDoctor(doctorId);
    }

    public Doctor getDoctorById(int doctorId) throws DoctorNotFoundException, DatabaseException {
        return doctorService.getDoctorById(doctorId);
    }

    public List<Doctor> getAllDoctors() throws DatabaseException {
        return doctorService.getAllDoctors();
    }

    public List<Doctor> searchDoctors(String keyword) throws DatabaseException {
        return doctorService.searchDoctors(keyword);
    }

    public List<Doctor> getDoctorsByDepartment(int departmentId) throws DatabaseException {
        return doctorService.getDoctorsByDepartment(departmentId);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) throws DatabaseException {
        return doctorService.getDoctorsBySpecialization(specialization);
    }

    public List<Department> getAllDepartments() throws DatabaseException {
        return doctorService.getAllDepartments();
    }

    public int getTotalDoctorsCount() {
        return doctorService.getTotalDoctorsCount();
    }
}
