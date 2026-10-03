package com.smartcare.ui;

import com.smartcare.controller.AppointmentController;
import com.smartcare.enums.AppointmentStatus;
import com.smartcare.model.Appointment;
import com.smartcare.ui.dialog.AppointmentDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Appointment Management Panel — SmartCare Hospital System.
 * Schedule, reschedule, cancel, and track patient appointments.
 */
public class AppointmentPanel extends JPanel {

    private final DashboardFrame parentFrame;
    private final AppointmentController controller;

    private AppointmentTableModel tableModel;
    private JTable appointmentTable;
    private TableRowSorter<AppointmentTableModel> sorter;
    private JTextField searchField;
    private JLabel statusLabel;
    private JComboBox<String> statusFilterCombo;

    private static final Color ACCENT_BLUE  = new Color(13, 110, 253);
    private static final Color ACCENT_GREEN = new Color(25, 135, 84);
    private static final Color ACCENT_RED   = new Color(220, 53, 69);
    private static final Color ACCENT_WARN  = new Color(255, 193, 7);
    private static final Color BG_LIGHT     = new Color(245, 247, 250);
    private static final Color PANEL_WHITE  = Color.WHITE;

    public AppointmentPanel(DashboardFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.controller  = new AppointmentController();
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

        JLabel pageTitle = new JLabel("📅  Appointment Management");
        pageTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        pageTitle.setForeground(new Color(33, 37, 41));

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPanel.setOpaque(false);

        // Status filter
        statusFilterCombo = new JComboBox<>(new String[]{
                "All Statuses", "SCHEDULED", "COMPLETED", "CANCELLED", "NO_SHOW"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusFilterCombo.addActionListener(e -> applyFilter());

        // Search
        searchField = new JTextField(16);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.putClientProperty("JTextField.placeholderText", "Search patient/doctor...");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });

        JButton btnNew    = createButton("+ New Appointment", ACCENT_GREEN);
        JButton btnRefresh = createButton("↻ Refresh", new Color(108, 117, 125));
        JButton btnToday  = createButton("📅 Today", new Color(13, 202, 240));

        btnNew.addActionListener(e -> openNewAppointmentDialog());
        btnRefresh.addActionListener(e -> loadData());
        btnToday.addActionListener(e -> loadTodayOnly());

        actionPanel.add(new JLabel("Filter: "));
        actionPanel.add(statusFilterCombo);
        actionPanel.add(searchField);
        actionPanel.add(btnToday);
        actionPanel.add(btnNew);
        actionPanel.add(btnRefresh);

        topBar.add(pageTitle, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);

        // ── TABLE ─────────────────────────────────────────────────────────────
        tableModel = new AppointmentTableModel();
        appointmentTable = new JTable(tableModel);
        appointmentTable.setRowHeight(32);
        appointmentTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appointmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        appointmentTable.getTableHeader().setBackground(new Color(248, 249, 250));
        appointmentTable.setSelectionBackground(new Color(207, 226, 255));
        appointmentTable.setSelectionForeground(Color.BLACK);
        appointmentTable.setGridColor(new Color(233, 236, 239));
        appointmentTable.setShowHorizontalLines(true);
        appointmentTable.setShowVerticalLines(false);
        appointmentTable.setFillsViewportHeight(true);

        // Status column coloring
        appointmentTable.getColumnModel().getColumn(5).setCellRenderer(new StatusCellRenderer());

        // Column widths
        int[] colWidths = {50, 130, 145, 100, 90, 105, 160, 80};
        for (int i = 0; i < colWidths.length; i++) {
            appointmentTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        appointmentTable.setRowSorter(sorter);

        JScrollPane scrollPane = new JScrollPane(appointmentTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(PANEL_WHITE);

        // ── BOTTOM ACTION BAR ─────────────────────────────────────────────────
        JPanel bottomBar = new JPanel(new BorderLayout(10, 0));
        bottomBar.setBackground(PANEL_WHITE);
        bottomBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
                new EmptyBorder(10, 20, 10, 20)));

        statusLabel = new JLabel("Loading appointments...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(108, 117, 125));

        JPanel rowActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rowActions.setOpaque(false);

        JButton btnEdit   = createButton("✏ Edit", ACCENT_BLUE);
        JButton btnComplete = createButton("✓ Mark Complete", ACCENT_GREEN);
        JButton btnCancel = createButton("✖ Cancel Appt.", ACCENT_RED);

        btnEdit.addActionListener(e -> editSelectedAppointment());
        btnComplete.addActionListener(e -> markSelectedStatus(AppointmentStatus.COMPLETED));
        btnCancel.addActionListener(e -> cancelSelectedAppointment());

        rowActions.add(btnEdit);
        rowActions.add(btnComplete);
        rowActions.add(btnCancel);

        bottomBar.add(statusLabel, BorderLayout.WEST);
        bottomBar.add(rowActions, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // ── DATA OPERATIONS ────────────────────────────────────────────────────────

    public void loadData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Appointment> list = controller.getAllAppointments();
                tableModel.setData(list);
                statusLabel.setText("Total: " + list.size() + " appointment(s) loaded.");
            } catch (Exception ex) {
                statusLabel.setText("Error loading data: " + ex.getMessage());
                JOptionPane.showMessageDialog(this,
                        "Failed to load appointments:\n" + ex.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void loadTodayOnly() {
        try {
            List<Appointment> list = controller.getTodayAppointments();
            tableModel.setData(list);
            statusLabel.setText("Today's appointments: " + list.size() + " record(s).");
            searchField.setText("");
            statusFilterCombo.setSelectedIndex(0);
        } catch (Exception ex) {
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String text = searchField.getText().trim();
        String statusSel = (String) statusFilterCombo.getSelectedItem();

        RowFilter<AppointmentTableModel, Object> textFilter = null;
        RowFilter<AppointmentTableModel, Object> statusFilter = null;

        if (!text.isEmpty()) {
            textFilter = RowFilter.regexFilter("(?i)" + text, 1, 2, 3, 6);
        }
        if (statusSel != null && !statusSel.equals("All Statuses")) {
            statusFilter = RowFilter.regexFilter("(?i)" + statusSel, 5);
        }

        if (textFilter != null && statusFilter != null) {
            sorter.setRowFilter(RowFilter.andFilter(List.of(textFilter, statusFilter)));
        } else if (textFilter != null) {
            sorter.setRowFilter(textFilter);
        } else if (statusFilter != null) {
            sorter.setRowFilter(statusFilter);
        } else {
            sorter.setRowFilter(null);
        }
    }

    private void openNewAppointmentDialog() {
        AppointmentDialog dialog = new AppointmentDialog(
                SwingUtilities.getWindowAncestor(this), null, controller);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void editSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to edit.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        Appointment appt = tableModel.getAppointmentAt(modelRow);

        AppointmentDialog dialog = new AppointmentDialog(
                SwingUtilities.getWindowAncestor(this), appt, controller);
        dialog.setVisible(true);
        if (dialog.isSaved()) loadData();
    }

    private void markSelectedStatus(AppointmentStatus status) {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        Appointment appt = tableModel.getAppointmentAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark appointment for " + appt.getPatientName() + " as " + status.name() + "?",
                "Update Status", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controller.updateStatus(appt.getAppointmentId(), status);
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelectedAppointment() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        Appointment appt = tableModel.getAppointmentAt(modelRow);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Cancel appointment for " + appt.getPatientName() + "?\nThis cannot be undone.",
                "Cancel Appointment", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            controller.cancelAppointment(appt.getAppointmentId());
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Cancel Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

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

    private static class AppointmentTableModel extends AbstractTableModel {
        private static final String[] COLUMNS = {
                "#", "Patient", "Doctor", "Specialization", "Date", "Status", "Reason", "Time"};
        private List<Appointment> data = new ArrayList<>();

        public void setData(List<Appointment> data) {
            this.data = data == null ? new ArrayList<>() : data;
            fireTableDataChanged();
        }

        public Appointment getAppointmentAt(int row) {
            return data.get(row);
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return COLUMNS.length; }
        @Override public String getColumnName(int col) { return COLUMNS[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            Appointment a = data.get(row);
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy");
            DateTimeFormatter ttf = DateTimeFormatter.ofPattern("hh:mm a");
            return switch (col) {
                case 0 -> a.getAppointmentId();
                case 1 -> a.getPatientName() != null ? a.getPatientName() : "P#" + a.getPatientId();
                case 2 -> a.getDoctorName()  != null ? a.getDoctorName()  : "D#" + a.getDoctorId();
                case 3 -> a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "-";
                case 4 -> a.getAppointmentDate() != null ? a.getAppointmentDate().format(dtf) : "-";
                case 5 -> a.getStatus() != null ? a.getStatus().name() : "-";
                case 6 -> a.getReasonForVisit() != null ? a.getReasonForVisit() : "-";
                case 7 -> a.getAppointmentTime() != null ? a.getAppointmentTime().format(ttf) : "-";
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class;
        }
    }

    // ── STATUS RENDERER ───────────────────────────────────────────────────────

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            String status = value != null ? value.toString() : "";
            if (!isSelected) {
                setBackground(switch (status) {
                    case "SCHEDULED"  -> new Color(209, 231, 221);
                    case "COMPLETED"  -> new Color(207, 226, 255);
                    case "CANCELLED"  -> new Color(248, 215, 218);
                    case "NO_SHOW"    -> new Color(255, 243, 205);
                    default           -> Color.WHITE;
                });
                setForeground(switch (status) {
                    case "SCHEDULED"  -> new Color(25, 135, 84);
                    case "COMPLETED"  -> new Color(13, 110, 253);
                    case "CANCELLED"  -> new Color(220, 53, 69);
                    case "NO_SHOW"    -> new Color(108, 60, 10);
                    default           -> Color.DARK_GRAY;
                });
            }
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            setText(" " + status + " ");
            return this;
        }
    }
}
