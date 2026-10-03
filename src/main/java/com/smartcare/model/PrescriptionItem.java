package com.smartcare.model;

import java.math.BigDecimal;

/**
 * Model representing an Individual Medicine Item within a Prescription.
 */
public class PrescriptionItem {
    private int itemId;
    private int prescriptionId;
    private int medicineId;
    private String medicineName;
    private String dosage; // e.g. "500 mg"
    private String frequency; // e.g. "Twice a day after food"
    private int durationDays; // e.g. 5 days
    private int quantity; // Total units/tablets
    private String notes;
    private BigDecimal unitPrice;

    public PrescriptionItem() {
        this.unitPrice = BigDecimal.ZERO;
    }

    public PrescriptionItem(int itemId, int prescriptionId, int medicineId, String medicineName,
                            String dosage, String frequency, int durationDays, int quantity, String notes) {
        this.itemId = itemId;
        this.prescriptionId = prescriptionId;
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.quantity = quantity;
        this.notes = notes;
        this.unitPrice = BigDecimal.ZERO;
    }

    public BigDecimal getTotalPrice() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(int prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @Override
    public String toString() {
        return medicineName + " (" + dosage + ") - " + frequency + " for " + durationDays + " days [Qty: " + quantity + "]";
    }
}
