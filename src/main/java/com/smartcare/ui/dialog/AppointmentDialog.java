package com.smartcare.ui.dialog;

import com.smartcare.controller.AppointmentController;
import com.smartcare.enums.AppointmentStatus;
import com.smartcare.model.Appointment;
import com.smartcare.model.Doctor;
import com.smartcare.model.Patient;
import com.smartcare.dao.impl.DoctorDAOImpl;
import com.smartcare.dao.impl.PatientDAOImpl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Modal dialog for creating or editing appointments in SmartCare.
 */
public class AppointmentDialog extends JDialog {

    private final AppointmentController controller;
    private final Appointment existingAppointment;
    private boolean saved = false;

    private JComboBox<PatientItem> patientCombo;
    private JComboBox<DoctorItem> doctorCombo;
    private JTextField dateField;
    private JComboBox<String> timeCombo;
    private JComboBox<AppointmentStatus> statusCombo;
    private JTextField reasonField;
    private JTextArea notesArea;
    private JLabel errorLabel;

    private static final Color ACCENT = new Color(13, 110, 253);

    public AppointmentDialog(Window parent, Appointment appointment, AppointmentController controller) {
        super(parent, appointment == null ? "Schedule New Appointment" : "Edit Appointment", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        this.existingAppointment = appointment;
        initComponents();
        if (appointment != null) populateFields(appointment);
        pack();
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ── HEADER ──────────────────────────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 12));
        header.setBackground(new Color(24, 43, 73));
        JLabel title = new JLabel(existingAppointment == null
                ? "📅  Schedule New Appointment" : "✏  Edit Appointment #" + existingAppointment.getAppointmentId());
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        header.add(title);

        // ── FORM ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 5, 7, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Patient Combo
        List<Patient> patients = new PatientDAOImpl().findAll();
        PatientItem[] patientItems = patients.stream()
                .map(p -> new PatientItem(p.getPatientId(),
                        p.getFullName() + " [" + p.getPatientCode() + "]"))
                .toArray(PatientItem[]::new);
        patientCombo = new JComboBox<>(patientItems);
        styleCombo(patientCombo);

        // Doctor Combo
        List<Doctor> doctors = new DoctorDAOImpl().findAll();
        DoctorItem[] doctorItems = doctors.stream()
                .map(d -> new DoctorItem(d.getDoctorId(),
                        "Dr. " + d.getFullName() + " (" + d.getSpecialization() + ")"))
                .toArray(DoctorItem[]::new);
        doctorCombo = new JComboBox<>(doctorItems);
        styleCombo(doctorCombo);

        // Date
        dateField = new JTextField(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), 14);
        styleField(dateField);

        // Time slots every 30 minutes from 08:00 to 17:30
        String[] times = generateTimeSlots();
        timeCombo = new JComboBox<>(times);
        styleCombo(timeCombo);

        // Status (only shown in edit mode)
        statusCombo = new JComboBox<>(AppointmentStatus.values());
        styleCombo(statusCombo);

        // Reason
        reasonField = new JTextField(24);
        reasonField.putClientProperty("JTextField.placeholderText", "e.g. General checkup, Follow-up...");
        styleField(reasonField);

        // Notes
        notesArea = new JTextArea(3, 24);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createLineBorder(new Color(206, 212, 218)));
        JScrollPane noteScroll = new JScrollPane(notesArea);

        // Error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        errorLabel.setForeground(new Color(220, 53, 69));

        // Layout rows
        addRow(form, gbc, 0, "Patient *:", patientCombo);
        addRow(form, gbc, 1, "Doctor *:", doctorCombo);
        addRow(form, gbc, 2, "Date * (yyyy-MM-dd):", dateField);
        addRow(form, gbc, 3, "Time Slot *:", timeCombo);
        if (existingAppointment != null) {
            addRow(form, gbc, 4, "Status:", statusCombo);
        }
        addRow(form, gbc, existingAppointment != null ? 5 : 4, "Reason for Visit:", reasonField);
        addRow(form, gbc, existingAppointment != null ? 6 : 5, "Notes:", noteScroll);

        gbc.gridx = 0; gbc.gridy = existingAppointment != null ? 7 : 6;
        gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        form.add(errorLabel, gbc);

        // ── BUTTONS ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(new Color(248, 249, 250));
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(222, 226, 230)));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = new JButton(existingAppointment == null ? "Schedule Appointment" : "Save Changes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setBackground(ACCENT);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveAppointment());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(header, BorderLayout.NORTH);
        add(new JScrollPane(form), BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void populateFields(Appointment a) {
        // Select patient
        for (int i = 0; i < patientCombo.getItemCount(); i++) {
            if (patientCombo.getItemAt(i).id == a.getPatientId()) {
                patientCombo.setSelectedIndex(i);
                break;
            }
        }
        // Select doctor
        for (int i = 0; i < doctorCombo.getItemCount(); i++) {
            if (doctorCombo.getItemAt(i).id == a.getDoctorId()) {
                doctorCombo.setSelectedIndex(i);
                break;
            }
        }
        if (a.getAppointmentDate() != null)
            dateField.setText(a.getAppointmentDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (a.getAppointmentTime() != null)
            timeCombo.setSelectedItem(a.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        if (a.getStatus() != null)
            statusCombo.setSelectedItem(a.getStatus());
        if (a.getReasonForVisit() != null)
            reasonField.setText(a.getReasonForVisit());
        if (a.getNotes() != null)
            notesArea.setText(a.getNotes());
    }

    private void saveAppointment() {
        errorLabel.setText(" ");
        try {
            PatientItem selectedPatient = (PatientItem) patientCombo.getSelectedItem();
            DoctorItem selectedDoctor   = (DoctorItem)  doctorCombo.getSelectedItem();

            if (selectedPatient == null || selectedDoctor == null) {
                errorLabel.setText("Please select a patient and a doctor.");
                return;
            }

            LocalDate date;
            try {
                date = LocalDate.parse(dateField.getText().trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException ex) {
                errorLabel.setText("Invalid date format. Use yyyy-MM-dd (e.g. 2025-12-31).");
                return;
            }

            LocalTime time = LocalTime.parse(timeCombo.getSelectedItem().toString());

            Appointment appt = existingAppointment != null ? existingAppointment : new Appointment();
            appt.setPatientId(selectedPatient.id);
            appt.setDoctorId(selectedDoctor.id);
            appt.setAppointmentDate(date);
            appt.setAppointmentTime(time);
            String userNotes = notesArea.getText().trim();
            if (existingAppointment == null) {
                if (!userNotes.contains("Token:")) {
                    String token = "SC-" + date.getYear() + "-" + String.format("%04d", (int)(System.currentTimeMillis() % 9000 + 1000));
                    appt.setNotes("Token: " + token + (userNotes.isEmpty() ? "" : " | " + userNotes));
                } else {
                    appt.setNotes(userNotes);
                }
            } else {
                appt.setNotes(userNotes);
            }

            if (existingAppointment != null)
                appt.setStatus((AppointmentStatus) statusCombo.getSelectedItem());

            if (existingAppointment == null) {
                Appointment created = controller.scheduleAppointment(appt);
                String tokenDisplay = created != null ? created.getTokenNumber() : appt.getTokenNumber();
                JOptionPane.showMessageDialog(this, "✅  Appointment scheduled successfully!\n\nTicket / Token #: " + tokenDisplay,
                        "Scheduled", JOptionPane.INFORMATION_MESSAGE);
            } else {
                controller.updateAppointment(appt);
                JOptionPane.showMessageDialog(this, "✅  Appointment updated successfully!\n\nTicket / Token #: " + appt.getTokenNumber(),
                        "Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();

        } catch (Exception ex) {
            errorLabel.setText("Error: " + ex.getMessage());
        }
    }

    private String[] generateTimeSlots() {
        java.util.List<String> slots = new java.util.ArrayList<>();
        for (int h = 8; h <= 17; h++) {
            slots.add(String.format("%02d:00", h));
            slots.add(String.format("%02d:30", h));
        }
        return slots.toArray(new String[0]);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(52, 58, 64));
        form.add(lbl, gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        form.add(comp, gbc);
        gbc.weightx = 0;
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(5, 8, 5, 8)));
    }

    private void styleCombo(JComboBox<?> c) {
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    public boolean isSaved() { return saved; }

    // Helper inner records
    private static class PatientItem {
        final int id; final String label;
        PatientItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class DoctorItem {
        final int id; final String label;
        DoctorItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
