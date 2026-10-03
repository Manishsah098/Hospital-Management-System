package com.smartcare.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility to load system configuration properties with Environment Variable fallbacks.
 */
public class ConfigLoader {
    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final Properties properties = new Properties();

    static {
        // 1. Try external db.properties in working directory
        java.io.File externalConfig = new java.io.File("db.properties");
        if (externalConfig.exists()) {
            try (java.io.FileInputStream fis = new java.io.FileInputStream(externalConfig)) {
                properties.load(fis);
                LOGGER.info("Loaded configuration from local ./db.properties");
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to load local db.properties: " + e.getMessage());
            }
        }

        // 2. Load from classpath if not found or merge defaults
        try (InputStream is = ConfigLoader.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                Properties defaultProps = new Properties();
                defaultProps.load(is);
                for (String key : defaultProps.stringPropertyNames()) {
                    if (!properties.containsKey(key)) {
                        properties.setProperty(key, defaultProps.getProperty(key));
                    }
                }
                LOGGER.info("Loaded classpath db.properties configuration");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load db.properties from classpath", e);
        }
    }

    public static String get(String key, String defaultValue) {
        // 1. Check system properties (e.g. -Dkey=value)
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) return value;

        // 2. Check environment variables (replace dot with underscore and uppercase)
        String envKey = key.replace('.', '_').toUpperCase();
        value = System.getenv(envKey);
        if (value != null && !value.isBlank()) return value;

        // 3. Check loaded properties file
        value = properties.getProperty(key);
        if (value != null && !value.isBlank()) return value;

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, String.valueOf(defaultValue));
        return Boolean.parseBoolean(value);
    }
}
