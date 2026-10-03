package com.smartcare.dao.impl;

import com.smartcare.dao.MedicineDAO;
import com.smartcare.exception.DatabaseException;
import com.smartcare.model.Medicine;
import com.smartcare.util.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation of MedicineDAO for pharmacy stock management.
 */
public class MedicineDAOImpl implements MedicineDAO {

    private static final Logger LOGGER = Logger.getLogger(MedicineDAOImpl.class.getName());

    private static final String BASE_QUERY =
            "SELECT medicine_id, medicine_name, generic_name, category, dosage_form, " +
            "unit_price, stock_quantity, reorder_level, manufacturer, expiry_date, created_at " +
            "FROM medicines ";

    @Override
    public Optional<Medicine> findById(int medicineId) {
        String sql = BASE_QUERY + "WHERE medicine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medicineId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding medicine ID: " + medicineId, e);
            throw new DatabaseException("Failed to find medicine: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Medicine> findAll() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_QUERY + "ORDER BY medicine_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all medicines", e);
            throw new DatabaseException("Failed to fetch medicines: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Medicine> search(String keyword) {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE medicine_name LIKE ? OR generic_name LIKE ? OR category LIKE ? ORDER BY medicine_name";
        String pattern = "%" + keyword.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching medicines: " + keyword, e);
            throw new DatabaseException("Failed to search medicines: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Medicine> findLowStock() {
        List<Medicine> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE stock_quantity <= reorder_level ORDER BY stock_quantity ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching low stock medicines", e);
            throw new DatabaseException("Failed to fetch low stock: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean create(Medicine m) {
        String sql = "INSERT INTO medicines (medicine_name, generic_name, category, dosage_form, " +
                     "unit_price, stock_quantity, reorder_level, manufacturer, expiry_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, m.getMedicineName());
            stmt.setString(2, m.getGenericName());
            stmt.setString(3, m.getCategory());
            stmt.setString(4, m.getDosageForm());
            stmt.setBigDecimal(5, m.getUnitPrice() != null ? m.getUnitPrice() : BigDecimal.ZERO);
            stmt.setInt(6, m.getStockQuantity());
            stmt.setInt(7, m.getReorderLevel());
            stmt.setString(8, m.getManufacturer());
            if (m.getExpiryDate() != null) stmt.setDate(9, Date.valueOf(m.getExpiryDate()));
            else stmt.setNull(9, Types.DATE);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) m.setMedicineId(rs.getInt(1));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating medicine: " + m.getMedicineName(), e);
            throw new DatabaseException("Failed to add medicine: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Medicine m) {
        String sql = "UPDATE medicines SET medicine_name=?, generic_name=?, category=?, dosage_form=?, " +
                     "unit_price=?, stock_quantity=?, reorder_level=?, manufacturer=?, expiry_date=? " +
                     "WHERE medicine_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getMedicineName());
            stmt.setString(2, m.getGenericName());
            stmt.setString(3, m.getCategory());
            stmt.setString(4, m.getDosageForm());
            stmt.setBigDecimal(5, m.getUnitPrice() != null ? m.getUnitPrice() : BigDecimal.ZERO);
            stmt.setInt(6, m.getStockQuantity());
            stmt.setInt(7, m.getReorderLevel());
            stmt.setString(8, m.getManufacturer());
            if (m.getExpiryDate() != null) stmt.setDate(9, Date.valueOf(m.getExpiryDate()));
            else stmt.setNull(9, Types.DATE);
            stmt.setInt(10, m.getMedicineId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating medicine ID: " + m.getMedicineId(), e);
            throw new DatabaseException("Failed to update medicine: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStock(int medicineId, int quantityDelta) {
        String sql = "UPDATE medicines SET stock_quantity = stock_quantity + ? WHERE medicine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantityDelta);
            stmt.setInt(2, medicineId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating stock for medicine ID: " + medicineId, e);
            throw new DatabaseException("Failed to update stock: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int medicineId) {
        String sql = "DELETE FROM medicines WHERE medicine_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, medicineId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting medicine ID: " + medicineId, e);
            throw new DatabaseException("Failed to delete medicine: " + e.getMessage(), e);
        }
    }

    @Override
    public int getTotalMedicineCount() {
        String sql = "SELECT COUNT(*) FROM medicines";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting medicines", e);
            return 0;
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setMedicineName(rs.getString("medicine_name"));
        m.setGenericName(rs.getString("generic_name"));
        m.setCategory(rs.getString("category"));
        m.setDosageForm(rs.getString("dosage_form"));
        BigDecimal price = rs.getBigDecimal("unit_price");
        m.setUnitPrice(price != null ? price : BigDecimal.ZERO);
        m.setStockQuantity(rs.getInt("stock_quantity"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        m.setManufacturer(rs.getString("manufacturer"));
        Date expiry = rs.getDate("expiry_date");
        if (expiry != null) m.setExpiryDate(expiry.toLocalDate());
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) m.setCreatedAt(ts.toLocalDateTime());
        return m;
    }
}
