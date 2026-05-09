package com.group30.tarecruitment.notifications;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldPersistUnreadNotificationsAndMarkRelatedApplicationAsRead() throws Exception {
        Path tempDir = Files.createTempDirectory("notifications");
        NotificationService service = new NotificationService(
                new CsvNotificationRepository(tempDir.resolve("notifications.csv")),
                fixedClock
        );

        service.notifyApplicationStatusChanged("ta@g30.local", "Software Engineering TA", "ACCEPTED", "app-001");
        service.notifyApplicationStatusChanged("ta@g30.local", "Data Structures TA", "REJECTED", "app-002");

        assertEquals(2, service.unreadCount("ta@g30.local", "TA"));

        service.markApplicationNotificationsAsRead("ta@g30.local", "app-001");
        List<NotificationRecord> notifications = service.listNotificationsFor("ta@g30.local", "TA");

        assertEquals(1, service.unreadCount("ta@g30.local", "TA"));
        assertTrue(notifications.stream()
                .filter(notification -> "app-001".equals(notification.relatedApplicationId()))
                .allMatch(NotificationRecord::isRead));
        assertTrue(notifications.stream()
                .filter(notification -> "app-002".equals(notification.relatedApplicationId()))
                .noneMatch(NotificationRecord::isRead));
    }

    @Test
    void shouldCreateSubmissionNoticeForMoInbox() throws Exception {
        Path tempDir = Files.createTempDirectory("mo-notifications");
        NotificationService service = new NotificationService(
                new CsvNotificationRepository(tempDir.resolve("notifications.csv")),
                fixedClock
        );

        service.notifyApplicationSubmitted("mo@g30.local", "Software Engineering TA", "Alice Zhang");

        List<NotificationRecord> notifications = service.listNotificationsFor("mo@g30.local", "MO");
        assertEquals(1, notifications.size());
        assertEquals("New TA application received", notifications.getFirst().title());
        assertTrue(notifications.getFirst().message().contains("Alice Zhang"));
        assertEquals("APPLICATION_SUBMITTED", notifications.getFirst().category());
    }
}
