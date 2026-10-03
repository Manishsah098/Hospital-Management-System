package com.smartcare;

import com.formdev.flatlaf.FlatLightLaf;
import com.smartcare.ui.LoginFrame;
import com.smartcare.util.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main application entry point for SmartCare: AI-Enhanced Hospital Management System.
 */
public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        // 1. Enable modern Swing system UI anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // 2. Initialize FlatLaf modern Look & Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // Optional UI customization defaults
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to initialize FlatLaf look and feel, using system default.", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        LOGGER.info("Starting SmartCare Hospital Management System...");

        // 3. Check and initialize Database Schema asynchronously
        new Thread(() -> {
            try {
                LOGGER.info("Checking database connection and schema status...");
                DatabaseConnection.initializeDatabaseIfRequired();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Database check encountered an issue: " + e.getMessage());
            }
        }).start();

        // 4. Launch Swing GUI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
