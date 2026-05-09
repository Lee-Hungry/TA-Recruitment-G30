package com.group30.tarecruitment.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AiSettingsRepository {

    private final Path settingsPath;

    public AiSettingsRepository(Path settingsPath) {
        this.settingsPath = settingsPath;
    }

    public AiSettings load() {
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(settingsPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load AI settings from " + settingsPath, e);
        }

        return new AiSettings(
                require(properties, "api_key"),
                require(properties, "base_url"),
                require(properties, "model"),
                parseInt(properties.getProperty("connect_timeout_seconds", "20")),
                parseInt(properties.getProperty("request_timeout_seconds", "60"))
        );
    }

    private String require(Properties properties, String key) {
        String value = properties.getProperty(key, "").trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Missing AI setting: " + key);
        }
        return value;
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Invalid integer value in AI settings: " + value, e);
        }
    }
}
