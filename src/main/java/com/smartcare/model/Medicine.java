package com.smartcare.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model representing a Pharmaceutical Medicine item.
 */
public class Medicine {
    private int medicineId;
    private String medicineName;
    private String genericName;
    private String category;
    private String dosageForm; // Tablet, Syrup, Capsule, Injection
    private BigDecimal unitPrice;
    private int stockQuantity;
    private int reorderLevel;
    private String manufacturer;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;

    public Medicine() {
        this.unitPrice = BigDecimal.ZERO;
        this.stockQuantity = 0;
        this.reorderLevel = 20;
    }

    public Medicine(int medicineId, String medicineName, String genericName, String category,
                    String dosageForm, BigDecimal unitPrice, int stockQuantity, int reorderLevel,
                    String manufacturer, LocalDate expiryDate) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.category = category;
        this.dosageForm = dosageForm;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
        this.manufacturer = manufacturer;
        this.expiryDate = expiryDate;
    }

    public boolean isLowStock() {
        return stockQuantity <= reorderLevel;
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

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return medicineName + " (" + dosageForm + ", ₹ " + unitPrice + " | Stock: " + stockQuantity + ")";
    }
}
