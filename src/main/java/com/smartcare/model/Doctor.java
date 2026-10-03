package com.smartcare.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing a Medical Practitioner / Doctor in SmartCare Hospital.
 */
public class Doctor {
    private int doctorId;
    private Integer userId;
    private String fullName;
    private String specialization;
    private Integer departmentId;
    private String departmentName;
    private String licenseNumber;
    private String phone;
    private String email;
    private BigDecimal consultationFee;
    private String qualification;
    private int experienceYears;
    private String availableDays;
    private boolean available;
    private LocalDateTime createdAt;

    public Doctor() {
        this.consultationFee = new BigDecimal("500.00");
        this.availableDays = "Mon,Tue,Wed,Thu,Fri";
        this.available = true;
    }

    public Doctor(int doctorId, String fullName, String specialization, Integer departmentId,
                  String departmentName, String licenseNumber, String phone, String email,
                  BigDecimal consultationFee, String qualification, int experienceYears) {
        this.doctorId = doctorId;
        this.fullName = fullName;
        this.specialization = specialization;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.email = email;
        this.consultationFee = consultationFee;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.availableDays = "Mon,Tue,Wed,Thu,Fri";
        this.available = true;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + specialization + " - " + (departmentName != null ? departmentName : "General") + ")";
    }
}
