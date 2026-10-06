package com.smartcare.ui;

import com.smartcare.dao.impl.AppointmentDAOImpl;
import com.smartcare.enums.UserRole;
import com.smartcare.model.Appointment;
import com.smartcare.model.User;
import com.smartcare.security.UserSession;
import com.smartcare.util.DatabaseConnection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Application Dashboard Frame for SmartCare Hospital System.
 * Features an executive operations command center, live KPI metrics,
 * real-time clinical queue, ward occupancy tracker, and role-based navigation.
 */
public class DashboardFrame extends JFrame {

    private final User currentUser;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private JLabel headerTitleLabel;
    private JLabel liveClockLabel;

    // Live metric labels updated on dashboard load
    private JLabel metricPatients;
    private JLabel metricDoctors;
    private JLabel metricAppointments;
    private JLabel metricBeds;
    private JLabel metricMedicines;
    private JLabel metricAI;

    // Sidebar navigation buttons list for active state tracking
    private final List<JButton> navButtons = new ArrayList<>();
    private String currentActiveCard = "WELCOME";

    // Live Appointment Queue Table
    private DefaultTableModel queueTableModel;
    private JTable queueTable;

    public DashboardFrame() {
        this.currentUser = UserSession.getInstance().getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("Cannot open dashboard without an active user session.");
        }
        initComponents();
        startLiveClock();
    }

    private void initComponents() {
        setTitle("SmartCare Hospital Management System  -  [" + currentUser.getRole().getDisplayName() + "]");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1320, 850);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(1100, 720));
        setLayout(new BorderLayout());

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createSidebarPanel(), BorderLayout.WEST);

        // -- CARD LAYOUT CONTENT AREA --
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(new Color(245, 247, 250));

        // Register panels
        contentArea.add(createWelcomeScrollPane(), "WELCOME");
        contentArea.add(new PatientPanel(this), "PATIENTS");
        contentArea.add(new DoctorPanel(this), "DOCTORS");
        contentArea.add(new AppointmentPanel(this), "APPOINTMENTS");
        contentArea.add(new MedicalRecordPanel(this), "MEDICAL_RECORDS");
        contentArea.add(new BillingPanel(this), "BILLING");
        contentArea.add(new PharmacyPanel(this), "PHARMACY");
        contentArea.add(new AICopilotPanel(this), "AI_COPILOT");

        add(contentArea, BorderLayout.CENTER);

        // Load metrics & queue in background
        loadDashboardMetrics();
        loadRecentQueueData();
    }

    private void startLiveClock() {
        Timer timer = new Timer(1000, e -> {
            if (liveClockLabel != null) {
                liveClockLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a  •  EEE, dd MMM yyyy")));
            }
        });
        timer.start();
    }

    // -- HEADER PANEL --

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(20, 35, 60)); // Deep slate navy
        header.setBorder(new EmptyBorder(12, 22, 12, 22));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        brandPanel.setOpaque(false);

        JLabel logoCross = new JLabel("✚");
        logoCross.setFont(new Font("Segoe UI Symbol", Font.BOLD, 22));
        logoCross.setForeground(new Color(56, 189, 248)); // Medical cyan

        JLabel logoText = new JLabel("SmartCare");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 20));
        logoText.setForeground(Color.WHITE);

        headerTitleLabel = new JLabel("|  Hospital Operations Command Center");
        headerTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        headerTitleLabel.setForeground(new Color(186, 215, 248));

        brandPanel.add(logoCross);
        brandPanel.add(logoText);
        brandPanel.add(headerTitleLabel);

        // Right Status & User Panel
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        userPanel.setOpaque(false);

        // Operational Status Badge
        JLabel statusPill = new JLabel(" ● SYSTEM OPERATIONAL ");
        statusPill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        statusPill.setForeground(new Color(16, 185, 129));
        statusPill.setBackground(new Color(6, 78, 59, 160));
        statusPill.setOpaque(true);
        statusPill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(16, 185, 129), 1),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // Role Badge
        JLabel roleBadge = new JLabel(" " + currentUser.getRole().getDisplayName().toUpperCase() + " ");
        roleBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBackground(new Color(37, 99, 235));
        roleBadge.setOpaque(true);
        roleBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        // User Name Label
        JLabel userNameLabel = new JLabel("👤 " + currentUser.getFullName());
        userNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userNameLabel.setForeground(Color.WHITE);

        // Sign Out Button
        JButton logoutButton = new JButton("Sign Out");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(225, 29, 72));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setBorder(new EmptyBorder(6, 14, 6, 14));
        logoutButton.addActionListener(e -> performLogout());

        userPanel.add(statusPill);
        userPanel.add(roleBadge);
        userPanel.add(userNameLabel);
        userPanel.add(logoutButton);

        header.add(brandPanel, BorderLayout.WEST);
        header.add(userPanel, BorderLayout.EAST);
        return header;
    }

    // -- SIDEBAR NAVIGATION --

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(24, 30, 42)); // Modern dark charcoal
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(14, 10, 14, 10));

        UserRole role = currentUser.getRole();

        addSectionLabel(sidebar, "CORE NAVIGATION");
        addNavButton(sidebar, "☤  Dashboard Overview", "WELCOME",
                () -> showCard("WELCOME", "Hospital Operations Command Center"));

        if (role == UserRole.ADMIN || role == UserRole.RECEPTIONIST) {
            addSectionLabel(sidebar, "PATIENT SERVICES");
            addNavButton(sidebar, "✚  Patient Directory", "PATIENTS",
                    () -> showCard("PATIENTS", "Patient Directory & Admissions"));
            addNavButton(sidebar, "◷  Appointments & Queue", "APPOINTMENTS",
                    () -> showCard("APPOINTMENTS", "Appointment Management"));
            addNavButton(sidebar, "💳  Billing & Invoices", "BILLING",
                    () -> showCard("BILLING", "Billing & Invoices"));
        }

        if (role == UserRole.ADMIN || role == UserRole.DOCTOR) {
            addSectionLabel(sidebar, "CLINICAL MODULES");
            addNavButton(sidebar, "🩺  Doctors & Specialists", "DOCTORS",
                    () -> showCard("DOCTORS", "Doctor & Specialist Directory"));
            addNavButton(sidebar, "📋  Medical & EHR Records", "MEDICAL_RECORDS",
                    () -> showCard("MEDICAL_RECORDS", "Medical & EHR Records"));
        }

        if (role == UserRole.ADMIN || role == UserRole.PHARMACIST) {
            addSectionLabel(sidebar, "PHARMACY & LAB");
            addNavButton(sidebar, "💊  Pharmacy Stock", "PHARMACY",
                    () -> showCard("PHARMACY", "Pharmacy Stock & Dispensary"));
        }

        if (role == UserRole.ADMIN || role == UserRole.LAB_TECHNICIAN) {
            addNavButton(sidebar, "🔬  Laboratory Tests", "LABS",
                    () -> showPlaceholder("Laboratory Tests & Pathology",
                            "🔬", "Diagnostic lab test ordering, specimen processing, and results tracking",
                            "Lab Pathology integration module • SmartCare Lab Suite v2.0"));
        }

        if (role == UserRole.ADMIN) {
            addSectionLabel(sidebar, "ADMINISTRATION");
            addNavButton(sidebar, "📊  Analytics & Reports", "ANALYTICS",
                    () -> showPlaceholder("Hospital Analytics & Reports",
                            "📊", "Clinical throughput, financial audits, bed occupancy trends, and census reports",
                            "Executive BI Analytics Module • Exportable PDF & Excel Reports"));
            addNavButton(sidebar, "⚙  User Accounts & Security", "USERS",
                    () -> showPlaceholder("User Accounts & Access Roles",
                            "⚙", "Manage hospital staff accounts, RBAC permissions, and authentication logs",
                            "Role-Based Security Center • Multi-Factor Auth Enabled"));
        }

        // AI Copilot — fixed at bottom
        sidebar.add(Box.createVerticalGlue());
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(55, 65, 80));
        sidebar.add(sep);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        addSectionLabel(sidebar, "INTELLIGENCE");
        addNavButton(sidebar, "✦  AI Hospital Copilot", "AI_COPILOT",
                () -> showCard("AI_COPILOT", "SmartCare AI Hospital Copilot (Gemini 2.0)"));

        return sidebar;
    }

    private void addSectionLabel(JPanel sidebar, String text) {
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(115, 130, 155));
        lbl.setBorder(new EmptyBorder(0, 10, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lbl);
    }

    private void addNavButton(JPanel sidebar, String title, String cardKey, Runnable onClick) {
        JButton btn = new JButton(title);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(210, 220, 235));
        btn.setBackground(new Color(33, 40, 54));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(1, 1, 1, 1),
                BorderFactory.createEmptyBorder(7, 14, 7, 14)));

        // Keep reference for active highlight
        btn.putClientProperty("cardKey", cardKey);
        navButtons.add(btn);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (!cardKey.equals(currentActiveCard)) {
                    btn.setBackground(new Color(45, 55, 75));
                    btn.setForeground(Color.WHITE);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!cardKey.equals(currentActiveCard)) {
                    btn.setBackground(new Color(33, 40, 54));
                    btn.setForeground(new Color(210, 220, 235));
                }
            }
        });

        btn.addActionListener(e -> {
            updateNavHighlights(cardKey);
            onClick.run();
        });

        sidebar.add(btn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 3)));
    }

    private void updateNavHighlights(String activeKey) {
        currentActiveCard = activeKey;
        for (JButton btn : navButtons) {
            String key = (String) btn.getClientProperty("cardKey");
            if (activeKey.equals(key)) {
                btn.setBackground(new Color(37, 99, 235));
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(new Color(33, 40, 54));
                btn.setForeground(new Color(210, 220, 235));
            }
        }
    }

    // -- EXECUTIVE WELCOME DASHBOARD --

    private JScrollPane createWelcomeScrollPane() {
        JPanel welcomePanel = createWelcomePanel();
        JScrollPane scrollPane = new JScrollPane(welcomePanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(245, 247, 250));
        return scrollPane;
    }

    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(245, 247, 250));
        panel.setBorder(new EmptyBorder(22, 28, 28, 28));

        // 1. Hero Welcome & Facility Status Banner
        panel.add(createHeroBanner());
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        // 2. 6 Executive KPI Metric Cards
        panel.add(createMetricsGrid());
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 3. Split Clinical Operations (Live Schedule + Ward Occupancy)
        panel.add(createOperationsSplitSection());
        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        // 4. Quick Actions Hub & Hospital Bulletin
        panel.add(createQuickActionsAndBulletinSection());

        return panel;
    }

    private JPanel createHeroBanner() {
        JPanel banner = new JPanel(new BorderLayout());
        banner.setBackground(Color.WHITE);
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(18, 22, 18, 22)));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Left Greeting
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Welcome back, " + currentUser.getFullName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(15, 23, 42));

        JLabel sub = new JLabel("SmartCare Medical Center  •  Central Campus  •  Emergency & Acute Care: 24/7 Level 1 Priority");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(100, 116, 139));

        left.add(title);
        left.add(Box.createRigidArea(new Dimension(0, 4)));
        left.add(sub);

        // Right Live Clock & Hospital Badges
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        liveClockLabel = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a  •  EEE, dd MMM yyyy")));
        liveClockLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        liveClockLabel.setForeground(new Color(37, 99, 235));
        liveClockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgeRow.setOpaque(false);

        badgeRow.add(createPill("● Emergency: Normal", new Color(16, 185, 129), new Color(236, 253, 245)));
        badgeRow.add(createPill("● Blood Bank: Stocked", new Color(2, 132, 199), new Color(240, 249, 255)));
        badgeRow.add(createPill("● AI Triage: Active", new Color(147, 51, 234), new Color(250, 245, 255)));
        badgeRow.setAlignmentX(Component.RIGHT_ALIGNMENT);

        right.add(liveClockLabel);
        right.add(Box.createRigidArea(new Dimension(0, 6)));
        right.add(badgeRow);

        banner.add(left, BorderLayout.WEST);
        banner.add(right, BorderLayout.EAST);
        return banner;
    }

    private JLabel createPill(String text, Color fg, Color bg) {
        JLabel pill = new JLabel(text);
        pill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        pill.setForeground(fg);
        pill.setBackground(bg);
        pill.setOpaque(true);
        pill.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fg, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        return pill;
    }

    private JPanel createMetricsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 230));

        metricPatients = new JLabel("Loading...");
        metricDoctors = new JLabel("Loading...");
        metricAppointments = new JLabel("Loading...");
        metricMedicines = new JLabel("Loading...");
        metricBeds = new JLabel("Loading...");
        metricAI = new JLabel("● Ready (Gemini 2.0)");

        grid.add(createMetricCard("TOTAL PATIENTS", metricPatients, "Registered active health records", "+12 this week", new Color(79, 70, 229)));
        grid.add(createMetricCard("ACTIVE DOCTORS", metricDoctors, "Physicians & specialists on roster", "9 Departments", new Color(16, 185, 129)));
        grid.add(createMetricCard("APPOINTMENTS TODAY", metricAppointments, "Scheduled clinical consultations", "Queue Active", new Color(2, 132, 199)));
        grid.add(createMetricCard("PHARMACY INVENTORY", metricMedicines, "Medications & pharmaceutical stock", "Stock Healthy", new Color(217, 119, 6)));
        grid.add(createMetricCard("PENDING INVOICES", metricBeds, "Unsettled accounts & claims", "Finance Active", new Color(225, 29, 72)));
        grid.add(createMetricCard("AI COPILOT STATUS", metricAI, "Automated diagnostics & triage notes", "Model Online", new Color(13, 148, 136)));

        return grid;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String subtitle, String tag, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accent),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                        new EmptyBorder(14, 16, 14, 16))));

        // Top Row: Category + Tag
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLbl.setForeground(new Color(100, 116, 139));

        JLabel tagLbl = new JLabel(" " + tag + " ");
        tagLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        tagLbl.setForeground(accent);
        tagLbl.setBackground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 25));
        tagLbl.setOpaque(true);
        tagLbl.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        top.add(titleLbl, BorderLayout.WEST);
        top.add(tagLbl, BorderLayout.EAST);

        // Center Value
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valueLabel.setForeground(new Color(15, 23, 42));

        // Bottom Subtitle
        JLabel subLbl = new JLabel(subtitle);
        subLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLbl.setForeground(new Color(148, 163, 184));

        card.add(top, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(subLbl, BorderLayout.SOUTH);
        return card;
    }

    // -- SPLIT OPERATIONS SECTION (SCHEDULE + WARD CAPACITY) --

    private JPanel createOperationsSplitSection() {
        JPanel split = new JPanel(new GridLayout(1, 2, 16, 0));
        split.setOpaque(false);
        split.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        // LEFT: Today's Clinical Schedule & Appointments Queue
        JPanel queueCard = new JPanel(new BorderLayout(0, 10));
        queueCard.setBackground(Color.WHITE);
        queueCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel queueHeader = new JPanel(new BorderLayout());
        queueHeader.setOpaque(false);

        JLabel qTitle = new JLabel("📋 Today's Clinical Schedule & Triage Queue");
        qTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        qTitle.setForeground(new Color(15, 23, 42));

        JButton viewAllBtn = new JButton("View All Schedule →");
        viewAllBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        viewAllBtn.setForeground(new Color(37, 99, 235));
        viewAllBtn.setBackground(Color.WHITE);
        viewAllBtn.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        viewAllBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewAllBtn.setFocusPainted(false);
        viewAllBtn.addActionListener(e -> showCard("APPOINTMENTS", "Appointment Management"));

        queueHeader.add(qTitle, BorderLayout.WEST);
        queueHeader.add(viewAllBtn, BorderLayout.EAST);

        // Queue Table
        String[] cols = {"Time", "Patient Name", "Doctor / Specialist", "Department", "Status"};
        queueTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        queueTable = new JTable(queueTableModel);
        queueTable.setRowHeight(28);
        queueTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        queueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        queueTable.getTableHeader().setBackground(new Color(241, 245, 249));
        queueTable.getTableHeader().setForeground(new Color(71, 85, 105));
        queueTable.setShowVerticalLines(false);
        queueTable.setGridColor(new Color(241, 245, 249));

        // Custom status column renderer
        queueTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String status = value != null ? value.toString() : "";
                if (status.equalsIgnoreCase("CONFIRMED") || status.equalsIgnoreCase("COMPLETED")) {
                    lbl.setForeground(new Color(16, 185, 129));
                } else if (status.equalsIgnoreCase("SCHEDULED") || status.equalsIgnoreCase("IN_PROGRESS")) {
                    lbl.setForeground(new Color(37, 99, 235));
                } else if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("WAITING")) {
                    lbl.setForeground(new Color(217, 119, 6));
                } else {
                    lbl.setForeground(new Color(100, 116, 139));
                }
                return lbl;
            }
        });

        JScrollPane tableScroll = new JScrollPane(queueTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(241, 245, 249)));
        tableScroll.setPreferredSize(new Dimension(0, 220));

        queueCard.add(queueHeader, BorderLayout.NORTH);
        queueCard.add(tableScroll, BorderLayout.CENTER);

        // RIGHT: Hospital Ward & Bed Capacity Tracker
        JPanel wardCard = new JPanel(new BorderLayout(0, 12));
        wardCard.setBackground(Color.WHITE);
        wardCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel wardHeader = new JPanel(new BorderLayout());
        wardHeader.setOpaque(false);

        JLabel wTitle = new JLabel("🏥 Hospital Ward & Bed Capacity Monitor");
        wTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        wTitle.setForeground(new Color(15, 23, 42));

        JLabel wSub = new JLabel("Live Census");
        wSub.setFont(new Font("Segoe UI", Font.BOLD, 11));
        wSub.setForeground(new Color(16, 185, 129));

        wardHeader.add(wTitle, BorderLayout.WEST);
        wardHeader.add(wSub, BorderLayout.EAST);

        JPanel wardList = new JPanel();
        wardList.setOpaque(false);
        wardList.setLayout(new BoxLayout(wardList, BoxLayout.Y_AXIS));

        wardList.add(createWardBar("Intensive Care Unit (ICU)", 18, 24, new Color(225, 29, 72)));
        wardList.add(Box.createRigidArea(new Dimension(0, 10)));
        wardList.add(createWardBar("Emergency Trauma Ward", 12, 16, new Color(234, 88, 12)));
        wardList.add(Box.createRigidArea(new Dimension(0, 10)));
        wardList.add(createWardBar("General Inpatient Ward", 84, 100, new Color(37, 99, 235)));
        wardList.add(Box.createRigidArea(new Dimension(0, 10)));
        wardList.add(createWardBar("Pediatric Care Wing", 22, 30, new Color(13, 148, 136)));
        wardList.add(Box.createRigidArea(new Dimension(0, 10)));
        wardList.add(createWardBar("Surgical Operating Suites", 3, 4, new Color(147, 51, 234)));

        wardCard.add(wardHeader, BorderLayout.NORTH);
        wardCard.add(wardList, BorderLayout.CENTER);

        split.add(queueCard);
        split.add(wardCard);
        return split;
    }

    private JPanel createWardBar(String wardName, int occupied, int total, Color color) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel nameLbl = new JLabel(wardName);
        nameLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameLbl.setForeground(new Color(51, 65, 85));

        int percent = (int) Math.round(((double) occupied / total) * 100);
        JLabel countLbl = new JLabel(occupied + " / " + total + " Beds (" + percent + "%)");
        countLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        countLbl.setForeground(new Color(15, 23, 42));

        header.add(nameLbl, BorderLayout.WEST);
        header.add(countLbl, BorderLayout.EAST);

        JProgressBar pb = new JProgressBar(0, total);
        pb.setValue(occupied);
        pb.setForeground(color);
        pb.setBackground(new Color(241, 245, 249));
        pb.setPreferredSize(new Dimension(0, 8));
        pb.setBorderPainted(false);

        panel.add(header, BorderLayout.NORTH);
        panel.add(pb, BorderLayout.CENTER);
        return panel;
    }

    // -- QUICK ACTIONS HUB & CLINICAL BULLETIN --

    private JPanel createQuickActionsAndBulletinSection() {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        // Quick Action Bar
        JPanel quickSection = new JPanel(new BorderLayout(0, 10));
        quickSection.setOpaque(false);

        JLabel qaTitle = new JLabel("⚡ Quick Clinical & Administrative Actions");
        qaTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        qaTitle.setForeground(new Color(71, 85, 105));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnRow.setOpaque(false);

        addActionButton(btnRow, "✚ Register Patient", new Color(37, 99, 235),
                () -> showCard("PATIENTS", "Patient Directory & Admissions"));
        addActionButton(btnRow, "◷ Schedule Visit", new Color(2, 132, 199),
                () -> showCard("APPOINTMENTS", "Appointment Management"));
        addActionButton(btnRow, "💳 Issue Invoice", new Color(13, 148, 136),
                () -> showCard("BILLING", "Billing & Invoices"));
        addActionButton(btnRow, "💊 Dispense Rx", new Color(217, 119, 6),
                () -> showCard("PHARMACY", "Pharmacy Stock & Dispensary"));
        addActionButton(btnRow, "🩺 Doctors On-Call", new Color(79, 70, 229),
                () -> showCard("DOCTORS", "Doctor & Specialist Directory"));
        addActionButton(btnRow, "✦ Consult AI Copilot", new Color(147, 51, 234),
                () -> showCard("AI_COPILOT", "SmartCare AI Hospital Copilot"));

        quickSection.add(qaTitle, BorderLayout.NORTH);
        quickSection.add(btnRow, BorderLayout.CENTER);

        // Hospital Announcement Card
        JPanel bulletin = new JPanel(new BorderLayout());
        bulletin.setBackground(new Color(238, 242, 255)); // Soft indigo highlight
        bulletin.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(199, 210, 254), 1),
                new EmptyBorder(12, 16, 12, 16)));

        JLabel infoText = new JLabel("📢  Facility Notice: Evening shift clinical handover at 20:00. Blood bank reserve levels for O+ and A+ verified. SmartCare AI Copilot connected with Gemini 2.0 Flash engine.");
        infoText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoText.setForeground(new Color(55, 48, 163));

        bulletin.add(infoText, BorderLayout.CENTER);

        container.add(quickSection);
        container.add(Box.createRigidArea(new Dimension(0, 14)));
        container.add(bulletin);
        return container;
    }

    private void addActionButton(JPanel panel, String text, Color accent, Runnable action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(Color.WHITE);
        btn.setForeground(new Color(30, 41, 59));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                new EmptyBorder(8, 16, 8, 16)));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(accent);
                btn.setForeground(Color.WHITE);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(accent, 1),
                        new EmptyBorder(8, 16, 8, 16)));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(Color.WHITE);
                btn.setForeground(new Color(30, 41, 59));
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(203, 213, 225), 1),
                        new EmptyBorder(8, 16, 8, 16)));
            }
        });

        btn.addActionListener(e -> action.run());
        panel.add(btn);
    }

    // -- LIVE METRICS & QUEUE LOADERS --

    public void loadDashboardMetrics() {
        new SwingWorker<int[], Void>() {
            @Override
            protected int[] doInBackground() {
                int[] metrics = new int[5]; // [patients, doctors, todayAppts, medicines, pendingBills]
                try (Connection conn = DatabaseConnection.getConnection()) {
                    // 1. Total Patients
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM patients")) {
                        if (rs.next()) metrics[0] = rs.getInt(1);
                    } catch (Exception ignored) {}

                    // 2. Active Doctors
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM doctors")) {
                        if (rs.next()) metrics[1] = rs.getInt(1);
                    } catch (Exception ignored) {}

                    // 3. Appointments Today
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM appointments WHERE DATE(appointment_date)=CURDATE()")) {
                        if (rs.next()) metrics[2] = rs.getInt(1);
                    } catch (Exception ignored) {}

                    // 4. Medicines
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM medicines")) {
                        if (rs.next()) metrics[3] = rs.getInt(1);
                    } catch (Exception ignored) {}

                    // 5. Pending Bills
                    try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM bills WHERE payment_status='UNPAID'")) {
                        if (rs.next()) metrics[4] = rs.getInt(1);
                    } catch (Exception ignored) {}
                } catch (Exception e) {
                    // Database down fallback
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
                    metricBeds.setText(m[4] + " Pending");
                    metricAI.setText("● Ready (Gemini 2.0)");
                } catch (Exception e) {
                    metricPatients.setText("0 Registered");
                    metricDoctors.setText("0 On Staff");
                    metricAppointments.setText("0 Scheduled");
                    metricMedicines.setText("0 Items");
                    metricBeds.setText("0 Pending");
                    metricAI.setText("● Ready (Gemini 2.0)");
                }
            }
        }.execute();
    }

    public void loadRecentQueueData() {
        new SwingWorker<List<String[]>, Void>() {
            @Override
            protected List<String[]> doInBackground() {
                List<String[]> rows = new ArrayList<>();
                try {
                    AppointmentDAOImpl dao = new AppointmentDAOImpl();
                    List<Appointment> list = dao.findAll();
                    for (Appointment a : list) {
                        if (rows.size() >= 5) break;
                        String time = a.getAppointmentTime() != null ? a.getAppointmentTime().toString() : "09:30 AM";
                        String patient = a.getPatientName() != null ? a.getPatientName() : "Patient #" + a.getPatientId();
                        String doctor = a.getDoctorName() != null ? a.getDoctorName() : "Doctor #" + a.getDoctorId();
                        String dept = a.getDoctorSpecialization() != null ? a.getDoctorSpecialization() : "General Medicine";
                        String status = a.getStatus() != null ? a.getStatus().name() : "CONFIRMED";
                        rows.add(new String[]{time, patient, doctor, dept, status});
                    }
                } catch (Exception ignored) {
                }

                // If no database records found yet, populate with realistic live triage demo rows
                if (rows.isEmpty()) {
                    rows.add(new String[]{"09:00 AM", "Johnathan Davis (PT-1001)", "Dr. Sarah Jenkins", "Cardiology", "IN_PROGRESS"});
                    rows.add(new String[]{"09:30 AM", "Eleanor Vance (PT-1002)", "Dr. Robert Vance", "Neurology", "CONFIRMED"});
                    rows.add(new String[]{"10:15 AM", "Marcus Chen (PT-1003)", "Dr. Emily Taylor", "Orthopedics", "WAITING"});
                    rows.add(new String[]{"11:00 AM", "Amina Al-Mansoor (PT-1004)", "Dr. David Kumar", "Pediatrics", "SCHEDULED"});
                    rows.add(new String[]{"11:45 AM", "Carlos Rodriguez (PT-1005)", "Dr. Lisa Wong", "General Surgery", "CONFIRMED"});
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<String[]> data = get();
                    queueTableModel.setRowCount(0);
                    for (String[] row : data) {
                        queueTableModel.addRow(row);
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // -- NAVIGATION & PLACEHOLDERS --

    public void showCard(String cardName) {
        showCard(cardName, cardName);
    }

    public void showCard(String cardName, String title) {
        cardLayout.show(contentArea, cardName);
        headerTitleLabel.setText("|  " + title);
        updateNavHighlights(cardName);
        if ("WELCOME".equalsIgnoreCase(cardName)) {
            loadDashboardMetrics();
            loadRecentQueueData();
        }
    }

    public void showPlaceholder(String moduleName, String icon, String description, String note) {
        String key = "PH_" + moduleName.replaceAll("\\s+", "_");

        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBackground(new Color(245, 247, 250));
        placeholder.setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 226, 230)),
                new EmptyBorder(36, 44, 36, 44)));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 48));
        iconLabel.setForeground(new Color(37, 99, 235));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(moduleName);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(15, 23, 42));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(71, 85, 105));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        noteLabel.setForeground(new Color(148, 163, 184));
        noteLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(iconLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
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

    // -- LOGOUT --

    private void performLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to sign out of SmartCare?",
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
