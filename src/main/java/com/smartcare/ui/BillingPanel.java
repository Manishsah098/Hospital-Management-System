package com.smartcare.ui;

import com.smartcare.dao.impl.PatientDAOImpl;
import com.smartcare.model.Patient;
import com.smartcare.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Billing & Invoices Panel — SmartCare Hospital System.
 * Create itemized bills, record partial/full payments, search records, and print professional invoices.
 */
public class BillingPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final PatientDAOImpl patientDAO;

    private BillTableModel tableModel;
    private JTable billTable;
    private JLabel statusLabel;
    private JTextField searchField;

    private JLabel totalBillsVal;
    private JLabel totalCollectedVal;
    private JLabel pendingAmountVal;

    private static final Color PRIMARY_BLUE = new Color(24, 43, 73);
    private static final Color ACCENT_TEAL  = new Color(32, 178, 170);
    private static final Color ACCENT_GREEN = new Color(25, 135, 84);
    private static final Color ACCENT_RED   = new Color(220, 53, 69);
    private static final Color BG_LIGHT     = new Color(245, 247, 250);
    private static final Color PANEL_WHITE  = Color.WHITE;

    public BillingPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.patientDAO  = new PatientDAOImpl();
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

        JLabel title = new JLabel("💳  Billing & Invoices");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search patient name / bill #...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { filterData(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { filterData(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterData(); }
        });

        JButton btnNew     = createButton("+ New Bill", ACCENT_TEAL);
        JButton btnPay     = createButton("✓ Record Payment", ACCENT_GREEN);
        JButton btnPrint   = createButton("🖨️ View / Print Invoice", new Color(13, 110, 253));
        JButton btnRefresh = createButton("↻ Refresh", new Color(108, 117, 125));

        btnNew.addActionListener(e -> openNewBillDialog());
        btnPay.addActionListener(e -> markBillPaid());
        btnPrint.addActionListener(e -> printSelectedInvoice());
        btnRefresh.addActionListener(e -> loadData());

        actionPanel.add(searchField);
        actionPanel.add(btnNew);
        actionPanel.add(btnPay);
        actionPanel.add(btnPrint);
        actionPanel.add(btnRefresh);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ── SUMMARY CARDS ─────────────────────────────────────────────────────
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        cardsPanel.setBackground(BG_LIGHT);
        cardsPanel.setBorder(new EmptyBorder(12, 20, 12, 20));
        
        JPanel card1 = createSummaryCard("💳  Total Invoices", "0", new Color(13, 110, 253));
        totalBillsVal = (JLabel) card1.getClientProperty("valLabel");
        
        JPanel card2 = createSummaryCard("✅  Total Revenue Collected", "₹ 0.00", ACCENT_GREEN);
        totalCollectedVal = (JLabel) card2.getClientProperty("valLabel");
        
        JPanel card3 = createSummaryCard("⏳  Outstanding Balance", "₹ 0.00", ACCENT_RED);
        pendingAmountVal = (JLabel) card3.getClientProperty("valLabel");

        cardsPanel.add(card1);
        cardsPanel.add(card2);
        cardsPanel.add(card3);

        // ── TABLE ─────────────────────────────────────────────────────────────
        tableModel = new BillTableModel();
        billTable  = new JTable(tableModel);
        billTable.setRowHeight(32);
        billTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        billTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        billTable.getTableHeader().setBackground(new Color(248, 249, 250));
        billTable.setSelectionBackground(new Color(204, 245, 245));
        billTable.setSelectionForeground(new Color(20, 40, 60));
        billTable.setGridColor(new Color(233, 236, 239));
        billTable.setShowHorizontalLines(true);
        billTable.setShowVerticalLines(false);
        billTable.setFillsViewportHeight(true);

        // Center Align Bill #, Mode, Date
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        billTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        billTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        billTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Status column custom badge renderer
        billTable.getColumnModel().getColumn(5).setCellRenderer((table, value, isSelected, hasFocus, row, col) -> {
            JLabel lbl = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setOpaque(true);
            String v = value != null ? value.toString() : "";
            if (!isSelected) {
                switch (v) {
                    case "PAID" -> {
                        lbl.setBackground(new Color(209, 231, 221));
                        lbl.setForeground(new Color(25, 135, 84));
                    }
                    case "UNPAID" -> {
                        lbl.setBackground(new Color(248, 215, 218));
                        lbl.setForeground(ACCENT_RED);
                    }
                    case "PARTIALLY_PAID" -> {
                        lbl.setBackground(new Color(255, 243, 205));
                        lbl.setForeground(new Color(133, 100, 4));
                    }
                    default -> {
                        lbl.setBackground(new Color(226, 227, 229));
                        lbl.setForeground(new Color(56, 61, 65));
                    }
                }
            }
            return lbl;
        });

        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(222, 226, 230)));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(BG_LIGHT);
        tableWrapper.setBorder(new EmptyBorder(0, 20, 10, 20));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        JPanel centerContent = new JPanel(new BorderLayout());
        centerContent.setBackground(BG_LIGHT);
        centerContent.add(cardsPanel, BorderLayout.NORTH);
        centerContent.add(tableWrapper, BorderLayout.CENTER);

        // ── BOTTOM BAR ────────────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(PANEL_WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
                new EmptyBorder(8, 20, 8, 20)));

        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));
        bottomBar.add(statusLabel, BorderLayout.WEST);

        add(topBar, BorderLayout.NORTH);
        add(centerContent, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // ── DATA OPERATIONS ───────────────────────────────────────────────────────

    private List<BillRow> allBills = new ArrayList<>();

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try (Connection conn = DatabaseConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT b.bill_id, b.bill_number, p.patient_id, p.full_name AS patient_name, " +
                     "b.subtotal, b.tax_amount, b.discount_amount, b.total_amount, " +
                     "b.paid_amount, b.payment_status, b.payment_mode, b.generated_at, b.notes " +
                     "FROM bills b JOIN patients p ON b.patient_id = p.patient_id " +
                     "ORDER BY b.bill_id DESC")) {

                allBills.clear();
                double totalRevenue = 0.0;
                double totalPending = 0.0;

                while (rs.next()) {
                    int billId = rs.getInt("bill_id");
                    String billNumber = rs.getString("bill_number");
                    if (billNumber == null || billNumber.isBlank()) {
                        billNumber = "BILL-" + billId;
                    }
                    String patientName = rs.getString("patient_name");
                    double subtotal = rs.getDouble("subtotal");
                    double tax = rs.getDouble("tax_amount");
                    double discount = rs.getDouble("discount_amount");
                    double total = rs.getDouble("total_amount");
                    double paid = rs.getDouble("paid_amount");
                    String status = rs.getString("payment_status");
                    String mode = rs.getString("payment_mode");
                    LocalDateTime date = rs.getTimestamp("generated_at") != null
                            ? rs.getTimestamp("generated_at").toLocalDateTime() : LocalDateTime.now();
                    String notes = rs.getString("notes");

                    allBills.add(new BillRow(billId, billNumber, patientName, subtotal, tax, discount, total, paid, status, mode, date, notes));

                    totalRevenue += paid;
                    totalPending += Math.max(0, total - paid);
                }

                tableModel.setData(allBills);
                totalBillsVal.setText(String.valueOf(allBills.size()));
                totalCollectedVal.setText(String.format("₹ %.2f", totalRevenue));
                pendingAmountVal.setText(String.format("₹ %.2f", totalPending));
                statusLabel.setText("Total " + allBills.size() + " invoice(s) loaded successfully.");
            } catch (Exception ex) {
                statusLabel.setText("Error loading bills: " + ex.getMessage());
            }
        });
    }

    private void filterData() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            tableModel.setData(allBills);
            return;
        }
        List<BillRow> filtered = new ArrayList<>();
        for (BillRow b : allBills) {
            if (b.billNumber().toLowerCase().contains(q) ||
                b.patientName().toLowerCase().contains(q) ||
                b.status().toLowerCase().contains(q) ||
                b.paymentMode().toLowerCase().contains(q)) {
                filtered.add(b);
            }
        }
        tableModel.setData(filtered);
    }

    private void openNewBillDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Create New Patient Invoice", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(580, 520);
        dialog.setLocationRelativeTo(this);

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setBackground(PRIMARY_BLUE);
        JLabel headerTitle = new JLabel("💳  Generate Patient Bill & Invoice");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerTitle.setForeground(Color.WHITE);
        header.add(headerTitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 5, 6, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Patient selector
        List<Patient> patients = patientDAO.findAll();
        JComboBox<String> patientCombo = new JComboBox<>();
        patientCombo.addItem("-- Select Patient --");
        for (Patient p : patients) {
            patientCombo.addItem(p.getPatientId() + " | " + p.getFullName() + " (" + p.getPatientCode() + ")");
        }
        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField consultationFee = createNumField("500.00");
        JTextField medicineCost    = createNumField("0.00");
        JTextField labCharges      = createNumField("0.00");
        JTextField roomCharges     = createNumField("0.00");
        JTextField otherCharges    = createNumField("0.00");
        JTextField discountField   = createNumField("0.00");
        JTextField taxField        = createNumField("5.00"); // 5% GST

        JLabel subtotalLabel = new JLabel("₹ 500.00");
        subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JLabel totalLabel = new JLabel("₹ 525.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLabel.setForeground(ACCENT_TEAL);

        // Valid MySQL ENUM: CASH, CREDIT_CARD, DEBIT_CARD, UPI, INSURANCE
        String[] methods = {"CASH", "CREDIT_CARD", "DEBIT_CARD", "UPI", "INSURANCE"};
        JComboBox<String> methodCombo = new JComboBox<>(methods);
        methodCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JTextField paidAmountField = createNumField("0.00");

        // Live total calc
        javax.swing.event.DocumentListener calc = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { recalc(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { recalc(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { recalc(); }
            void recalc() {
                double sub = parseDouble(consultationFee.getText()) +
                             parseDouble(medicineCost.getText()) +
                             parseDouble(labCharges.getText()) +
                             parseDouble(roomCharges.getText()) +
                             parseDouble(otherCharges.getText());
                double disc = parseDouble(discountField.getText());
                double taxRate = parseDouble(taxField.getText());
                double taxable = Math.max(0, sub - disc);
                double tax = (taxable * taxRate) / 100.0;
                double grandTotal = taxable + tax;

                subtotalLabel.setText(String.format("₹ %.2f", sub));
                totalLabel.setText(String.format("₹ %.2f", grandTotal));
            }
        };

        consultationFee.getDocument().addDocumentListener(calc);
        medicineCost.getDocument().addDocumentListener(calc);
        labCharges.getDocument().addDocumentListener(calc);
        roomCharges.getDocument().addDocumentListener(calc);
        otherCharges.getDocument().addDocumentListener(calc);
        discountField.getDocument().addDocumentListener(calc);
        taxField.getDocument().addDocumentListener(calc);

        addFormRow(form, gbc, 0, "Patient *:", patientCombo);
        addFormRow(form, gbc, 1, "Consultation Fee (₹):", consultationFee);
        addFormRow(form, gbc, 2, "Medicine Cost (₹):", medicineCost);
        addFormRow(form, gbc, 3, "Lab Tests Fee (₹):", labCharges);
        addFormRow(form, gbc, 4, "Room / Bed Charges (₹):", roomCharges);
        addFormRow(form, gbc, 5, "Other Procedures (₹):", otherCharges);
        addFormRow(form, gbc, 6, "Subtotal:", subtotalLabel);
        addFormRow(form, gbc, 7, "Discount (₹):", discountField);
        addFormRow(form, gbc, 8, "Tax / GST (%):", taxField);
        addFormRow(form, gbc, 9, "Grand Total:", totalLabel);
        addFormRow(form, gbc, 10, "Payment Mode:", methodCombo);
        addFormRow(form, gbc, 11, "Initial Paid (₹):", paidAmountField);

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 12; gbc.gridwidth = 2;
        form.add(errLabel, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(new Color(248, 249, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("💾  Generate & Save Invoice");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(ACCENT_TEAL);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            try {
                if (patientCombo.getSelectedIndex() <= 0) {
                    errLabel.setText("Please select a patient.");
                    return;
                }
                int patId = Integer.parseInt(patientCombo.getSelectedItem().toString().split(" \\| ")[0].trim());
                double consultation = parseDouble(consultationFee.getText());
                double medicine     = parseDouble(medicineCost.getText());
                double lab          = parseDouble(labCharges.getText());
                double room         = parseDouble(roomCharges.getText());
                double other        = parseDouble(otherCharges.getText());
                double subtotal     = consultation + medicine + lab + room + other;
                double discount     = parseDouble(discountField.getText());
                double taxPercent   = parseDouble(taxField.getText());
                double taxable      = Math.max(0, subtotal - discount);
                double taxAmount    = (taxable * taxPercent) / 100.0;
                double grandTotal   = taxable + taxAmount;
                double paidAmount   = parseDouble(paidAmountField.getText());

                String status = (paidAmount >= grandTotal && grandTotal > 0) ? "PAID"
                              : (paidAmount > 0) ? "PARTIALLY_PAID" : "UNPAID";

                String mode = methodCombo.getSelectedItem().toString();

                // Unique Bill Number format: BILL-YYYY-XXXXX
                long seq = (System.currentTimeMillis() % 100000);
                String billNum = "BILL-" + java.time.Year.now().getValue() + "-" + String.format("%05d", seq);

                String sql = "INSERT INTO bills (bill_number, patient_id, subtotal, tax_percentage, tax_amount, " +
                             "discount_amount, total_amount, paid_amount, payment_status, payment_mode, notes) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                int generatedBillId = 0;
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, billNum);
                    stmt.setInt(2, patId);
                    stmt.setDouble(3, subtotal);
                    stmt.setDouble(4, taxPercent);
                    stmt.setDouble(5, taxAmount);
                    stmt.setDouble(6, discount);
                    stmt.setDouble(7, grandTotal);
                    stmt.setDouble(8, paidAmount);
                    stmt.setString(9, status);
                    stmt.setString(10, mode);
                    stmt.setString(11, String.format(
                            "Consultation: %.2f | Medicine: %.2f | Lab: %.2f | Room: %.2f | Other: %.2f",
                            consultation, medicine, lab, room, other));
                    stmt.executeUpdate();

                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedBillId = keys.getInt(1);
                        }
                    }

                    // Also record itemized rows in bill_items table
                    if (generatedBillId > 0) {
                        insertBillItem(conn, generatedBillId, "Doctor Consultation Fee", "CONSULTATION", consultation);
                        if (medicine > 0) insertBillItem(conn, generatedBillId, "Pharmacy Medicines", "MEDICINE", medicine);
                        if (lab > 0) insertBillItem(conn, generatedBillId, "Laboratory Investigations", "LAB_TEST", lab);
                        if (room > 0) insertBillItem(conn, generatedBillId, "Room / Ward Charges", "ROOM_CHARGE", room);
                        if (other > 0) insertBillItem(conn, generatedBillId, "Procedures & Consumables", "OTHER", other);
                    }
                }

                JOptionPane.showMessageDialog(dialog,
                        String.format("✅  Invoice Generated Successfully!\n\nInvoice No: %s\nTotal Amount: ₹ %.2f\nAmount Paid: ₹ %.2f\nStatus: %s",
                                billNum, grandTotal, paidAmount, status),
                        "Bill Generated", JOptionPane.INFORMATION_MESSAGE);

                dialog.dispose();
                loadData();
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

    private void insertBillItem(Connection conn, int billId, String desc, String type, double amount) {
        if (amount <= 0) return;
        String sql = "INSERT INTO bill_items (bill_id, item_description, item_type, unit_price, quantity, total_price) " +
                     "VALUES (?, ?, ?, ?, 1, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, billId);
            stmt.setString(2, desc);
            stmt.setString(3, type);
            stmt.setDouble(4, amount);
            stmt.setDouble(5, amount);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private void markBillPaid() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table first.",
                    "No Invoice Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BillRow bill = tableModel.getAt(row);
        if ("PAID".equals(bill.status())) {
            JOptionPane.showMessageDialog(this, "This invoice is already fully PAID.",
                    "Already Paid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        double balance = bill.totalAmount() - bill.paidAmount();
        String input = JOptionPane.showInputDialog(this,
                String.format("Enter payment amount for %s (%s)\nTotal: ₹ %.2f | Paid So Far: ₹ %.2f | Balance Due: ₹ %.2f",
                        bill.billNumber(), bill.patientName(), bill.totalAmount(), bill.paidAmount(), balance),
                String.format("%.2f", balance));

        if (input == null || input.trim().isEmpty()) return;

        try {
            double additionalPaid = Double.parseDouble(input.trim());
            if (additionalPaid <= 0) return;

            double newPaid = bill.paidAmount() + additionalPaid;
            String newStatus = (newPaid >= bill.totalAmount()) ? "PAID" : "PARTIALLY_PAID";

            String sql = "UPDATE bills SET paid_amount = ?, payment_status = ?, paid_at = NOW() WHERE bill_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setDouble(1, newPaid);
                stmt.setString(2, newStatus);
                stmt.setInt(3, bill.billId());
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this,
                    String.format("✅  Payment recorded!\nReceived: ₹ %.2f | Total Paid: ₹ %.2f | Status: %s",
                            additionalPaid, newPaid, newStatus),
                    "Payment Updated", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid numeric amount.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating payment: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void printSelectedInvoice() {
        int row = billTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the table to print or view.",
                    "No Invoice Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        BillRow bill = tableModel.getAt(row);

        JDialog invoiceDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Tax Invoice Receipt — " + bill.billNumber(), Dialog.ModalityType.APPLICATION_MODAL);
        invoiceDialog.setSize(560, 680);
        invoiceDialog.setLocationRelativeTo(this);
        invoiceDialog.setLayout(new BorderLayout());

        JEditorPane receiptPane = new JEditorPane();
        receiptPane.setContentType("text/html");
        receiptPane.setEditable(false);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a");
        String formattedDate = bill.billDate() != null ? bill.billDate().format(fmt) : "";

        double balance = Math.max(0, bill.totalAmount() - bill.paidAmount());

        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family: Arial, sans-serif; margin: 20px; color: #333;'>");
        html.append("<div style='text-align: center; border-bottom: 2px solid #20B2AA; padding-bottom: 10px;'>");
        html.append("<h2 style='color: #182B49; margin: 0;'>SMARTCARE MULTI-SPECIALTY HOSPITAL</h2>");
        html.append("<p style='font-size: 11px; color: #666; margin: 3px;'>123 Healthcare Boulevard, Medical Enclave | Tel: +91 (022) 8899-7700</p>");
        html.append("<h3 style='color: #20B2AA; margin-top: 8px;'>PATIENT INVOICE & RECEIPT</h3>");
        html.append("</div>");

        html.append("<table style='width: 100%; font-size: 12px; margin-top: 15px;'>");
        html.append("<tr><td><b>Invoice No:</b> ").append(bill.billNumber()).append("</td><td style='text-align: right;'><b>Date:</b> ").append(formattedDate).append("</td></tr>");
        html.append("<tr><td><b>Patient Name:</b> ").append(bill.patientName()).append("</td><td style='text-align: right;'><b>Payment Mode:</b> ").append(bill.paymentMode()).append("</td></tr>");
        html.append("<tr><td><b>Status:</b> <span style='font-weight: bold; color: ").append(bill.status().equals("PAID") ? "#198754" : "#DC3545").append(";'>").append(bill.status()).append("</span></td><td></td></tr>");
        html.append("</table>");

        html.append("<table style='width: 100%; border-collapse: collapse; margin-top: 20px; font-size: 12px;'>");
        html.append("<tr style='background-color: #f2f2f2;'><th style='border: 1px solid #ddd; padding: 8px; text-align: left;'>Description</th><th style='border: 1px solid #ddd; padding: 8px; text-align: right;'>Amount (₹)</th></tr>");

        if (bill.notes() != null && !bill.notes().isEmpty()) {
            String[] parts = bill.notes().split(" \\| ");
            for (String p : parts) {
                String[] item = p.split(": ");
                if (item.length == 2 && parseDouble(item[1]) > 0) {
                    html.append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>").append(item[0]).append("</td>");
                    html.append("<td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>₹ ").append(item[1]).append("</td></tr>");
                }
            }
        } else {
            html.append("<tr><td style='border: 1px solid #ddd; padding: 8px;'>Medical & Consultation Services</td><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>₹ ").append(String.format("%.2f", bill.subtotal())).append("</td></tr>");
        }

        html.append("<tr><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'><b>Subtotal:</b></td><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>₹ ").append(String.format("%.2f", bill.subtotal())).append("</td></tr>");
        if (bill.discount() > 0) {
            html.append("<tr><td style='border: 1px solid #ddd; padding: 8px; text-align: right; color: green;'><b>Discount:</b></td><td style='border: 1px solid #ddd; padding: 8px; text-align: right; color: green;'>- ₹ ").append(String.format("%.2f", bill.discount())).append("</td></tr>");
        }
        if (bill.tax() > 0) {
            html.append("<tr><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'><b>Tax / GST:</b></td><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'>+ ₹ ").append(String.format("%.2f", bill.tax())).append("</td></tr>");
        }
        html.append("<tr style='background-color: #E8F8F5;'><td style='border: 1px solid #ddd; padding: 10px; text-align: right; font-size: 14px;'><b>Grand Total:</b></td><td style='border: 1px solid #ddd; padding: 10px; text-align: right; font-size: 14px; color: #20B2AA;'><b>₹ ").append(String.format("%.2f", bill.totalAmount())).append("</b></td></tr>");
        html.append("<tr><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'><b>Amount Paid:</b></td><td style='border: 1px solid #ddd; padding: 8px; text-align: right; color: #198754;'><b>₹ ").append(String.format("%.2f", bill.paidAmount())).append("</b></td></tr>");
        html.append("<tr><td style='border: 1px solid #ddd; padding: 8px; text-align: right;'><b>Balance Due:</b></td><td style='border: 1px solid #ddd; padding: 8px; text-align: right; color: #DC3545;'><b>₹ ").append(String.format("%.2f", balance)).append("</b></td></tr>");
        html.append("</table>");

        html.append("<div style='margin-top: 30px; text-align: center; font-size: 11px; color: #888;'>");
        html.append("<p>Thank you for choosing SmartCare Hospital. Wishing you good health!</p>");
        html.append("<p><i>This is a computer-generated invoice and requires no physical signature.</i></p>");
        html.append("</div>");
        html.append("</body></html>");

        receiptPane.setText(html.toString());

        JPanel bottomBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBtnBar.setBackground(PANEL_WHITE);

        JButton printBtn = new JButton("🖨️ Print Receipt");
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        printBtn.setBackground(ACCENT_TEAL);
        printBtn.setForeground(Color.WHITE);
        printBtn.addActionListener(e -> {
            try {
                boolean done = receiptPane.print();
                if (done) {
                    JOptionPane.showMessageDialog(invoiceDialog, "Printing completed successfully.");
                }
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(invoiceDialog, "Printing failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> invoiceDialog.dispose());

        bottomBtnBar.add(printBtn);
        bottomBtnBar.add(closeBtn);

        invoiceDialog.add(new JScrollPane(receiptPane), BorderLayout.CENTER);
        invoiceDialog.add(bottomBtnBar, BorderLayout.SOUTH);
        invoiceDialog.setVisible(true);
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private double parseDouble(String s) {
        if (s == null) return 0.0;
        try { return Double.parseDouble(s.trim()); } catch (NumberFormatException e) { return 0.0; }
    }

    private JTextField createNumField(String defaultVal) {
        JTextField f = new JTextField(defaultVal, 12);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
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

    private JPanel createSummaryCard(String title, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(12, 14, 12, 14)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(108, 117, 125));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valLbl.setForeground(new Color(33, 37, 41));

        card.putClientProperty("valLabel", valLbl);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLbl, BorderLayout.CENTER);
        return card;
    }

    // ── TABLE MODEL ───────────────────────────────────────────────────────────

    record BillRow(int billId, String billNumber, String patientName,
                   double subtotal, double tax, double discount,
                   double totalAmount, double paidAmount, String status,
                   String paymentMode, LocalDateTime billDate, String notes) {}

    private static class BillTableModel extends AbstractTableModel {
        private static final String[] COLS = {"Bill #", "Patient", "Total (₹)", "Paid (₹)", "Balance (₹)", "Status", "Mode", "Date"};
        private List<BillRow> data = new ArrayList<>();

        void setData(List<BillRow> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        BillRow getAt(int row) { return data.get(row); }
        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            BillRow r = data.get(row);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");
            return switch (col) {
                case 0 -> r.billNumber();
                case 1 -> r.patientName();
                case 2 -> String.format("₹ %.2f", r.totalAmount());
                case 3 -> String.format("₹ %.2f", r.paidAmount());
                case 4 -> String.format("₹ %.2f", Math.max(0, r.totalAmount() - r.paidAmount()));
                case 5 -> r.status() != null ? r.status() : "-";
                case 6 -> r.paymentMode() != null ? r.paymentMode() : "-";
                case 7 -> r.billDate() != null ? r.billDate().format(fmt) : "-";
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int c) {
            return String.class;
        }
    }
}
