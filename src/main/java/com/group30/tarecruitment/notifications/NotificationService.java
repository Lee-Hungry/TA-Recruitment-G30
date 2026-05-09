package com.group30.tarecruitment.notifications;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class NotificationService {

    private final CsvNotificationRepository repository;
    private final Clock clock;

    public NotificationService(CsvNotificationRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public NotificationRecord createNotification(
            String recipientEmail,
            String recipientRole,
            String title,
            String message,
            String category,
            String relatedApplicationId
    ) {
        NotificationRecord notification = new NotificationRecord(
                "notice-" + UUID.randomUUID(),
                normalizeEmail(recipientEmail),
                normalizeRole(recipientRole),
                clean(title),
                clean(message),
                clean(category),
                clean(relatedApplicationId),
                OffsetDateTime.now(clock).toString(),
                ""
        );
        repository.append(notification);
        return notification;
    }

    public void notifyApplicationSubmitted(String recipientMoEmail, String jobTitle, String applicantLabel) {
        createNotification(
                recipientMoEmail,
                "MO",
                "New TA application received",
                applicantLabel + " applied for " + jobTitle + ".",
                "APPLICATION_SUBMITTED",
                ""
        );
    }

    public void notifyApplicationStatusChanged(String taEmail, String jobTitle, String nextStatus, String applicationId) {
        String readableStatus = "ACCEPTED".equalsIgnoreCase(nextStatus) ? "accepted" : "rejected";
        createNotification(
                taEmail,
                "TA",
                "Application status updated",
                "Your application for " + jobTitle + " was " + readableStatus + ".",
                "APPLICATION_STATUS_CHANGED",
                applicationId
        );
    }

    public List<NotificationRecord> listNotificationsFor(String recipientEmail, String recipientRole) {
        String normalizedEmail = normalizeEmail(recipientEmail);
        String normalizedRole = normalizeRole(recipientRole);
        return repository.readAll().stream()
                .filter(notification -> notification.recipientEmail().equalsIgnoreCase(normalizedEmail))
                .filter(notification -> normalizedRole.isBlank()
                        || notification.recipientRole().equalsIgnoreCase(normalizedRole))
                .sorted(Comparator.comparing(NotificationRecord::createdAt).reversed())
                .toList();
    }

    public List<NotificationRecord> listUnreadNotificationsFor(String recipientEmail, String recipientRole) {
        return listNotificationsFor(recipientEmail, recipientRole).stream()
                .filter(notification -> !notification.isRead())
                .toList();
    }

    public int unreadCount(String recipientEmail, String recipientRole) {
        return listUnreadNotificationsFor(recipientEmail, recipientRole).size();
    }

    public void markAsRead(String notificationId) {
        repository.readAll().stream()
                .filter(notification -> notification.notificationId().equals(notificationId))
                .findFirst()
                .ifPresent(notification -> repository.replace(notification.markRead(OffsetDateTime.now(clock).toString())));
    }

    public void markApplicationNotificationsAsRead(String recipientEmail, String applicationId) {
        String normalizedEmail = normalizeEmail(recipientEmail);
        String now = OffsetDateTime.now(clock).toString();
        repository.readAll().stream()
                .filter(notification -> notification.recipientEmail().equalsIgnoreCase(normalizedEmail))
                .filter(notification -> applicationId.equals(notification.relatedApplicationId()))
                .filter(notification -> !notification.isRead())
                .forEach(notification -> repository.replace(notification.markRead(now)));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        return role == null ? "" : role.trim().toUpperCase(Locale.ROOT);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}
