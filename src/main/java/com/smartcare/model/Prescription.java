package com.smartcare.model;

import com.smartcare.enums.PrescriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Prescription containing one or more medicine items.
 */
public class Prescription {
    private int prescriptionId;
    private int patientId;
    private String patientName;
    private String patientCode;
    private int doctorId;
    private String doctorName;
    private Integer recordId;
    private PrescriptionStatus status;
    private String instructions;
    private LocalDateTime prescribedAt;
    private LocalDateTime dispensedAt;
    private List<PrescriptionItem> items = new ArrayList<>();

    public Prescription() {
        this.status = PrescriptionStatus.PENDING;
    }

    public Prescription(int prescriptionId, int patientId, int doctorId, Integer recordId, String instructions) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.recordId = recordId;
        this.instructions = instructions;
        this.status = PrescriptionStatus.PENDING;
    }

    public BigDecimal calculateTotalCost() {
        return items.stream()
                .map(PrescriptionItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientCode() {
        return patientCode;
    }

    public void setPatientCode(String patientCode) {
        this.patientCode = patientCode;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public PrescriptionStatus getStatus() {
        return status;
    }

    public void setStatus(PrescriptionStatus status) {
        this.status = status;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public LocalDateTime getPrescribedAt() {
        return prescribedAt;
    }

    public void setPrescribedAt(LocalDateTime prescribedAt) {
        this.prescribedAt = prescribedAt;
    }

    public LocalDateTime getDispensedAt() {
        return dispensedAt;
    }

    public void setDispensedAt(LocalDateTime dispensedAt) {
        this.dispensedAt = dispensedAt;
    }

    public List<PrescriptionItem> getItems() {
        return items;
    }

    public void setItems(List<PrescriptionItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public void addItem(PrescriptionItem item) {
        this.items.add(item);
    }

    @Override
    public String toString() {
        return "Prescription #" + prescriptionId + " for " + patientName + " by " + doctorName + " (" + items.size() + " medicines)";
    }
}
