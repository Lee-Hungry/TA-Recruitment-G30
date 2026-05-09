package com.group30.tarecruitment.notifications;

import com.group30.tarecruitment.csv.CsvSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CsvNotificationRepository {

    private static final String HEADER = "notification_id,recipient_email,recipient_role,title,message,category,related_application_id,created_at,read_at";
    private final Path csvPath;

    public CsvNotificationRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public void append(NotificationRecord notification) {
        ensureFileExists();
        try {
            Files.writeString(csvPath, toCsv(notification) + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append notification", e);
        }
    }

    public List<NotificationRecord> readAll() {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            List<NotificationRecord> notifications = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> parts = CsvSupport.parseRow(line);
                if (parts.size() < 9) {
                    continue;
                }
                notifications.add(new NotificationRecord(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        parts.get(3),
                        parts.get(4),
                        parts.get(5),
                        parts.get(6),
                        parts.get(7),
                        parts.get(8)
                ));
            }
            return notifications;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read notification csv", e);
        }
    }

    public void replace(NotificationRecord updated) {
        List<NotificationRecord> notifications = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);

        boolean replaced = false;
        for (NotificationRecord current : notifications) {
            if (current.notificationId().equals(updated.notificationId())) {
                rewritten.add(toCsv(updated));
                replaced = true;
            } else {
                rewritten.add(toCsv(current));
            }
        }

        if (!replaced) {
            rewritten.add(toCsv(updated));
        }
        rewrite(rewritten);
    }

    private String toCsv(NotificationRecord notification) {
        return CsvSupport.joinRow(
                notification.notificationId(),
                notification.recipientEmail(),
                notification.recipientRole(),
                notification.title(),
                notification.message(),
                notification.category(),
                notification.relatedApplicationId(),
                notification.createdAt(),
                notification.readAt()
        );
    }

    private void rewrite(List<String> rewritten) {
        try {
            Files.writeString(
                    csvPath,
                    String.join(System.lineSeparator(), rewritten) + System.lineSeparator(),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to rewrite notification csv", e);
        }
    }

    private void ensureFileExists() {
        try {
            if (csvPath.getParent() != null) {
                Files.createDirectories(csvPath.getParent());
            }
            if (!Files.exists(csvPath)) {
                Files.writeString(csvPath, HEADER + System.lineSeparator(), StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to init notification csv", e);
        }
    }
}
