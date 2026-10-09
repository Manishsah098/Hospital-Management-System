package com.smartcare.ui;

import com.smartcare.controller.AppointmentController;
import com.smartcare.controller.PatientController;
import com.smartcare.model.Appointment;
import com.smartcare.model.Patient;
import com.smartcare.ui.dialog.PatientDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Java Swing Panel for Patient Management.
 */
public class PatientPanel extends JPanel {
    private final PatientController patientController;
    private final Frame parentFrame;

    private JTable patientTable;
    private PatientTableModel tableModel;
    private JTextField searchField;
    private JLabel countBadge;

    public PatientPanel(Frame parentFrame) {
        this.parentFrame = parentFrame;
        this.patientController = new PatientController();
        initComponents();
        loadPatientData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(0, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 25, 20, 25));

        // 1. Top Bar (Title, Count Badge, Action Buttons)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel("✚  Patient Management");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLbl.setForeground(new Color(33, 37, 41));

        countBadge = new JLabel(" 0 Patients ");
        countBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countBadge.setForeground(new Color(13, 110, 253));
        countBadge.setBackground(new Color(207, 226, 255));
        countBadge.setOpaque(true);
        countBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        titlePanel.add(titleLbl);
        titlePanel.add(countBadge);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton addBtn = createActionButton("➕ Register Patient", new Color(13, 110, 253), e -> openAddPatientDialog());
        JButton checkupBtn = createActionButton("🩺 Attending Doctor & Ticket", new Color(13, 148, 136), e -> viewPatientDoctorAndCheckup());
        JButton editBtn = createActionButton("✏ Edit Details", new Color(108, 117, 125), e -> openEditPatientDialog());
        JButton viewBtn = createActionButton("👁 View Card", new Color(25, 135, 84), e -> viewPatientCard());
        JButton deleteBtn = createActionButton("🗑 Remove", new Color(220, 53, 69), e -> deleteSelectedPatient());

        actionPanel.add(addBtn);
        actionPanel.add(checkupBtn);
        actionPanel.add(editBtn);
        actionPanel.add(viewBtn);
        actionPanel.add(deleteBtn);

        topBar.add(titlePanel, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // 2. Center Panel with Search Filter Bar & Table
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        // Search Bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBar.setOpaque(false);

        JLabel searchIcon = new JLabel("🔍 Search:");
        searchIcon.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchField = new JTextField(25);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search by name, phone, code or email...");
        searchField.addActionListener(e -> performSearch());

        JButton searchBtn = new JButton("Filter");
        searchBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchBtn.addActionListener(e -> performSearch());

        JButton refreshBtn = new JButton("Reset");
        refreshBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadPatientData();
        });

        searchBar.add(searchIcon);
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(refreshBtn);

        centerPanel.add(searchBar, BorderLayout.NORTH);

        // Table
        tableModel = new PatientTableModel();
        patientTable = new JTable(tableModel);
        patientTable.setRowHeight(32);
        patientTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        patientTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        patientTable.getTableHeader().setBackground(new Color(241, 243, 245));
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setShowVerticalLines(false);

        // Center align ID, Code, Gender, Blood group
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        patientTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        patientTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        patientTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        patientTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        patientTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewPatientCard();
                }
            }
        });

        JScrollPane tableScroll = new JScrollPane(patientTable);
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

    public void loadPatientData() {
        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() {
                return patientController.getAllPatients();
            }

            @Override
            protected void done() {
                try {
                    List<Patient> patients = get();
                    tableModel.setPatients(patients);
                    countBadge.setText(" " + patients.size() + " Patients ");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PatientPanel.this, "Failed to load patients: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPatientData();
            return;
        }

        SwingWorker<List<Patient>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Patient> doInBackground() {
                return patientController.searchPatients(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<Patient> results = get();
                    tableModel.setPatients(results);
                    countBadge.setText(" " + results.size() + " Found ");
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(PatientPanel.this, "Search error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void openAddPatientDialog() {
        PatientDialog dialog = new PatientDialog(parentFrame, patientController, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadPatientData();
            if (parentFrame instanceof DashboardFrame df) {
                df.loadDashboardMetrics();
            }
        }
    }

    private void openEditPatientDialog() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient selected = tableModel.getPatientAt(selectedRow);
        PatientDialog dialog = new PatientDialog(parentFrame, patientController, selected);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            loadPatientData();
            if (parentFrame instanceof DashboardFrame df) {
                df.loadDashboardMetrics();
            }
        }
    }

    public void viewPatientDoctorAndCheckup() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient to view their attending doctor and checkup details.",
                    "Select Patient", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient p = tableModel.getPatientAt(selectedRow);

        List<Appointment> appts = new AppointmentController().getAppointmentsByPatient(p.getPatientId());

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Attending Doctor & Checkups — " + p.getFullName(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(750, 480);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(15, 23, 42));
        top.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("🩺  " + p.getFullName() + " [" + p.getPatientCode() + "]");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Phone: " + p.getPhone() + "  •  Age: " + p.getAge() + " yrs  •  Gender: " + p.getGender() + "  •  Blood: " + (p.getBloodGroup() != null ? p.getBloodGroup() : "-"));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(148, 163, 184));

        JPanel titleBox = new JPanel(new GridLayout(2, 1, 0, 4));
        titleBox.setOpaque(false);
        titleBox.add(title);
        titleBox.add(sub);
        top.add(titleBox, BorderLayout.WEST);

        JLabel countBadgeLbl = new JLabel(appts.size() + " Checkup(s) Booked ");
        countBadgeLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        countBadgeLbl.setForeground(new Color(45, 212, 191));
        top.add(countBadgeLbl, BorderLayout.EAST);

        // Table
        String[] cols = {"Ticket / Token #", "Attending Doctor", "Specialization", "Date", "Time", "Reason", "Status"};
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
        DateTimeFormatter ttf = DateTimeFormatter.ofPattern("hh:mm a");

        for (Appointment a : appts) {
            model.addRow(new Object[]{
                    a.getTokenNumber(),
                    a.getDoctorName() != null ? a.getDoctorName() : "Doctor #" + a.getDoctorId(),
                    a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "General",
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

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
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

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        bottom.setBackground(new Color(248, 249, 250));
        bottom.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton viewTicketBtn = new JButton("🎫 View Full Ticket Slip");
        viewTicketBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        viewTicketBtn.setBackground(new Color(13, 148, 136));
        viewTicketBtn.setForeground(Color.WHITE);
        viewTicketBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(dialog, "Select an appointment from the table first.", "Select Row", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Appointment a = appts.get(row);
            new AppointmentPanel(parentFrame instanceof DashboardFrame df ? df : null).showTicketDetailsDialog(a);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> dialog.dispose());

        bottom.add(viewTicketBtn);
        bottom.add(closeBtn);

        dialog.add(top, BorderLayout.NORTH);
        if (appts.isEmpty()) {
            JPanel empty = new JPanel(new GridBagLayout());
            empty.setBackground(Color.WHITE);
            JLabel el = new JLabel("<html><center><div style='font-size:24px;'>📋</div><br><b>No scheduled checkup appointments for "
                    + p.getFullName() + ".</b><br><br><span style='color:#64748b;'>Use the Appointments tab to schedule a checkup with a specialist doctor.</span></center></html>");
            empty.add(el);
            dialog.add(empty, BorderLayout.CENTER);
        } else {
            dialog.add(scroll, BorderLayout.CENTER);
        }
        dialog.add(bottom, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewPatientCard() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient p = tableModel.getPatientAt(selectedRow);

        List<Appointment> appts = new AppointmentController().getAppointmentsByPatient(p.getPatientId());
        StringBuilder apptSb = new StringBuilder();
        if (appts.isEmpty()) {
            apptSb.append("   (No active checkup appointments scheduled)\n");
        } else {
            for (Appointment a : appts) {
                apptSb.append(String.format("   • Attending Doctor : %s (%s)\n     Ticket / Token   : %s\n     Scheduled Time   : %s %s\n     Reason / Status  : %s [%s]\n\n",
                        a.getDoctorName() != null ? a.getDoctorName() : "Doctor #" + a.getDoctorId(),
                        a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "General",
                        a.getTokenNumber(),
                        a.getAppointmentDate(),
                        a.getAppointmentTime(),
                        a.getReasonForVisit(),
                        a.getStatus()));
            }
        }

        String message = String.format(
                "═══════════════════════════════════════════════════════════════════\n" +
                "               PATIENT MEDICAL & ATTENDING DOCTOR CARD             \n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "UHID / Code       : %s\n" +
                "Full Name         : %s\n" +
                "Age / Gender      : %d Years (%s)\n" +
                "Date of Birth     : %s\n" +
                "Blood Group       : %s\n" +
                "Contact Phone     : %s\n" +
                "Email             : %s\n" +
                "Address           : %s\n" +
                "Emergency Contact : %s (Ph: %s)\n" +
                "Known Allergies   : %s\n" +
                "Registered Since  : %s\n" +
                "═══════════════════════════════════════════════════════════════════\n" +
                "ATTENDING DOCTOR(S) & CHECKUP APPOINTMENTS (%d):\n" +
                "%s" +
                "═══════════════════════════════════════════════════════════════════",
                p.getPatientCode(),
                p.getFullName(),
                p.getAge(),
                p.getGender(),
                p.getDateOfBirth(),
                p.getBloodGroup() != null ? p.getBloodGroup() : "N/A",
                p.getPhone(),
                p.getEmail() != null ? p.getEmail() : "N/A",
                p.getAddress() != null ? p.getAddress() : "N/A",
                p.getEmergencyContactName() != null ? p.getEmergencyContactName() : "N/A",
                p.getEmergencyContactPhone() != null ? p.getEmergencyContactPhone() : "N/A",
                p.getAllergies() != null && !p.getAllergies().isEmpty() ? p.getAllergies() : "None Recorded",
                p.getRegisteredAt() != null ? p.getRegisteredAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")) : "N/A",
                appts.size(),
                apptSb.toString()
        );

        JTextArea area = new JTextArea(message);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(248, 249, 250));

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Patient Profile: " + p.getFullName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSelectedPatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a patient to remove.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Patient selected = tableModel.getPatientAt(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete patient record: " + selected.getFullName() + " (" + selected.getPatientCode() + ")?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                patientController.deletePatient(selected.getPatientId());
                JOptionPane.showMessageDialog(this, "Patient record removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadPatientData();
                if (parentFrame instanceof DashboardFrame df) {
                    df.loadDashboardMetrics();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error deleting patient: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Custom Table Model
    private static class PatientTableModel extends AbstractTableModel {
        private final String[] columns = {"ID", "Patient Code", "Full Name", "Age / Gender", "Blood Group", "Phone Number", "Email", "Registered Date"};
        private List<Patient> patients = new ArrayList<>();

        public void setPatients(List<Patient> patients) {
            this.patients = patients != null ? patients : new ArrayList<>();
            fireTableDataChanged();
        }

        public Patient getPatientAt(int row) {
            return patients.get(row);
        }

        @Override
        public int getRowCount() {
            return patients.size();
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
            Patient p = patients.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getPatientId();
                case 1 -> p.getPatientCode();
                case 2 -> p.getFullName();
                case 3 -> p.getAge() + " yrs / " + p.getGender();
                case 4 -> p.getBloodGroup() != null ? p.getBloodGroup() : "-";
                case 5 -> p.getPhone();
                case 6 -> p.getEmail() != null ? p.getEmail() : "-";
                case 7 -> p.getRegisteredAt() != null ? p.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "-";
                default -> "";
            };
        }
    }
}
