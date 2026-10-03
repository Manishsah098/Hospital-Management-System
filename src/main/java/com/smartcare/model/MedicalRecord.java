package com.smartcare.model;

import java.time.LocalDateTime;

/**
 * Model representing a Medical Clinical Consultation Record.
 */
public class MedicalRecord {
    private int recordId;
    private int patientId;
    private String patientName;
    private String patientCode;
    private int doctorId;
    private String doctorName;
    private Integer appointmentId;
    private String diagnosis;
    private String symptoms;
    private String treatmentPlan;
    private String vitalSigns; // e.g. "BP: 120/80, Pulse: 72 bpm, Temp: 98.6 F, SpO2: 99%"
    private String doctorNotes;
    private LocalDateTime recordedAt;

    public MedicalRecord() {}

    public MedicalRecord(int recordId, int patientId, int doctorId, Integer appointmentId,
                         String diagnosis, String symptoms, String treatmentPlan, String vitalSigns) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.diagnosis = diagnosis;
        this.symptoms = symptoms;
        this.treatmentPlan = treatmentPlan;
        this.vitalSigns = vitalSigns;
    }

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
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

    public Integer getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getTreatmentPlan() {
        return treatmentPlan;
    }

    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }

    public String getVitalSigns() {
        return vitalSigns;
    }

    public void setVitalSigns(String vitalSigns) {
        this.vitalSigns = vitalSigns;
    }

    public String getDoctorNotes() {
        return doctorNotes;
    }

    public void setDoctorNotes(String doctorNotes) {
        this.doctorNotes = doctorNotes;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    @Override
    public String toString() {
        return "Record #" + recordId + " [Diagnosis: " + diagnosis + "] for " + patientName + " by " + doctorName;
    }
}
