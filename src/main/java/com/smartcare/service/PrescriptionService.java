package com.smartcare.service;

import com.smartcare.enums.PrescriptionStatus;
import com.smartcare.model.Medicine;
import com.smartcare.model.Prescription;
import com.smartcare.model.PrescriptionItem;

import java.util.List;

public interface PrescriptionService {
    Prescription createPrescription(Prescription prescription, List<PrescriptionItem> items);
    boolean dispensePrescription(int prescriptionId);
    boolean cancelPrescription(int prescriptionId);
    Prescription getPrescriptionById(int prescriptionId);
    List<Prescription> getAllPrescriptions();
    List<Prescription> getPrescriptionsByPatient(int patientId);
    List<Prescription> getPrescriptionsByDoctor(int doctorId);
    List<Prescription> getPrescriptionsByStatus(PrescriptionStatus status);
    List<Prescription> searchPrescriptions(String keyword);

    // Medicines
    List<Medicine> getAllMedicines();
    List<Medicine> searchMedicines(String keyword);
    List<Medicine> getLowStockMedicines();
    Medicine addMedicine(Medicine medicine);
    boolean updateMedicine(Medicine medicine);
    boolean adjustMedicineStock(int medicineId, int quantityDelta);
}
