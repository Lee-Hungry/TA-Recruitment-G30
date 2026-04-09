package com.group30.tarecruitment.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class WorkloadSettingsRepository {

    private static final String MAX_WEEKLY_HOURS_KEY = "max_weekly_hours";
    private static final int DEFAULT_MAX_WEEKLY_HOURS = 20;
    private final Path settingsPath;

    public WorkloadSettingsRepository(Path settingsPath) {
        this.settingsPath = settingsPath;
        ensureFileExists();
    }

    public int maxWeeklyHours() {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(settingsPath)) {
            properties.load(inputStream);
            return Integer.parseInt(properties.getProperty(MAX_WEEKLY_HOURS_KEY, Integer.toString(DEFAULT_MAX_WEEKLY_HOURS)).trim());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read workload settings", e);
        }
    }

    private void ensureFileExists() {
        try {
            if (settingsPath.getParent() != null) {
                Files.createDirectories(settingsPath.getParent());
            }
            if (Files.exists(settingsPath)) {
                return;
            }

            Properties properties = new Properties();
            properties.setProperty(MAX_WEEKLY_HOURS_KEY, Integer.toString(DEFAULT_MAX_WEEKLY_HOURS));
            try (OutputStream outputStream = Files.newOutputStream(settingsPath)) {
                properties.store(outputStream, "TA recruitment settings");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to init workload settings", e);
        }
    }
}
