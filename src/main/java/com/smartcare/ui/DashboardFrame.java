package com.smartcare.ui;

import com.smartcare.controller.AppointmentController;
import com.smartcare.dao.impl.MedicineDAOImpl;
import com.smartcare.dao.impl.PatientDAOImpl;
import com.smartcare.dao.impl.DoctorDAOImpl;
import com.smartcare.enums.UserRole;
import com.smartcare.model.User;
import com.smartcare.security.UserSession;
import com.smartcare.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.format.DateTimeFormatter;

/**
 * Main Application Dashboard Frame for SmartCare Hospital System.
 * Implements dynamic Role-Based Navigation and wires all module panels.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel headerTitleLabel;

    // Live metric labels updated on dashboard load
    private JLabel metricPatients;
    private JLabel metricDoctors;
    private JLabel metricAppointments;
    private JLabel metricBeds;
    private JLabel metricMedicines;
    private JLabel metricAI;

    public DashboardFrame() {
        this.currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null)
            throw new IllegalStateException("Cannot open dashboard without an active user session.");
        initComponents();
    }

    private void initComponents() {
        setTitle("SmartCare Hospital Management System  ⚕  [" + currentUser.getRole().getDisplayName() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1060, 680));
        setLayout(new BorderLayout());

        add(createHeaderPanel(),  BorderLayout.NORTH);
        add(createSidebarPanel(), BorderLayout.WEST);

        // ── CARD LAYOUT CONTENT AREA ─────────────────────────────────────────
        cardLayout  = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(new Color(245, 247, 250));

        contentArea.add(createWelcomePanel(),        "WELCOME");
        contentArea.add(new PatientPanel(this),      "PATIENTS");
        contentArea.add(new DoctorPanel(this),       "DOCTORS");
        contentArea.add(new AppointmentPanel(this),  "APPOINTMENTS");
        contentArea.add(new MedicalRecordPanel(this),"MEDICAL_RECORDS");
        contentArea.add(new BillingPanel(this),      "BILLING");
        contentArea.add(new PharmacyPanel(this),     "PHARMACY");
        contentArea.add(new AICopilotPanel(this),    "AI_COPILOT");

        add(contentArea, BorderLayout.CENTER);

        // Load live metrics in background
        loadDashboardMetrics();
    }

    // ── HEADER ────────────────────────────────────────────────────────────────

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 43, 73));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logo = new JLabel("⚕  SmartCare");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logo.setForeground(Color.WHITE);

        headerTitleLabel = new JLabel("|  Hospital Management Portal");
        headerTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerTitleLabel.setForeground(new Color(180, 205, 237));

        brandPanel.add(logo);
        brandPanel.add(headerTitleLabel);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        JLabel roleBadge = new JLabel("  " + currentUser.getRole().getDisplayName().toUpperCase() + "  ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBackground(new Color(13, 110, 253));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        JLabel userNameLabel = new JLabel("👤  " + currentUser.getFullName());
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userNameLabel.setForeground(Color.WHITE);

        JButton logoutButton = new JButton("Sign Out");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(new EmptyBorder(5, 14, 5, 14));
        logoutButton.addActionListener(e -> performLogout());

        userPanel.add(roleBadge);
        userPanel.add(userNameLabel);
        userPanel.add(logoutButton);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel,  BorderLayout.EAST);
        return header;
    }

    // ── SIDEBAR ───────────────────────────────────────────────────────────────

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(28, 32, 42));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(12, 8, 12, 8));

        UserRole role = currentUser.getRole();

        addSectionLabel(sidebar, "CORE NAVIGATION");
        addNavButton(sidebar, "📊  Dashboard Home",
                () -> showCard("WELCOME", "Dashboard Overview"));

        if (role == UserRole.ADMIN || role == UserRole.RECEPTIONIST) {
            addSectionLabel(sidebar, "PATIENT SERVICES");
            addNavButton(sidebar, "👥  Patient Directory",
                    () -> showCard("PATIENTS", "Patient Directory"));
            addNavButton(sidebar, "📅  Appointments",
                    () -> showCard("APPOINTMENTS", "Appointment Management"));
            addNavButton(sidebar, "💳  Billing & Invoices",
                    () -> showCard("BILLING", "Billing & Invoices"));
        }

        if (role == UserRole.ADMIN || role == UserRole.DOCTOR) {
            addSectionLabel(sidebar, "CLINICAL MODULES");
            addNavButton(sidebar, "🩺  Doctors Directory",
                    () -> showCard("DOCTORS", "Doctor Management"));
            addNavButton(sidebar, "📋  Medical Records",
                    () -> showCard("MEDICAL_RECORDS", "Medical Records"));
        }

        if (role == UserRole.ADMIN || role == UserRole.PHARMACIST) {
            addSectionLabel(sidebar, "PHARMACY");
            addNavButton(sidebar, "🏪  Pharmacy Stock",
                    () -> showCard("PHARMACY", "Pharmacy Stock Management"));
        }

        if (role == UserRole.ADMIN || role == UserRole.LAB_TECHNICIAN) {
            addSectionLabel(sidebar, "LABORATORY");
            addNavButton(sidebar, "🔬  Laboratory Tests",
                    () -> showPlaceholder("Laboratory Management",
                            "🔬", "Lab test ordering and results tracking",
                            "Future milestone — planned for next sprint."));
        }

        if (role == UserRole.ADMIN) {
            addSectionLabel(sidebar, "ADMINISTRATION");
            addNavButton(sidebar, "📈  Analytics & Reports",
                    () -> showPlaceholder("Analytics & Reports",
                            "📈", "Hospital analytics and statistical reports",
                            "Future milestone — planned for next sprint."));
            addNavButton(sidebar, "⚙  User Accounts",
                    () -> showPlaceholder("User Management",
                            "⚙", "Manage staff accounts and access roles",
                            "Future milestone — planned for next sprint."));
        }

        // AI Copilot — always visible at bottom
        sidebar.add(Box.createVerticalGlue());
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(55, 65, 80));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));
        addNavButton(sidebar, "✨  AI Hospital Copilot",
                () -> showCard("AI_COPILOT", "SmartCare AI Hospital Copilot"));

        return sidebar;
    }

    private void addSectionLabel(JPanel sidebar, String text) {
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(100, 116, 139));
        lbl.setBorder(new EmptyBorder(0, 10, 2, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lbl);
    }

    private void addNavButton(JPanel sidebar, String title, Runnable onClick) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setBackground(new Color(38, 44, 58));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(13, 110, 253));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(38, 44, 58));
                btn.setForeground(new Color(203, 213, 225));
            }
        });
        btn.addActionListener(e -> onClick.run());

        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    // ── WELCOME PANEL ─────────────────────────────────────────────────────────

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Header
        JPanel headerSection = new JPanel();
        headerSection.setOpaque(false);
        headerSection.setLayout(new BoxLayout(headerSection, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back, " + currentUser.getFullName() + "  👋");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(new Color(33, 37, 41));

        String loginTimeStr = UserSession.getInstance().getLoginTime() != null
                ? UserSession.getInstance().getLoginTime().format(DateTimeFormatter.ofPattern("hh:mm a, dd MMM yyyy"))
                : "Active Session";

        JLabel sub = new JLabel("Logged in as " + currentUser.getRole().getDisplayName()
                + "   •   Session started: " + loginTimeStr);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(new Color(108, 117, 125));

        headerSection.add(title);
        headerSection.add(Box.createRigidArea(new Dimension(0, 6)));
        headerSection.add(sub);
        headerSection.add(Box.createRigidArea(new Dimension(0, 22)));

        // Metric Cards
        JPanel metricsGrid = new JPanel(new GridLayout(2, 3, 14, 14));
        metricsGrid.setOpaque(false);

        metricPatients     = new JLabel("Loading...");
        metricDoctors      = new JLabel("Loading...");
        metricAppointments = new JLabel("Loading...");
        metricBeds         = new JLabel("Loading...");
        metricMedicines    = new JLabel("Loading...");
        metricAI           = new JLabel("✅  Ready");

        metricsGrid.add(createMetricCard("👥  Total Patients",     metricPatients,     new Color(13, 110, 253)));
        metricsGrid.add(createMetricCard("🩺  Active Doctors",     metricDoctors,      new Color(25, 135, 84)));
        metricsGrid.add(createMetricCard("📅  Appointments Today", metricAppointments, new Color(13, 202, 240)));
        metricsGrid.add(createMetricCard("💊  Medicines in Stock", metricMedicines,    new Color(253, 126, 20)));
        metricsGrid.add(createMetricCard("💳  Pending Bills",      metricBeds,         new Color(255, 193, 7)));
        metricsGrid.add(createMetricCard("✨  AI Copilot",         metricAI,           new Color(32, 201, 151)));

        // Quick access
        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quickPanel.setOpaque(false);
        quickPanel.setBorder(new EmptyBorder(18, 0, 0, 0));

        JLabel quickLabel = new JLabel("Quick Access: ");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        quickPanel.add(quickLabel);
        addQuickBtn(quickPanel, "📅 New Appointment", () -> showCard("APPOINTMENTS", "Appointment Management"));
        addQuickBtn(quickPanel, "👥 Add Patient",     () -> showCard("PATIENTS",     "Patient Directory"));
        addQuickBtn(quickPanel, "✨ Ask AI Copilot",  () -> showCard("AI_COPILOT",   "SmartCare AI Hospital Copilot"));

        panel.add(headerSection, BorderLayout.NORTH);
        panel.add(metricsGrid,   BorderLayout.CENTER);
        panel.add(quickPanel,    BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLbl.setForeground(new Color(108, 117, 125));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(33, 37, 41));

        card.add(titleLbl,   BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void addQuickBtn(JPanel panel, String text, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(33, 37, 41));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 212, 218)),
                new EmptyBorder(7, 14, 7, 14)));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(13, 110, 253));
                btn.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(33, 37, 41));
            }
        });
        btn.addActionListener(e -> action.run());
        panel.add(btn);
    }

    // ── LIVE METRICS LOADER ───────────────────────────────────────────────────

    private void loadDashboardMetrics() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int[] metrics = new int[5]; // [patients, doctors, todayAppts, medicines, pendingBills]
                try (Connection conn = DatabaseConnection.getConnection()) {
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM patients WHERE is_active=1")) {
                        if (rs.next()) metrics[0] = rs.getInt(1);
                    }
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM doctors WHERE is_available=1")) {
                        if (rs.next()) metrics[1] = rs.getInt(1);
                    }
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM appointments WHERE DATE(appointment_date)=CURDATE()")) {
                        if (rs.next()) metrics[2] = rs.getInt(1);
                    }
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM medicines")) {
                        if (rs.next()) metrics[3] = rs.getInt(1);
                    }
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM bills WHERE payment_status='UNPAID'")) {
                        if (rs.next()) metrics[4] = rs.getInt(1);
                    }
                } catch (Exception e) {
                    // Non-fatal: dashboard still renders with defaults
                }
                return metrics;
            }

            @Override
            protected void done() {
                try {
                    int[] m = get();
                    metricPatients.setText(m[0] + " Registered");
                    metricDoctors.setText(m[1] + " On Staff");
                    metricAppointments.setText(m[2] + " Scheduled");
                    metricMedicines.setText(m[3] + " Items");
                    metricBeds.setText(m[4] + " Bills Pending");
                    metricAI.setText("✅  Ready (Gemini)");
                } catch (Exception e) {
                    metricPatients.setText("N/A");
                }
            }
        }.execute();
    }

    // ── NAVIGATION ────────────────────────────────────────────────────────────

    public void showCard(String cardName) {
        showCard(cardName, cardName);
    }

    public void showCard(String cardName, String title) {
        cardLayout.show(contentArea, cardName);
        headerTitleLabel.setText("|  " + title);
    }

    public void showPlaceholder(String moduleName, String icon, String description, String note) {
        String key = "PH_" + moduleName.replaceAll("\\s+", "_");

        // Check if already added to avoid duplicates
        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBackground(new Color(245, 247, 250));
        placeholder.setBorder(new EmptyBorder(60, 60, 60, 60));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                new EmptyBorder(40, 50, 40, 50)));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(moduleName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(33, 37, 41));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descLabel.setForeground(new Color(108, 117, 125));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        noteLabel.setForeground(new Color(150, 160, 175));
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(descLabel);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(noteLabel);

        placeholder.add(card, BorderLayout.CENTER);
        contentArea.add(placeholder, key);
        cardLayout.show(contentArea, key);
        headerTitleLabel.setText("|  " + moduleName);
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to sign out?",
                "Sign Out Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            UserSession.getInstance().clearSession();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
}
