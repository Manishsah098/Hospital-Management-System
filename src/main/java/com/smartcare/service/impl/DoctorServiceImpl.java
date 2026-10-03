package com.smartcare.service.impl;

import com.smartcare.dao.DepartmentDAO;
import com.smartcare.dao.DoctorDAO;
import com.smartcare.dao.impl.DepartmentDAOImpl;
import com.smartcare.dao.impl.DoctorDAOImpl;
import com.smartcare.exception.DoctorNotFoundException;
import com.smartcare.exception.ValidationException;
import com.smartcare.model.Department;
import com.smartcare.model.Doctor;
import com.smartcare.service.DoctorService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class DoctorServiceImpl implements DoctorService {
    private final DoctorDAO doctorDAO;
    private final DepartmentDAO departmentDAO;
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,14}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public DoctorServiceImpl() {
        this.doctorDAO = new DoctorDAOImpl();
        this.departmentDAO = new DepartmentDAOImpl();
    }

    public DoctorServiceImpl(DoctorDAO doctorDAO, DepartmentDAO departmentDAO) {
        this.doctorDAO = doctorDAO;
        this.departmentDAO = departmentDAO;
    }

    @Override
    public Doctor registerDoctor(Doctor doctor) {
        validateDoctor(doctor);

        Optional<Doctor> existing = doctorDAO.findByLicenseNumber(doctor.getLicenseNumber().trim());
        if (existing.isPresent()) {
            throw new ValidationException("Doctor with Medical License " + doctor.getLicenseNumber() + " already exists.");
        }

        boolean created = doctorDAO.create(doctor);
        if (!created) {
            throw new ValidationException("Could not save doctor profile. Please try again.");
        }
        return doctor;
    }

    @Override
    public Doctor updateDoctor(Doctor doctor) {
        if (doctor.getDoctorId() <= 0) {
            throw new ValidationException("Invalid doctor ID");
        }
        validateDoctor(doctor);

        boolean updated = doctorDAO.update(doctor);
        if (!updated) {
            throw new DoctorNotFoundException("Doctor record could not be updated or does not exist.");
        }
        return doctor;
    }

    @Override
    public boolean deleteDoctor(int doctorId) {
        if (doctorId <= 0) {
            throw new ValidationException("Invalid doctor ID");
        }
        return doctorDAO.delete(doctorId);
    }

    @Override
    public Doctor getDoctorById(int doctorId) {
        return doctorDAO.findById(doctorId)
                .orElseThrow(() -> new DoctorNotFoundException("Doctor with ID " + doctorId + " not found."));
    }

    @Override
    public List<Doctor> getAllDoctors() {
        return doctorDAO.findAll();
    }

    @Override
    public List<Doctor> searchDoctors(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllDoctors();
        }
        return doctorDAO.search(keyword.trim());
    }

    @Override
    public List<Doctor> getDoctorsByDepartment(int departmentId) {
        return doctorDAO.findByDepartment(departmentId);
    }

    @Override
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        return doctorDAO.findBySpecialization(specialization);
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentDAO.findAll();
    }

    @Override
    public int getTotalDoctorsCount() {
        return doctorDAO.getTotalDoctorCount();
    }

    private void validateDoctor(Doctor d) {
        if (d == null) {
            throw new ValidationException("Doctor data cannot be null");
        }
        if (d.getFullName() == null || d.getFullName().trim().length() < 2) {
            throw new ValidationException("Doctor name must be at least 2 characters long");
        }
        if (d.getSpecialization() == null || d.getSpecialization().trim().isEmpty()) {
            throw new ValidationException("Specialization is required");
        }
        if (d.getLicenseNumber() == null || d.getLicenseNumber().trim().isEmpty()) {
            throw new ValidationException("Medical License Number is required");
        }
        if (d.getPhone() == null || !PHONE_PATTERN.matcher(d.getPhone().trim()).matches()) {
            throw new ValidationException("Phone number must contain 10-14 digits");
        }
        if (d.getEmail() != null && !d.getEmail().trim().isEmpty() && !EMAIL_PATTERN.matcher(d.getEmail().trim()).matches()) {
            throw new ValidationException("Invalid email format");
        }
        if (d.getConsultationFee() == null || d.getConsultationFee().compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Consultation fee must be greater than or equal to zero");
        }
        if (d.getExperienceYears() < 0) {
            throw new ValidationException("Experience years cannot be negative");
        }
    }
}
