package com.smartcare.ui;

import com.smartcare.controller.AuthController;
import com.smartcare.enums.UserRole;
import com.smartcare.model.User;
import com.smartcare.security.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern Java Swing Login Frame for SmartCare Hospital Management System.
 */
public class LoginFrame extends JFrame {
    private final AuthController authController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JCheckBox showPasswordCheck;
    private JLabel statusLabel;
    private JProgressBar progressBar;
    private JComboBox<String> roleQuickSelect;

    public LoginFrame() {
        this.authController = new AuthController();
        initComponents();
    }

    private void initComponents() {
        setTitle("SmartCare - AI Hospital Management System | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 560);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());

        // Main Split Container (Left Branding Hero, Right Login Card)
        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        // 1. LEFT PANEL - Branding & Overview
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(new Color(24, 43, 73)); // Deep Hospital Navy
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel brandContent = new JPanel();
        brandContent.setOpaque(false);
        brandContent.setLayout(new BoxLayout(brandContent, BoxLayout.Y_AXIS));

        JLabel logoIcon = new JLabel("⚕");
        logoIcon.setFont(new Font("Segoe UI Symbol", Font.BOLD, 54));
        logoIcon.setForeground(new Color(66, 165, 245));
        logoIcon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appTitle = new JLabel("SmartCare");
        appTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        appTitle.setForeground(Color.WHITE);
        appTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subTitle = new JLabel("AI-Enhanced Hospital Management");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subTitle.setForeground(new Color(180, 205, 237));
        subTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel desc = new JLabel("<html><br><br>• Intelligent Clinical Workflows<br>• Role-Based Secure Access Control<br>• AI Copilot & Diagnostics Assistant<br>• Real-time Inpatient & Billing Sync</html>");
        desc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        desc.setForeground(new Color(200, 220, 245));
        desc.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandContent.add(logoIcon);
        brandContent.add(Box.createRigidArea(new Dimension(0, 10)));
        brandContent.add(appTitle);
        brandContent.add(subTitle);
        brandContent.add(desc);

        JLabel versionLabel = new JLabel("Academic PBL Project • Java 21 • MySQL");
        versionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        versionLabel.setForeground(new Color(140, 170, 205));

        leftPanel.add(brandContent, BorderLayout.CENTER);
        leftPanel.add(versionLabel, BorderLayout.SOUTH);

        // 2. RIGHT PANEL - Form
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new BorderLayout());
        rightPanel.setBorder(new EmptyBorder(35, 45, 35, 45));

        JPanel formPanel = new JPanel();
        formPanel.setOpaque(false);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));

        JLabel loginHeader = new JLabel("Welcome Back");
        loginHeader.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loginHeader.setForeground(new Color(33, 37, 41));
        loginHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel loginSub = new JLabel("Enter your credentials to access your dashboard");
        loginSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        loginSub.setForeground(new Color(108, 117, 125));
        loginSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Demo Quick Fill Selector
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        demoPanel.setOpaque(false);
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel quickLabel = new JLabel("Quick Demo Login: ");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quickLabel.setForeground(new Color(70, 90, 120));

        String[] demoRoles = {
                "-- Select Demo Account --",
                "ADMIN (admin)",
                "DOCTOR (dr.sharma)",
                "DOCTOR (dr.patel)",
                "RECEPTIONIST (receptionist1)",
                "LAB TECHNICIAN (labtech1)",
                "PHARMACIST (pharmacist1)"
        };
        roleQuickSelect = new JComboBox<>(demoRoles);
        roleQuickSelect.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleQuickSelect.addActionListener(e -> fillDemoCredentials());

        demoPanel.add(quickLabel);
        demoPanel.add(roleQuickSelect);

        // Username Field
        JLabel userLabel = new JLabel("Username / Staff ID");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        userLabel.setForeground(new Color(73, 80, 87));
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Password Field
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        passLabel.setForeground(new Color(73, 80, 87));
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheck.setOpaque(false);
        showPasswordCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        showPasswordCheck.addActionListener(e -> {
            if (showPasswordCheck.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('•');
            }
        });

        // Status Label & Progress Bar
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(220, 53, 69));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 4));
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        loginButton = new JButton("Sign In to Portal");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(13, 110, 253));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.addActionListener(e -> performLogin());

        // Keyboard Enter key listeners
        KeyAdapter enterListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    performLogin();
                }
            }
        };
        usernameField.addKeyListener(enterListener);
        passwordField.addKeyListener(enterListener);

        // Add components with spacing
        formPanel.add(loginHeader);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(loginSub);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(demoPanel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(userLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(usernameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        formPanel.add(passLabel);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        formPanel.add(showPasswordCheck);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(statusLabel);
        formPanel.add(progressBar);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(loginButton);

        rightPanel.add(formPanel, BorderLayout.CENTER);

        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);
        add(mainPanel, BorderLayout.CENTER);
    }

    private void fillDemoCredentials() {
        int index = roleQuickSelect.getSelectedIndex();
        String defaultPass = "Admin@123";
        switch (index) {
            case 1 -> { // ADMIN
                usernameField.setText("admin");
                passwordField.setText(defaultPass);
            }
            case 2 -> { // DOCTOR Sharma
                usernameField.setText("dr.sharma");
                passwordField.setText(defaultPass);
            }
            case 3 -> { // DOCTOR Patel
                usernameField.setText("dr.patel");
                passwordField.setText(defaultPass);
            }
            case 4 -> { // RECEPTIONIST
                usernameField.setText("receptionist1");
                passwordField.setText(defaultPass);
            }
            case 5 -> { // LAB TECH
                usernameField.setText("labtech1");
                passwordField.setText(defaultPass);
            }
            case 6 -> { // PHARMACIST
                usernameField.setText("pharmacist1");
                passwordField.setText(defaultPass);
            }
            default -> {
                usernameField.setText("");
                passwordField.setText("");
            }
        }
        statusLabel.setText(" ");
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(new Color(220, 53, 69));
            statusLabel.setText("Please enter both username and password.");
            return;
        }

        loginButton.setEnabled(false);
        progressBar.setVisible(true);
        statusLabel.setForeground(new Color(13, 110, 253));
        statusLabel.setText("Authenticating credentials...");

        // Perform authentication in background worker thread to keep Swing UI snappy
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override
            protected User doInBackground() {
                return authController.login(username, password);
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                progressBar.setVisible(false);
                try {
                    User user = get();
                    statusLabel.setForeground(new Color(25, 135, 84));
                    statusLabel.setText("Login successful! Welcome, " + user.getFullName());

                    // Open Dashboard
                    openDashboard(user);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    statusLabel.setForeground(new Color(220, 53, 69));
                    statusLabel.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openDashboard(User user) {
        // Will launch DashboardFrame in Step 2/subsequent steps
        SwingUtilities.invokeLater(() -> {
            try {
                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Authenticated as " + user.getFullName() + " (" + user.getRole().getDisplayName() + ")\n" +
                                "Dashboard is launching.",
                        "SmartCare Login Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }
}
