package com.smartcare.ui;

import com.smartcare.controller.DoctorController;
import com.smartcare.model.Department;
import com.smartcare.model.Doctor;
import com.smartcare.ui.dialog.DoctorDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Java Swing Panel for Doctor & Medical Staff Management.
 */
public class DoctorPanel extends JPanel {
    private final DoctorController doctorController;
    private final Frame parentFrame;

    private JTable doctorTable;
    private DoctorTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> departmentFilterCombo;
    private JLabel countBadge;

    public DoctorPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        this.doctorController = new DoctorController();
        initComponents();
        loadDepartmentFilters();
        loadDoctorData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. Top Bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel("🩺 Doctor Management");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(33, 37, 41));

        countBadge = new JLabel(" 0 Doctors ");
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countBadge.setForeground(new Color(25, 135, 84));
        countBadge.setBackground(new Color(209, 231, 221));
        countBadge.setOpaque(true);
        countBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        titlePanel.add(titleLbl);
        titlePanel.add(countBadge);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton addBtn = createActionButton("➕ Add Doctor", new Color(25, 135, 84), e -> openAddDoctorDialog());
        JButton editBtn = createActionButton("✏ Edit Profile", new Color(108, 117, 125), e -> openEditDoctorDialog());
        JButton viewBtn = createActionButton("👁 View Details", new Color(13, 110, 253), e -> viewDoctorCard());
        JButton deactivateBtn = createActionButton("🚫 Deactivate", new Color(220, 53, 69), e -> deactivateDoctor());

        actionPanel.add(addBtn);
        actionPanel.add(editBtn);
        actionPanel.add(viewBtn);
        actionPanel.add(deactivateBtn);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. Center Content with Filters & Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Filters Toolbar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterBar.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍 Search:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchField = new JTextField(18);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Name, specialization, license...");
        searchField.addActionListener(e -> performSearch());

        JLabel deptLabel = new JLabel("Department:");
        deptLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        departmentFilterCombo = new JComboBox<>(new String[]{"All Departments"});
        departmentFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        departmentFilterCombo.addActionListener(e -> applyDepartmentFilter());

        JButton searchBtn = new JButton("Filter");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchBtn.addActionListener(e -> performSearch());

        JButton resetBtn = new JButton("Reset");
        resetBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            departmentFilterCombo.setSelectedIndex(0);
            loadDoctorData();
        });

        filterBar.add(searchIcon);
        filterBar.add(searchField);
        filterBar.add(deptLabel);
        filterBar.add(departmentFilterCombo);
        filterBar.add(searchBtn);
        filterBar.add(resetBtn);

        centerPanel.add(filterBar, BorderLayout.NORTH);

        // Table
        tableModel = new DoctorTableModel();
        doctorTable = new JTable(tableModel);
        doctorTable.setRowHeight(32);
        doctorTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        doctorTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        doctorTable.getTableHeader().setBackground(new Color(241, 243, 245));
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.setShowVerticalLines(false);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        doctorTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        doctorTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        doctorTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        doctorTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);

        doctorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewDoctorCard();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(doctorTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(230, 235, 240)));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, Color bgColor, java.awt.event.ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    private void loadDepartmentFilters() {
        try {
            List<Department> list = doctorController.getAllDepartments();
            for (Department d : list) {
                departmentFilterCombo.addItem(d.getName());
            }
        } catch (Exception ignored) {}
    }

    public void loadDoctorData() {
        SwingWorker<List<Doctor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Doctor> doInBackground() {
                return doctorController.getAllDoctors();
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> doctors = get();
                    tableModel.setDoctors(doctors);
                    countBadge.setText(" " + doctors.size() + " Doctors ");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DoctorPanel.this, "Failed to load doctors: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadDoctorData();
            return;
        }

        SwingWorker<List<Doctor>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Doctor> doInBackground() {
                return doctorController.searchDoctors(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> results = get();
                    tableModel.setDoctors(results);
                    countBadge.setText(" " + results.size() + " Found ");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DoctorPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void applyDepartmentFilter() {
        String selected = (String) departmentFilterCombo.getSelectedItem();
        if (selected == null || selected.equals("All Departments")) {
            loadDoctorData();
            return;
        }
        performSearch();
    }

    private void openAddDoctorDialog() {
        DoctorDialog dialog = new DoctorDialog(parentFrame, doctorController, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadDoctorData();
        }
    }

    private void openEditDoctorDialog() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor selected = tableModel.getDoctorAt(selectedRow);
        DoctorDialog dialog = new DoctorDialog(parentFrame, doctorController, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadDoctorData();
        }
    }

    private void viewDoctorCard() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to view.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor d = tableModel.getDoctorAt(selectedRow);

        String message = String.format(
                "═══════════════════════════════════════════════\n" +
                "               DOCTOR PROFILE CARD             \n" +
                "═══════════════════════════════════════════════\n" +
                "Doctor Name       : %s\n" +
                "Specialization    : %s\n" +
                "Department        : %s\n" +
                "License Number    : %s\n" +
                "Qualifications    : %s\n" +
                "Experience        : %d Years\n" +
                "Consultation Fee  : ₹ %,.2f\n" +
                "Duty Days         : %s\n" +
                "Contact Phone     : %s\n" +
                "Official Email    : %s\n" +
                "Status            : %s\n" +
                "═══════════════════════════════════════════════",
                d.getFullName(),
                d.getSpecialization(),
                d.getDepartmentName() != null ? d.getDepartmentName() : "General Medicine",
                d.getLicenseNumber(),
                d.getQualification() != null ? d.getQualification() : "MBBS",
                d.getExperienceYears(),
                d.getConsultationFee(),
                d.getAvailableDays(),
                d.getPhone(),
                d.getEmail(),
                d.isAvailable() ? "AVAILABLE / ON DUTY" : "UNAVAILABLE"
        );

        JTextArea area = new JTextArea(message);
        area.setFont(new Font("Consolas", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(new Color(248, 249, 250));

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Doctor Profile: " + d.getFullName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void deactivateDoctor() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to deactivate.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor selected = tableModel.getDoctorAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Set doctor " + selected.getFullName() + " status to Unavailable?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                doctorController.deleteDoctor(selected.getDoctorId());
                JOptionPane.showMessageDialog(this, "Doctor status updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadDoctorData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static class DoctorTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Doctor Name", "Specialization", "Department", "Fee (₹)", "Experience", "Duty Days", "Phone", "Status"};
        private List<Doctor> doctors = new ArrayList<>();

        public void setDoctors(List<Doctor> doctors) {
            this.doctors = doctors != null ? doctors : new ArrayList<>();
            fireTableDataChanged();
        }

        public Doctor getDoctorAt(int row) {
            return doctors.get(row);
        }

        @Override
        public int getRowCount() {
            return doctors.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Doctor d = doctors.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> d.getDoctorId();
                case 1 -> d.getFullName();
                case 2 -> d.getSpecialization();
                case 3 -> d.getDepartmentName() != null ? d.getDepartmentName() : "General";
                case 4 -> "₹ " + d.getConsultationFee();
                case 5 -> d.getExperienceYears() + " yrs";
                case 6 -> d.getAvailableDays();
                case 7 -> d.getPhone();
                case 8 -> d.isAvailable() ? "Active" : "Inactive";
                default -> "";
            };
        }
    }
}
