package com.smartcare.dao;

import com.smartcare.model.Medicine;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Medicine inventory entity operations.
 */
public interface MedicineDAO {
    Optional<Medicine> findById(int medicineId);
    List<Medicine> findAll();
    List<Medicine> search(String keyword);
    List<Medicine> findLowStock();
    boolean create(Medicine medicine);
    boolean update(Medicine medicine);
    boolean updateStock(int medicineId, int quantityDelta);
    boolean delete(int medicineId);
    int getTotalMedicineCount();
}
