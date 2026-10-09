package com.smartcare.ui;
 
import com.smartcare.controller.AppointmentController;
import com.smartcare.controller.DoctorController;
import com.smartcare.enums.AppointmentStatus;
import com.smartcare.model.Appointment;
import com.smartcare.model.Department;
import com.smartcare.model.Doctor;
import com.smartcare.ui.dialog.DoctorDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        JLabel titleLbl = new JLabel("✚  Doctor & Specialist Management");
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
        JButton checkupBtn = createActionButton("📋 Patients for Checkup", new Color(13, 148, 136), e -> viewDoctorPatients());
        JButton editBtn = createActionButton("✏ Edit Profile", new Color(108, 117, 125), e -> openEditDoctorDialog());
        JButton viewBtn = createActionButton("👁 View Details", new Color(13, 110, 253), e -> viewDoctorCard());
        JButton deactivateBtn = createActionButton("🚫 Deactivate", new Color(220, 53, 69), e -> deactivateDoctor());

        actionPanel.add(addBtn);
        actionPanel.add(checkupBtn);
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
        doctorTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        doctorTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        doctorTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        doctorTable.getColumnModel().getColumn(9).setCellRenderer(centerRenderer);

        // Patients for Checkup badge renderer (Column 4)
        doctorTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                String text = val != null ? val.toString() : "0 Patients";
                if (!sel) {
                    if (text.contains("Queued") || text.contains("Waiting")) {
                        setForeground(new Color(13, 148, 136));
                        setBackground(new Color(240, 253, 250));
                    } else {
                        setForeground(new Color(100, 116, 139));
                        setBackground(Color.WHITE);
                    }
                }
                return this;
            }
        });

        doctorTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewDoctorPatients();
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
            private Map<Integer, Long> checkupCounts = new HashMap<>();

            @Override
            protected List<Doctor> doInBackground() {
                List<Doctor> doctors = doctorController.getAllDoctors();
                try {
                    List<Appointment> allAppts = new AppointmentController().getAllAppointments();
                    checkupCounts = allAppts.stream()
                            .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                            .collect(Collectors.groupingBy(Appointment::getDoctorId, Collectors.counting()));
                } catch (Exception ignored) {}
                return doctors;
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> doctors = get();
                    tableModel.setDoctors(doctors, checkupCounts);
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
            private Map<Integer, Long> checkupCounts = new HashMap<>();

            @Override
            protected List<Doctor> doInBackground() {
                List<Doctor> doctors = doctorController.searchDoctors(keyword);
                try {
                    List<Appointment> allAppts = new AppointmentController().getAllAppointments();
                    checkupCounts = allAppts.stream()
                            .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
                            .collect(Collectors.groupingBy(Appointment::getDoctorId, Collectors.counting()));
                } catch (Exception ignored) {}
                return doctors;
            }

            @Override
            protected void done() {
                try {
                    List<Doctor> results = get();
                    tableModel.setDoctors(results, checkupCounts);
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
            if (parentFrame instanceof DashboardFrame df) {
                df.loadDashboardMetrics();
            }
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
            if (parentFrame instanceof DashboardFrame df) {
                df.loadDashboardMetrics();
            }
        }
    }

    public void viewDoctorPatients() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to view their registered patients for checkup.",
                    "Select Doctor", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor doctor = tableModel.getDoctorAt(selectedRow);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Checkup Patients — Dr. " + doctor.getFullName(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(900, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        // Top info card
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(15, 23, 42)); // Slate Navy
        top.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("👨‍⚕️  " + doctor.getFullName() + "  —  Patient Checkup Queue");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel(doctor.getSpecialization() + "  •  Department: "
                + (doctor.getDepartmentName() != null ? doctor.getDepartmentName() : "General Medicine")
                + "  •  Fee: ₹" + doctor.getConsultationFee()
                + "  •  Duty: " + doctor.getAvailableDays());
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(148, 163, 184));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(sub);
        top.add(titleBox, BorderLayout.CENTER);

        // Fetch appointments for this doctor
        AppointmentController apptCtrl = new AppointmentController();
        List<Appointment> list = apptCtrl.getAppointmentsByDoctor(doctor.getDoctorId());

        JLabel countBadgeLbl = new JLabel(" " + list.size() + " Patient(s) Booked ");
        countBadgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        countBadgeLbl.setForeground(new Color(45, 212, 191));
        countBadgeLbl.setBackground(new Color(15, 118, 110, 80));
        countBadgeLbl.setOpaque(true);
        countBadgeLbl.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        top.add(countBadgeLbl, BorderLayout.EAST);

        // Table
        String[] cols = {"#", "Ticket / Token #", "Patient Name", "UHID Code", "Phone", "Date", "Time", "Reason / Symptoms", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter ttf = DateTimeFormatter.ofPattern("hh:mm a");

        for (Appointment a : list) {
            model.addRow(new Object[]{
                    a.getAppointmentId(),
                    a.getTokenNumber(),
                    a.getPatientName() != null ? a.getPatientName() : "Patient #" + a.getPatientId(),
                    a.getPatientCode() != null ? a.getPatientCode() : "PAT-" + a.getPatientId(),
                    a.getPatientPhone() != null && !a.getPatientPhone().isEmpty() ? a.getPatientPhone() : "On file",
                    a.getAppointmentDate() != null ? a.getAppointmentDate().format(dtf) : "-",
                    a.getAppointmentTime() != null ? a.getAppointmentTime().format(ttf) : "-",
                    a.getReasonForVisit() != null ? a.getReasonForVisit() : "-",
                    a.getStatus() != null ? a.getStatus().name() : "SCHEDULED"
            });
        }

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(241, 245, 249));

        // Center renderers
        DefaultTableCellRenderer cr = new DefaultTableCellRenderer();
        cr.setHorizontalAlignment(JLabel.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(cr);
        table.getColumnModel().getColumn(3).setCellRenderer(cr);
        table.getColumnModel().getColumn(5).setCellRenderer(cr);
        table.getColumnModel().getColumn(6).setCellRenderer(cr);
        table.getColumnModel().getColumn(8).setCellRenderer(cr);

        // Ticket column styling
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Consolas", Font.BOLD, 12));
                if (!sel) setForeground(new Color(13, 148, 136));
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottom.setBackground(new Color(248, 249, 250));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton completeBtn = new JButton("✓ Mark Checkup Complete");
        completeBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        completeBtn.setBackground(new Color(25, 135, 84));
        completeBtn.setForeground(Color.WHITE);
        completeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a patient appointment first.", "Select Row", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int apptId = (int) model.getValueAt(row, 0);
            apptCtrl.updateStatus(apptId, AppointmentStatus.COMPLETED);
            model.setValueAt("COMPLETED", row, 8);
            loadDoctorData();
            JOptionPane.showMessageDialog(dialog, "Checkup marked as COMPLETED!", "Updated", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton viewTicketBtn = new JButton("🎫 View Full Ticket Slip");
        viewTicketBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewTicketBtn.setBackground(new Color(13, 148, 136));
        viewTicketBtn.setForeground(Color.WHITE);
        viewTicketBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a patient appointment first.", "Select Row", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int apptId = (int) model.getValueAt(row, 0);
            Appointment appt = list.stream().filter(a -> a.getAppointmentId() == apptId).findFirst().orElse(null);
            if (appt != null) {
                new AppointmentPanel(parentFrame instanceof DashboardFrame df ? df : null).showTicketDetailsDialog(appt);
            }
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dialog.dispose());

        bottom.add(completeBtn);
        bottom.add(viewTicketBtn);
        bottom.add(closeBtn);

        dialog.add(top, BorderLayout.NORTH);
        if (list.isEmpty()) {
            JPanel emptyPanel = new JPanel(new GridBagLayout());
            emptyPanel.setBackground(Color.WHITE);
            JLabel emptyLbl = new JLabel("<html><center><div style='font-size:24px;'>🩺</div><br><b>No registered patients for checkup with Dr. "
                    + doctor.getFullName() + " right now.</b><br><br><span style='color:#64748b;'>Patients booked via the Patient Booking Portal or Reception will appear here automatically.</span></center></html>");
            emptyLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            emptyPanel.add(emptyLbl);
            dialog.add(emptyPanel, BorderLayout.CENTER);
        } else {
            dialog.add(scroll, BorderLayout.CENTER);
        }
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewDoctorCard() {
        int selectedRow = doctorTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a doctor to view.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Doctor d = tableModel.getDoctorAt(selectedRow);

        // Fetch patients waiting for checkup
        List<Appointment> appts = new AppointmentController().getAppointmentsByDoctor(d.getDoctorId());
        StringBuilder patientsSection = new StringBuilder();
        if (appts.isEmpty()) {
            patientsSection.append("   (No patients currently waiting for checkup)\n");
        } else {
            for (Appointment a : appts) {
                patientsSection.append(String.format("   • %-20s | Ticket: %-14s | %s %s | Status: %s\n",
                        a.getPatientName() != null ? a.getPatientName() : "Patient #" + a.getPatientId(),
                        a.getTokenNumber(),
                        a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "",
                        a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "",
                        a.getStatus() != null ? a.getStatus().name() : "SCHEDULED"));
            }
        }

        String message = String.format(
                "═══════════════════════════════════════════════════════════════════\n" +
                "                    DOCTOR PROFILE & CHECKUP CARD                 \n" +
                "═══════════════════════════════════════════════════════════════════\n" +
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
                "═══════════════════════════════════════════════════════════════════\n" +
                "REGISTERED PATIENTS CURRENTLY IN CHECKUP QUEUE (%d):\n" +
                "%s" +
                "═══════════════════════════════════════════════════════════════════",
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
                d.isAvailable() ? "AVAILABLE / ON DUTY" : "UNAVAILABLE",
                appts.size(),
                patientsSection.toString()
        );

        JTextArea area = new JTextArea(message);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
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
        private final String[] columns = {"ID", "Doctor Name", "Specialization", "Department", "Patients for Checkup", "Fee (₹)", "Experience", "Duty Days", "Phone", "Status"};
        private List<Doctor> doctors = new ArrayList<>();
        private Map<Integer, Long> checkupCounts = new HashMap<>();

        public void setDoctors(List<Doctor> doctors, Map<Integer, Long> checkupCounts) {
            this.doctors = doctors != null ? doctors : new ArrayList<>();
            this.checkupCounts = checkupCounts != null ? checkupCounts : new HashMap<>();
            fireTableDataChanged();
        }

        public void setDoctors(List<Doctor> doctors) {
            setDoctors(doctors, new HashMap<>());
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
                case 4 -> {
                    long count = checkupCounts.getOrDefault(d.getDoctorId(), 0L);
                    yield count > 0 ? "👥 " + count + " Queued" : "0 Patients";
                }
                case 5 -> "₹ " + d.getConsultationFee();
                case 6 -> d.getExperienceYears() + " yrs";
                case 7 -> d.getAvailableDays();
                case 8 -> d.getPhone();
                case 9 -> d.isAvailable() ? "Active" : "Inactive";
                default -> "";
            };
        }
    }
}
