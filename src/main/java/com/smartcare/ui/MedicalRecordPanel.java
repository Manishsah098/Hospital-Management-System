package com.smartcare.ui;

import com.smartcare.dao.impl.DoctorDAOImpl;
import com.smartcare.dao.impl.MedicalRecordDAOImpl;
import com.smartcare.dao.impl.PatientDAOImpl;
import com.smartcare.model.Doctor;
import com.smartcare.model.MedicalRecord;
import com.smartcare.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Medical Records Panel — SmartCare Hospital System.
 * View, add, and manage patient medical records / clinical notes.
 */
public class MedicalRecordPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final MedicalRecordDAOImpl recordDAO;
    private final PatientDAOImpl patientDAO;
    private final DoctorDAOImpl doctorDAO;

    private MedicalRecordTableModel tableModel;
    private JTable recordTable;
    private TableRowSorter<MedicalRecordTableModel> sorter;
    private JTextField searchField;
    private JLabel statusLabel;

    private static final Color ACCENT_PURPLE = new Color(111, 66, 193);
    private static final Color BG_LIGHT      = new Color(245, 247, 250);
    private static final Color PANEL_WHITE   = Color.WHITE;

    public MedicalRecordPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.recordDAO  = new MedicalRecordDAOImpl();
        this.patientDAO = new PatientDAOImpl();
        this.doctorDAO  = new DoctorDAOImpl();
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

        JLabel title = new JLabel("📋  Medical Records");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(33, 37, 41));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search patient / diagnosis...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton btnAdd     = createButton("+ Add Record", ACCENT_PURPLE);
        JButton btnView    = createButton("👁 View Details", new Color(13, 110, 253));
        JButton btnRefresh = createButton("↻ Refresh", new Color(108, 117, 125));

        btnAdd.addActionListener(e -> openAddDialog());
        btnView.addActionListener(e -> viewRecord());
        btnRefresh.addActionListener(e -> loadData());

        actionPanel.add(searchField);
        actionPanel.add(btnAdd);
        actionPanel.add(btnView);
        actionPanel.add(btnRefresh);

        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ── TABLE ─────────────────────────────────────────────────────────────
        tableModel  = new MedicalRecordTableModel();
        recordTable = new JTable(tableModel);
        recordTable.setRowHeight(30);
        recordTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        recordTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        recordTable.getTableHeader().setBackground(new Color(248, 249, 250));
        recordTable.setSelectionBackground(new Color(230, 218, 255));
        recordTable.setGridColor(new Color(233, 236, 239));
        recordTable.setShowHorizontalLines(true);
        recordTable.setShowVerticalLines(false);
        recordTable.setFillsViewportHeight(true);

        int[] widths = {50, 150, 150, 220, 130, 180};
        for (int i = 0; i < widths.length; i++)
            recordTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        sorter = new TableRowSorter<>(tableModel);
        recordTable.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(recordTable);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PANEL_WHITE);

        // ── BOTTOM BAR ────────────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(PANEL_WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        statusLabel = new JLabel("Loading records...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));

        JButton btnDelete = createButton("🗑 Delete Record", new Color(220, 53, 69));
        btnDelete.addActionListener(e -> deleteRecord());

        bottomBar.add(statusLabel, BorderLayout.WEST);
        bottomBar.add(btnDelete, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // ── DATA OPERATIONS ───────────────────────────────────────────────────────

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<MedicalRecord> records = recordDAO.findAll();
                tableModel.setData(records);
                statusLabel.setText("Total: " + records.size() + " medical record(s) loaded.");
            } catch (Exception ex) {
                statusLabel.setText("Error: " + ex.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to load records:\n" + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + text, 1, 2, 3));
    }

    private void openAddDialog() {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Add Medical Record", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(580, 520);
        dialog.setLocationRelativeTo(this);

        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setBackground(new Color(24, 43, 73));
        JLabel headerTitle = new JLabel("📋  New Medical Record");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerTitle.setForeground(Color.WHITE);
        header.add(headerTitle);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.anchor = GridBagConstraints.WEST;

        List<Patient> patients = patientDAO.findAll();
        JComboBox<String> patientCombo = new JComboBox<>();
        patientCombo.addItem("-- Select Patient --");
        for (Patient p : patients)
            patientCombo.addItem(p.getPatientId() + " | " + p.getFullName()
                    + " [" + p.getPatientCode() + "]");

        List<Doctor> doctors = doctorDAO.findAll();
        JComboBox<String> doctorCombo = new JComboBox<>();
        doctorCombo.addItem("-- Select Doctor --");
        for (Doctor d : doctors)
            doctorCombo.addItem(d.getDoctorId() + " | Dr. " + d.getFullName()
                    + " (" + d.getSpecialization() + ")");

        JTextField diagnosisField = createTextField("Primary diagnosis...");
        JTextField vitalsField    = createTextField("e.g. BP: 120/80, Pulse: 72 bpm, Temp: 98.6°F");
        JTextArea  symptomsArea   = createTextArea();
        JTextArea  treatmentArea  = createTextArea();
        JTextArea  notesArea      = createTextArea();

        patientCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        doctorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        addFormRow(form, gbc, 0, "Patient *:", patientCombo);
        addFormRow(form, gbc, 1, "Doctor *:", doctorCombo);
        addFormRow(form, gbc, 2, "Diagnosis *:", diagnosisField);
        addFormRow(form, gbc, 3, "Vital Signs:", vitalsField);
        addFormRow(form, gbc, 4, "Symptoms:", new JScrollPane(symptomsArea));
        addFormRow(form, gbc, 5, "Treatment Plan:", new JScrollPane(treatmentArea));
        addFormRow(form, gbc, 6, "Doctor Notes:", new JScrollPane(notesArea));

        JLabel errLabel = new JLabel(" ");
        errLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errLabel.setForeground(Color.RED);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        form.add(errLabel, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btnPanel.setBackground(new Color(248, 249, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("💾  Save Record");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(ACCENT_PURPLE);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.addActionListener(e -> {
            try {
                if (patientCombo.getSelectedIndex() == 0) { errLabel.setText("Please select a patient."); return; }
                if (doctorCombo.getSelectedIndex() == 0)  { errLabel.setText("Please select a doctor.");  return; }
                if (diagnosisField.getText().trim().isEmpty()) { errLabel.setText("Diagnosis is required."); return; }

                int patId = Integer.parseInt(patientCombo.getSelectedItem().toString().split(" \\| ")[0].trim());
                int docId = Integer.parseInt(doctorCombo.getSelectedItem().toString().split(" \\| ")[0].trim());

                MedicalRecord rec = new MedicalRecord();
                rec.setPatientId(patId);
                rec.setDoctorId(docId);
                rec.setDiagnosis(diagnosisField.getText().trim());
                rec.setVitalSigns(vitalsField.getText().trim());
                rec.setSymptoms(symptomsArea.getText().trim());
                rec.setTreatmentPlan(treatmentArea.getText().trim());
                rec.setDoctorNotes(notesArea.getText().trim());

                boolean ok = recordDAO.create(rec);
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "✅  Medical record saved successfully!",
                            "Record Saved", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadData();
                } else {
                    errLabel.setText("Failed to save. Please try again.");
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

    private void viewRecord() {
        int row = recordTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a record to view details.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MedicalRecord rec = tableModel.getAt(recordTable.convertRowIndexToModel(row));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

        String info = String.format(
            "<html><body style='font-family:Segoe UI;width:420px;padding:5px;'>" +
            "<h3 style='color:#6f42c1;margin-bottom:10px;'>📋 Medical Record #%d</h3>" +
            "<table cellpadding='4' style='width:100%%;'>" +
            "<tr><td><b>Patient:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Doctor:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Date:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Vital Signs:</b></td><td>%s</td></tr>" +
            "<tr><td><b>Diagnosis:</b></td><td>%s</td></tr>" +
            "<tr><td valign='top'><b>Symptoms:</b></td><td>%s</td></tr>" +
            "<tr><td valign='top'><b>Treatment Plan:</b></td><td>%s</td></tr>" +
            "<tr><td valign='top'><b>Doctor Notes:</b></td><td>%s</td></tr>" +
            "</table></body></html>",
            rec.getRecordId(),
            rec.getPatientName() != null ? rec.getPatientName() : "Patient #" + rec.getPatientId(),
            rec.getDoctorName()  != null ? "Dr. " + rec.getDoctorName()  : "Doctor #"  + rec.getDoctorId(),
            rec.getRecordedAt()  != null ? rec.getRecordedAt().format(fmt) : "-",
            rec.getVitalSigns()  != null ? rec.getVitalSigns()  : "-",
            rec.getDiagnosis()   != null ? rec.getDiagnosis()   : "-",
            rec.getSymptoms()    != null ? rec.getSymptoms()    : "-",
            rec.getTreatmentPlan() != null ? rec.getTreatmentPlan() : "-",
            rec.getDoctorNotes() != null ? rec.getDoctorNotes() : "-"
        );
        JOptionPane.showMessageDialog(this, new JLabel(info), "Medical Record Details", JOptionPane.PLAIN_MESSAGE);
    }

    private void deleteRecord() {
        int row = recordTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a record to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        MedicalRecord rec = tableModel.getAt(recordTable.convertRowIndexToModel(row));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete medical record #" + rec.getRecordId() + " for " + rec.getPatientName() + "?\nThis is permanent.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            recordDAO.delete(rec.getRecordId());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Delete Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private JTextField createTextField(String placeholder) {
        JTextField f = new JTextField(24);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private JTextArea createTextArea() {
        JTextArea a = new JTextArea(3, 24);
        a.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218)));
        return a;
    }

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
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

    private static class MedicalRecordTableModel extends AbstractTableModel {
        private static final String[] COLS = {"#", "Patient", "Doctor", "Diagnosis", "Vital Signs", "Recorded On"};
        private List<MedicalRecord> data = new ArrayList<>();

        void setData(List<MedicalRecord> data) {
            this.data = data != null ? data : new ArrayList<>();
            fireTableDataChanged();
        }

        MedicalRecord getAt(int row) { return data.get(row); }
        @Override public int getRowCount()    { return data.size(); }
        @Override public int getColumnCount() { return COLS.length; }
        @Override public String getColumnName(int c) { return COLS[c]; }

        @Override
        public Object getValueAt(int row, int col) {
            MedicalRecord r = data.get(row);
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            return switch (col) {
                case 0 -> r.getRecordId();
                case 1 -> r.getPatientName() != null ? r.getPatientName() : "P#" + r.getPatientId();
                case 2 -> r.getDoctorName()  != null ? "Dr. " + r.getDoctorName()  : "D#" + r.getDoctorId();
                case 3 -> r.getDiagnosis()   != null ? r.getDiagnosis()   : "-";
                case 4 -> r.getVitalSigns()  != null ? r.getVitalSigns()  : "-";
                case 5 -> r.getRecordedAt()  != null ? r.getRecordedAt().format(fmt) : "-";
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int c) {
            return c == 0 ? Integer.class : String.class;
        }
    }
}
