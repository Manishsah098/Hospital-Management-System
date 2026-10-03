package com.smartcare.ui;

import com.smartcare.dao.impl.MedicineDAOImpl;
import com.smartcare.model.Medicine;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Pharmacy Stock Management Panel — SmartCare Hospital System.
 * Manage medicine inventory, stock levels, and reorder alerts.
 */
public class PharmacyPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final MedicineDAOImpl medicineDAO;

    private MedicineTableModel tableModel;
    private JTable medicineTable;
    private TableRowSorter<MedicineTableModel> sorter;
    private JTextField searchField;
    private JLabel statusLabel;

    private static final Color ACCENT_ORANGE = new Color(253, 126, 20);
    private static final Color ACCENT_GREEN  = new Color(25, 135, 84);
    private static final Color ACCENT_RED    = new Color(220, 53, 69);
    private static final Color BG_LIGHT      = new Color(245, 247, 250);
    private static final Color PANEL_WHITE   = Color.WHITE;

    public PharmacyPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.medicineDAO = new MedicineDAOImpl();
        setLayout(new BorderLayout(0, 0));
        setBackground(BG_LIGHT);
        initComponents();
        loadData();
    }

    private void initComponents() {
        // ── TOP BAR ──────────────────────────────────────────────────────────
        JPanel topBar = new JPanel(new BorderLayout(10, 0));
        topBar.setBackground(PANEL_WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                new EmptyBorder(14, 20, 14, 20)));

        JLabel title = new JLabel("🏪  Pharmacy Stock Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search medicine name / category...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton btnAdd        = createButton("+ Add Medicine", ACCENT_ORANGE);
        JButton btnLowStock   = createButton("⚠ Low Stock", ACCENT_RED);
        JButton btnRestock    = createButton("📦 Restock", ACCENT_GREEN);
        JButton btnRefresh    = createButton("↻ Refresh", new Color(108, 117, 125));

        btnAdd.addActionListener(e -> openAddMedicineDialog(null));
        btnLowStock.addActionListener(e -> showLowStock());
        btnRestock.addActionListener(e -> restockSelected());
        btnRefresh.addActionListener(e -> loadData());

        actionPanel.add(searchField);
        actionPanel.add(btnLowStock);
        actionPanel.add(btnAdd);
        actionPanel.add(btnRestock);
        actionPanel.add(btnRefresh);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ── TABLE ─────────────────────────────────────────────────────────────
        tableModel    = new MedicineTableModel();
        medicineTable = new JTable(tableModel);
        medicineTable.setRowHeight(30);
        medicineTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        medicineTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        medicineTable.getTableHeader().setBackground(new Color(248, 249, 250));
        medicineTable.setSelectionBackground(new Color(255, 235, 210));
        medicineTable.setGridColor(new Color(233, 236, 239));
        medicineTable.setShowHorizontalLines(true);
        medicineTable.setShowVerticalLines(false);
        medicineTable.setFillsViewportHeight(true);

        // Stock column — color low stock red
        medicineTable.getColumnModel().getColumn(5).setCellRenderer(new StockRenderer());

        int[] widths = {50, 170, 130, 100, 100, 80, 100, 130, 110};
        for (int i = 0; i < widths.length; i++)
            medicineTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        sorter = new TableRowSorter<>(tableModel);
        medicineTable.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(medicineTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_WHITE);

        // ── BOTTOM BAR ────────────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(PANEL_WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        statusLabel = new JLabel("Loading pharmacy stock...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));

        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rowActions.setOpaque(false);

        JButton btnEdit   = createButton("✏ Edit", new Color(13, 110, 253));
        JButton btnDelete = createButton("🗑 Delete", ACCENT_RED);

        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());

        rowActions.add(btnEdit);
        rowActions.add(btnDelete);

        bottomBar.add(statusLabel, BorderLayout.WEST);
        bottomBar.add(rowActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // ── DATA ─────────────────────────────────────────────────────────────────

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Medicine> meds = medicineDAO.findAll();
                tableModel.setData(meds);
                long lowCount = meds.stream().filter(Medicine::isLowStock).count();
                statusLabel.setText("Total: " + meds.size() + " medicine(s)  |  ⚠ Low Stock: " + lowCount);
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
    }

    private void showLowStock() {
        try {
            List<Medicine> low = medicineDAO.findLowStock();
            tableModel.setData(low);
            statusLabel.setText("⚠  Showing " + low.size() + " low-stock medicine(s). Click Refresh to see all.");
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1, 2, 3, 4));
    }

    private void restockSelected() {
        int row = medicineTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine to restock.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medicine med = tableModel.getAt(medicineTable.convertRowIndexToModel(row));
        String input = JOptionPane.showInputDialog(this,
                "Restock '" + med.getMedicineName() + "'\nCurrent stock: " + med.getStockQuantity()
                + "\nEnter quantity to add:", "Restock Medicine", JOptionPane.PLAIN_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;
        try {
            int qty = Integer.parseInt(input.trim());
            if (qty <= 0) { JOptionPane.showMessageDialog(this, "Enter a positive quantity.", "Invalid", JOptionPane.WARNING_MESSAGE); return; }
            medicineDAO.updateStock(med.getMedicineId(), qty);
            JOptionPane.showMessageDialog(this, "✅  Stock updated! New quantity: " + (med.getStockQuantity() + qty),
                    "Restocked", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid quantity entered.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int row = medicineTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medicine med = tableModel.getAt(medicineTable.convertRowIndexToModel(row));
        openAddMedicineDialog(med);
    }

    private void deleteSelected() {
        int row = medicineTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a medicine to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Medicine med = tableModel.getAt(medicineTable.convertRowIndexToModel(row));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete '" + med.getMedicineName() + "' from pharmacy?\nThis is permanent.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            medicineDAO.delete(med.getMedicineId());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openAddMedicineDialog(Medicine existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "Edit Medicine" : "Add New Medicine", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setBackground(new Color(24, 43, 73));
        JLabel headerTitle = new JLabel(isEdit ? "✏  Edit Medicine" : "🏪  Add New Medicine");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerTitle.setForeground(Color.WHITE);
        header.add(headerTitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(PANEL_WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameField       = createField(isEdit ? existing.getMedicineName()   : "");
        JTextField genericField    = createField(isEdit ? existing.getGenericName()    : "");
        JTextField categoryField   = createField(isEdit ? existing.getCategory()       : "");
        String[] dosageForms = {"Tablet", "Capsule", "Syrup", "Injection", "Ointment", "Drops", "Inhaler", "Patch"};
        JComboBox<String> dosageCombo = new JComboBox<>(dosageForms);
        if (isEdit && existing.getDosageForm() != null) dosageCombo.setSelectedItem(existing.getDosageForm());
        JTextField priceField      = createField(isEdit && existing.getUnitPrice() != null ? existing.getUnitPrice().toPlainString() : "0.00");
        JTextField stockField      = createField(isEdit ? String.valueOf(existing.getStockQuantity())  : "0");
        JTextField reorderField    = createField(isEdit ? String.valueOf(existing.getReorderLevel())   : "20");
        JTextField mfgField        = createField(isEdit ? (existing.getManufacturer() != null ? existing.getManufacturer() : "") : "");
        JTextField expiryField     = createField(isEdit && existing.getExpiryDate() != null
                ? existing.getExpiryDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        expiryField.putClientProperty("JTextField.placeholderText", "yyyy-MM-dd (optional)");

        addRow(form, gbc, 0, "Medicine Name *:", nameField);
        addRow(form, gbc, 1, "Generic Name:", genericField);
        addRow(form, gbc, 2, "Category:", categoryField);
        addRow(form, gbc, 3, "Dosage Form:", dosageCombo);
        addRow(form, gbc, 4, "Unit Price (₹):", priceField);
        addRow(form, gbc, 5, "Stock Quantity:", stockField);
        addRow(form, gbc, 6, "Reorder Level:", reorderField);
        addRow(form, gbc, 7, "Manufacturer:", mfgField);
        addRow(form, gbc, 8, "Expiry Date:", expiryField);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errLabel.setForeground(ACCENT_RED);
        gbc.gridx = 0; gbc.gridy = 9; gbc.gridwidth = 2;
        form.add(errLabel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(new Color(248, 249, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton(isEdit ? "💾  Save Changes" : "💾  Add Medicine");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(ACCENT_ORANGE);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            try {
                if (nameField.getText().trim().isEmpty()) { errLabel.setText("Medicine name is required."); return; }

                Medicine med = isEdit ? existing : new Medicine();
                med.setMedicineName(nameField.getText().trim());
                med.setGenericName(genericField.getText().trim());
                med.setCategory(categoryField.getText().trim());
                med.setDosageForm(dosageCombo.getSelectedItem().toString());
                try { med.setUnitPrice(new BigDecimal(priceField.getText().trim())); }
                catch (NumberFormatException ex) { errLabel.setText("Invalid price format."); return; }
                try { med.setStockQuantity(Integer.parseInt(stockField.getText().trim())); }
                catch (NumberFormatException ex) { errLabel.setText("Invalid stock quantity."); return; }
                try { med.setReorderLevel(Integer.parseInt(reorderField.getText().trim())); }
                catch (NumberFormatException ex) { errLabel.setText("Invalid reorder level."); return; }
                med.setManufacturer(mfgField.getText().trim());
                if (!expiryField.getText().trim().isEmpty()) {
                    try { med.setExpiryDate(LocalDate.parse(expiryField.getText().trim())); }
                    catch (DateTimeParseException ex) { errLabel.setText("Invalid date. Use yyyy-MM-dd."); return; }
                }

                boolean ok = isEdit ? medicineDAO.update(med) : medicineDAO.create(med);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog,
                            "✅  Medicine " + (isEdit ? "updated" : "added") + " successfully!",
                            isEdit ? "Updated" : "Added", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    errLabel.setText("Operation failed. Please try again.");
                }
            } catch (Exception ex) {
                errLabel.setText("Error: " + ex.getMessage());
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private JTextField createField(String val) {
        JTextField f = new JTextField(val, 20);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(52, 58, 64));
        form.add(lbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(comp, gbc);
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // ── TABLE MODEL ───────────────────────────────────────────────────────────

    private static class MedicineTableModel extends AbstractTableModel {
        private static final String[] COLS = {"#", "Medicine Name", "Generic Name", "Category", "Form", "Stock", "Reorder Lvl", "Unit Price (₹)", "Expiry"};
        private List<Medicine> data = new ArrayList<>();

        void setData(List<Medicine> data) { this.data = data != null ? data : new ArrayList<>(); fireTableDataChanged(); }
        Medicine getAt(int row) { return data.get(row); }
        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            Medicine m = data.get(row);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            return switch (col) {
                case 0 -> m.getMedicineId();
                case 1 -> m.getMedicineName() != null ? m.getMedicineName() : "-";
                case 2 -> m.getGenericName()  != null ? m.getGenericName()  : "-";
                case 3 -> m.getCategory()     != null ? m.getCategory()     : "-";
                case 4 -> m.getDosageForm()   != null ? m.getDosageForm()   : "-";
                case 5 -> m.getStockQuantity();
                case 6 -> m.getReorderLevel();
                case 7 -> m.getUnitPrice()   != null ? "₹ " + m.getUnitPrice().toPlainString() : "₹ 0.00";
                case 8 -> m.getExpiryDate()  != null ? m.getExpiryDate().format(fmt) : "-";
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int c) {
            return (c == 0 || c == 5 || c == 6) ? Integer.class : String.class;
        }
    }

    // Stock cell renderer — red for low stock
    private class StockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!sel && row < tableModel.getRowCount()) {
                Medicine m = tableModel.getAt(t.convertRowIndexToModel(row));
                if (m.isLowStock()) {
                    setBackground(new Color(248, 215, 218));
                    setForeground(ACCENT_RED);
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    setBackground(new Color(209, 231, 221));
                    setForeground(ACCENT_GREEN);
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
            }
            return this;
        }
    }
}
