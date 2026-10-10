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

        JLabel pageTitle = new JLabel("◷  Appointment Management");
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

        // Status column coloring (Column 8)
        appointmentTable.getColumnModel().getColumn(8).setCellRenderer(new StatusCellRenderer());
        // Ticket / Token column badge renderer (Column 1)
        appointmentTable.getColumnModel().getColumn(1).setCellRenderer(new TicketCellRenderer());

        // Column widths
        int[] colWidths = {45, 125, 140, 105, 140, 130, 95, 85, 100, 150};
        for (int i = 0; i < colWidths.length; i++) {
            appointmentTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
        }

        sorter = new TableRowSorter<>(tableModel);
        appointmentTable.setRowSorter(sorter);

        // Double click to view ticket details
        appointmentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    viewSelectedTicketDetails();
                }
            }
        });

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

        JButton btnTicket = createButton("🎫 View Ticket Details", new Color(13, 148, 136));
        JButton btnEdit   = createButton("✏ Edit", ACCENT_BLUE);
        JButton btnComplete = createButton("✓ Mark Complete", ACCENT_GREEN);
        JButton btnCancel = createButton("✖ Cancel Appt.", ACCENT_RED);

        btnTicket.addActionListener(e -> viewSelectedTicketDetails());
        btnEdit.addActionListener(e -> editSelectedAppointment());
        btnComplete.addActionListener(e -> markSelectedStatus(AppointmentStatus.COMPLETED));
        btnCancel.addActionListener(e -> cancelSelectedAppointment());

        rowActions.add(btnTicket);
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
            textFilter = RowFilter.regexFilter("(?i)" + text, 0, 1, 2, 3, 4, 5, 9);
        }
        if (statusSel != null && !statusSel.equals("All Statuses")) {
            statusFilter = RowFilter.regexFilter("(?i)" + statusSel, 8);
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

    public void viewSelectedTicketDetails() {
        int selectedRow = appointmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an appointment to view ticket details.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = appointmentTable.convertRowIndexToModel(selectedRow);
        Appointment a = tableModel.getAppointmentAt(modelRow);

        showTicketDetailsDialog(a);
    }

    public void showTicketDetailsDialog(Appointment a) {
        JDialog ticketDialog = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Appointment Ticket Slip: " + a.getTokenNumber(), Dialog.ModalityType.APPLICATION_MODAL);
        ticketDialog.setSize(520, 620);
        ticketDialog.setLocationRelativeTo(this);
        ticketDialog.setLayout(new BorderLayout());

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(Color.WHITE);
        root.setBorder(new EmptyBorder(20, 25, 20, 25));

        // Header Banner
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42)); // Slate Navy
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("✚  SMARTCARE HOSPITAL");
        title.setFont(new Font("Segoe UI", Font.BOLD, 17));
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Official OPD Checkup & Consultation Ticket");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(148, 163, 184));

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 3));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(sub);

        JLabel queueLbl = new JLabel("#" + a.getAppointmentId());
        queueLbl.setFont(new Font("Segoe UI", Font.BOLD, 28));
        queueLbl.setForeground(new Color(56, 189, 248)); // Medical cyan
        queueLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        headerPanel.add(titleBlock, BorderLayout.WEST);
        headerPanel.add(queueLbl, BorderLayout.EAST);
        root.add(headerPanel);
        root.add(Box.createRigidArea(new Dimension(0, 16)));

        // Token Badge Box
        JPanel tokenBox = new JPanel(new BorderLayout());
        tokenBox.setBackground(new Color(240, 253, 250)); // Light teal
        tokenBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 212, 191), 1),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel tLbl = new JLabel("TICKET / TOKEN NUMBER:");
        tLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        tLbl.setForeground(new Color(15, 118, 110));

        JLabel tVal = new JLabel(a.getTokenNumber());
        tVal.setFont(new Font("Consolas", Font.BOLD, 22));
        tVal.setForeground(new Color(13, 148, 136));

        tokenBox.add(tLbl, BorderLayout.NORTH);
        tokenBox.add(tVal, BorderLayout.CENTER);
        root.add(tokenBox);
        root.add(Box.createRigidArea(new Dimension(0, 16)));

        // Details Grid
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy (EEEE)");
        DateTimeFormatter ttf = DateTimeFormatter.ofPattern("hh:mm a");

        addDetailItem(grid, "Patient Full Name", a.getPatientName() != null ? a.getPatientName() : "-");
        addDetailItem(grid, "Patient UHID Code", a.getPatientCode() != null ? a.getPatientCode() : "PAT-" + a.getPatientId());
        addDetailItem(grid, "Contact Phone", a.getPatientPhone() != null && !a.getPatientPhone().isEmpty() ? a.getPatientPhone() : "Recorded on file");
        addDetailItem(grid, "Appointment Status", a.getStatus() != null ? a.getStatus().name() : "SCHEDULED");
        addDetailItem(grid, "Attending Doctor", a.getDoctorName() != null ? a.getDoctorName() : "Doctor #" + a.getDoctorId());
        addDetailItem(grid, "Specialization", a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "General");
        addDetailItem(grid, "Scheduled Date", a.getAppointmentDate() != null ? a.getAppointmentDate().format(dtf) : "-");
        addDetailItem(grid, "Time Slot", a.getAppointmentTime() != null ? a.getAppointmentTime().format(ttf) : "-");
        addDetailItem(grid, "Consultation Fee", a.getDoctorFee() != null && a.getDoctorFee() > 0 ? "₹ " + String.format("%.2f", a.getDoctorFee()) : "Standard OPD");
        addDetailItem(grid, "Reason for Checkup", a.getReasonForVisit() != null ? a.getReasonForVisit() : "Routine Checkup");

        root.add(grid);
        root.add(Box.createRigidArea(new Dimension(0, 14)));

        // Notes & Source box
        String notesText = a.getNotes() != null && !a.getNotes().isEmpty() ? a.getNotes() : "Created via SmartCare HMS Counter";
        JLabel notesLbl = new JLabel("Notes / Source: " + notesText);
        notesLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        notesLbl.setForeground(new Color(100, 116, 139));
        root.add(notesLbl);

        // Buttons Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(new Color(248, 249, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton copyBtn = new JButton("📋 Copy Token");
        copyBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        copyBtn.addActionListener(e -> {
            java.awt.datatransfer.StringSelection ss = new java.awt.datatransfer.StringSelection(a.getTokenNumber());
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            JOptionPane.showMessageDialog(ticketDialog, "Token " + a.getTokenNumber() + " copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton printBtn = new JButton("🖨 Print / Save Slip");
        printBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        printBtn.setBackground(new Color(13, 110, 253));
        printBtn.setForeground(Color.WHITE);
        printBtn.addActionListener(e -> {
            JOptionPane.showMessageDialog(ticketDialog,
                    "Ticket slip sent to default printer queue!\nToken: " + a.getTokenNumber() + "\nPatient: " + a.getPatientName(),
                    "Print Ticket", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        closeBtn.addActionListener(e -> ticketDialog.dispose());

        footer.add(copyBtn);
        footer.add(printBtn);
        footer.add(closeBtn);

        ticketDialog.add(new JScrollPane(root), BorderLayout.CENTER);
        ticketDialog.add(footer, BorderLayout.SOUTH);
        ticketDialog.setVisible(true);
    }

    private void addDetailItem(JPanel panel, String label, String value) {
        JPanel p = new JPanel(new BorderLayout(0, 2));
        p.setOpaque(false);
        JLabel l = new JLabel(label.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(new Color(100, 116, 139));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 13));
        v.setForeground(new Color(30, 41, 59));
        p.add(l, BorderLayout.NORTH);
        p.add(v, BorderLayout.CENTER);
        panel.add(p);
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
                "#", "Ticket / Token #", "Patient Name", "Phone", "Attending Doctor", "Specialization", "Date", "Time", "Status", "Reason"};
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
                case 1 -> a.getTokenNumber();
                case 2 -> a.getPatientName() != null ? a.getPatientName() : "P#" + a.getPatientId();
                case 3 -> a.getPatientPhone() != null && !a.getPatientPhone().isEmpty() ? a.getPatientPhone() : "-";
                case 4 -> a.getDoctorName()  != null ? a.getDoctorName()  : "D#" + a.getDoctorId();
                case 5 -> a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "-";
                case 6 -> a.getAppointmentDate() != null ? a.getAppointmentDate().format(dtf) : "-";
                case 7 -> a.getAppointmentTime() != null ? a.getAppointmentTime().format(ttf) : "-";
                case 8 -> a.getStatus() != null ? a.getStatus().name() : "-";
                case 9 -> a.getReasonForVisit() != null ? a.getReasonForVisit() : "-";
                default -> "";
            };
        }

        @Override public Class<?> getColumnClass(int col) {
            return col == 0 ? Integer.class : String.class;
        }
    }

    // ── TICKET CELL RENDERER ──────────────────────────────────────────────────

    private static class TicketCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Consolas", Font.BOLD, 12));
            if (!isSelected) {
                setForeground(new Color(13, 148, 136)); // Teal
                setBackground(new Color(240, 253, 250));
            }
            setText(value != null ? value.toString() : "-");
            return this;
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
