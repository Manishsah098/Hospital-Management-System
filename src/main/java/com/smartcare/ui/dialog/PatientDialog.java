package com.smartcare.ui.dialog;

import com.smartcare.controller.PatientController;
import com.smartcare.enums.Gender;
import com.smartcare.model.Patient;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Modal Dialog for Registering and Editing Patient information.
 */
public class PatientDialog extends JDialog {
    private final PatientController patientController;
    private final Patient existingPatient;
    private boolean saved = false;

    private JTextField codeField;
    private JTextField nameField;
    private JTextField dobField;
    private JComboBox<Gender> genderCombo;
    private JComboBox<String> bloodGroupCombo;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextArea addressArea;
    private JTextField emgNameField;
    private JTextField emgPhoneField;
    private JTextField allergiesField;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PatientDialog(Frame owner, PatientController patientController, Patient patientToEdit) {
        super(owner, patientToEdit == null ? "Register New Patient" : "Edit Patient Information", true);
        this.patientController = patientController;
        this.existingPatient = patientToEdit;

        initComponents();
        if (existingPatient != null) {
            populateFields(existingPatient);
        } else {
            codeField.setText(patientController.getNextPatientCode());
            dobField.setText(LocalDate.now().minusYears(25).format(DATE_FMT));
        }
    }

    private void initComponents() {
        setSize(580, 680);
        setLocationRelativeTo(getOwner());
        setResizable(false);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLbl = new JLabel(existingPatient == null ? "➕ Patient Registration Form" : "✏ Update Patient Details");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Content
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Row 0: Code & Gender
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        formPanel.add(createFieldPanel("Patient Code / UHID", codeField = new JTextField()), gbc);
        codeField.setEditable(false);
        codeField.setBackground(new Color(240, 243, 246));

        gbc.gridx = 1; gbc.gridy = 0;
        genderCombo = new JComboBox<>(Gender.values());
        formPanel.add(createFieldPanel("Gender *", genderCombo), gbc);

        // Row 1: Full Name
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        formPanel.add(createFieldPanel("Full Name *", nameField = new JTextField()), gbc);

        // Row 2: DOB & Blood Group
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(createFieldPanel("Date of Birth (YYYY-MM-DD) *", dobField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        String[] bloodGroups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown"};
        bloodGroupCombo = new JComboBox<>(bloodGroups);
        formPanel.add(createFieldPanel("Blood Group", bloodGroupCombo), gbc);

        // Row 3: Phone & Email
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createFieldPanel("Contact Phone *", phoneField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(createFieldPanel("Email Address", emailField = new JTextField()), gbc);

        // Row 4: Address
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        addressArea = new JTextArea(2, 20);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(addressArea);
        formPanel.add(createFieldPanel("Residential Address", addrScroll), gbc);

        // Row 5: Emergency Contact Name & Phone
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(createFieldPanel("Emergency Contact Person", emgNameField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(createFieldPanel("Emergency Contact Phone", emgPhoneField = new JTextField()), gbc);

        // Row 6: Known Allergies
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        formPanel.add(createFieldPanel("Known Drug Allergies / Conditions", allergiesField = new JTextField()), gbc);

        JScrollPane centerScroll = new JScrollPane(formPanel);
        centerScroll.setBorder(null);
        add(centerScroll, BorderLayout.CENTER);

        // Bottom Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingPatient == null ? "Register Patient" : "Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(new Color(13, 110, 253));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> savePatient());

        buttonPanel.add(cancelBtn);
        buttonPanel.add(saveBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createFieldPanel(String labelText, JComponent field) {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(60, 68, 77));
        p.add(lbl, BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private void populateFields(Patient p) {
        codeField.setText(p.getPatientCode());
        nameField.setText(p.getFullName());
        if (p.getDateOfBirth() != null) {
            dobField.setText(p.getDateOfBirth().format(DATE_FMT));
        }
        if (p.getGender() != null) genderCombo.setSelectedItem(p.getGender());
        if (p.getBloodGroup() != null) bloodGroupCombo.setSelectedItem(p.getBloodGroup());
        phoneField.setText(p.getPhone());
        emailField.setText(p.getEmail() != null ? p.getEmail() : "");
        addressArea.setText(p.getAddress() != null ? p.getAddress() : "");
        emgNameField.setText(p.getEmergencyContactName() != null ? p.getEmergencyContactName() : "");
        emgPhoneField.setText(p.getEmergencyContactPhone() != null ? p.getEmergencyContactPhone() : "");
        allergiesField.setText(p.getAllergies() != null ? p.getAllergies() : "");
    }

    private void savePatient() {
        try {
            LocalDate dob;
            try {
                dob = LocalDate.parse(dobField.getText().trim(), DATE_FMT);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid Date format! Please use YYYY-MM-DD (e.g. 1995-08-25)", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Patient p = existingPatient != null ? existingPatient : new Patient();
            p.setPatientCode(codeField.getText().trim());
            p.setFullName(nameField.getText().trim());
            p.setDateOfBirth(dob);
            p.setGender((Gender) genderCombo.getSelectedItem());
            p.setBloodGroup((String) bloodGroupCombo.getSelectedItem());
            p.setPhone(phoneField.getText().trim());
            p.setEmail(emailField.getText().trim());
            p.setAddress(addressArea.getText().trim());
            p.setEmergencyContactName(emgNameField.getText().trim());
            p.setEmergencyContactPhone(emgPhoneField.getText().trim());
            p.setAllergies(allergiesField.getText().trim());

            if (existingPatient == null) {
                patientController.registerPatient(p);
                JOptionPane.showMessageDialog(this, "Patient " + p.getFullName() + " registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                patientController.updatePatient(p);
                JOptionPane.showMessageDialog(this, "Patient details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
