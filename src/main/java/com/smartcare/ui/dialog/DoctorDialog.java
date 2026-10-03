package com.smartcare.ui.dialog;

import com.smartcare.controller.DoctorController;
import com.smartcare.model.Department;
import com.smartcare.model.Doctor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Modal Dialog for Registering and Editing Doctor profiles.
 */
public class DoctorDialog extends JDialog {
    private final DoctorController doctorController;
    private final Doctor existingDoctor;
    private boolean saved = false;

    private JTextField nameField;
    private JTextField specializationField;
    private JComboBox<Department> departmentCombo;
    private JTextField licenseField;
    private JTextField phoneField;
    private JTextField emailField;
    private JTextField feeField;
    private JTextField qualificationField;
    private JSpinner experienceSpinner;
    private JTextField daysField;
    private JCheckBox availableCheck;

    public DoctorDialog(Frame owner, DoctorController doctorController, Doctor doctorToEdit) {
        super(owner, doctorToEdit == null ? "Add Medical Doctor" : "Edit Doctor Profile", true);
        this.doctorController = doctorController;
        this.existingDoctor = doctorToEdit;

        initComponents();
        loadDepartments();
        if (existingDoctor != null) {
            populateFields(existingDoctor);
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

        JLabel titleLbl = new JLabel(existingDoctor == null ? "🩺 Add Doctor Profile" : "✏ Edit Doctor Profile");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLbl.setForeground(Color.WHITE);
        headerPanel.add(titleLbl, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        // Row 0: Full Name
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        formPanel.add(createFieldPanel("Doctor Full Name (e.g. Dr. Rajesh Sharma) *", nameField = new JTextField()), gbc);

        // Row 1: Specialization & Department
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1; gbc.weightx = 0.5;
        formPanel.add(createFieldPanel("Specialization (e.g. Cardiologist) *", specializationField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        departmentCombo = new JComboBox<>();
        formPanel.add(createFieldPanel("Department *", departmentCombo), gbc);

        // Row 2: License Number & Consultation Fee
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(createFieldPanel("Medical License Number *", licenseField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 2;
        feeField = new JTextField("500.00");
        formPanel.add(createFieldPanel("Consultation Fee (₹) *", feeField), gbc);

        // Row 3: Phone & Email
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(createFieldPanel("Phone Number *", phoneField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(createFieldPanel("Official Email Address *", emailField = new JTextField()), gbc);

        // Row 4: Qualification & Experience
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(createFieldPanel("Qualifications (e.g. MBBS, MD, DM)", qualificationField = new JTextField()), gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        experienceSpinner = new JSpinner(new SpinnerNumberModel(5, 0, 50, 1));
        formPanel.add(createFieldPanel("Experience (Years)", experienceSpinner), gbc);

        // Row 5: Available Days & Status
        gbc.gridx = 0; gbc.gridy = 5;
        daysField = new JTextField("Mon,Tue,Wed,Thu,Fri");
        formPanel.add(createFieldPanel("Duty Days (e.g. Mon,Wed,Fri)", daysField), gbc);

        gbc.gridx = 1; gbc.gridy = 5;
        availableCheck = new JCheckBox("Currently Available for Consultation", true);
        availableCheck.setOpaque(false);
        formPanel.add(createFieldPanel("Availability", availableCheck), gbc);

        JScrollPane centerScroll = new JScrollPane(formPanel);
        centerScroll.setBorder(null);
        add(centerScroll, BorderLayout.CENTER);

        // Bottom Actions
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setBackground(new Color(245, 247, 250));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelBtn.addActionListener(e -> dispose());

        JButton saveBtn = new JButton(existingDoctor == null ? "Save Doctor" : "Save Changes");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setBackground(new Color(25, 135, 84));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> saveDoctor());

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

    private void loadDepartments() {
        try {
            List<Department> departments = doctorController.getAllDepartments();
            departmentCombo.removeAllItems();
            for (Department d : departments) {
                departmentCombo.addItem(d);
            }
        } catch (Exception e) {
            departmentCombo.addItem(new Department(1, "General Medicine", "", ""));
        }
    }

    private void populateFields(Doctor d) {
        nameField.setText(d.getFullName());
        specializationField.setText(d.getSpecialization());
        licenseField.setText(d.getLicenseNumber());
        phoneField.setText(d.getPhone());
        emailField.setText(d.getEmail());
        feeField.setText(d.getConsultationFee() != null ? d.getConsultationFee().toString() : "500.00");
        qualificationField.setText(d.getQualification() != null ? d.getQualification() : "");
        experienceSpinner.setValue(d.getExperienceYears());
        daysField.setText(d.getAvailableDays() != null ? d.getAvailableDays() : "Mon,Tue,Wed,Thu,Fri");
        availableCheck.setSelected(d.isAvailable());

        if (d.getDepartmentId() != null) {
            for (int i = 0; i < departmentCombo.getItemCount(); i++) {
                Department dept = departmentCombo.getItemAt(i);
                if (dept.getDepartmentId() == d.getDepartmentId()) {
                    departmentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void saveDoctor() {
        try {
            BigDecimal fee;
            try {
                fee = new BigDecimal(feeField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid numeric consultation fee (e.g. 600.00)", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Doctor d = existingDoctor != null ? existingDoctor : new Doctor();
            d.setFullName(nameField.getText().trim());
            d.setSpecialization(specializationField.getText().trim());

            Department selectedDept = (Department) departmentCombo.getSelectedItem();
            if (selectedDept != null) {
                d.setDepartmentId(selectedDept.getDepartmentId());
                d.setDepartmentName(selectedDept.getName());
            }

            d.setLicenseNumber(licenseField.getText().trim());
            d.setPhone(phoneField.getText().trim());
            d.setEmail(emailField.getText().trim());
            d.setConsultationFee(fee);
            d.setQualification(qualificationField.getText().trim());
            d.setExperienceYears((Integer) experienceSpinner.getValue());
            d.setAvailableDays(daysField.getText().trim());
            d.setAvailable(availableCheck.isSelected());

            if (existingDoctor == null) {
                doctorController.registerDoctor(d);
                JOptionPane.showMessageDialog(this, "Doctor " + d.getFullName() + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                doctorController.updateDoctor(d);
                JOptionPane.showMessageDialog(this, "Doctor details updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
