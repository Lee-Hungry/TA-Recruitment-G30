package com.group30.tarecruitment.notifications;

public record NotificationRecord(
        String notificationId,
        String recipientEmail,
        String recipientRole,
        String title,
        String message,
        String category,
        String relatedApplicationId,
        String createdAt,
        String readAt
) {

    public boolean isRead() {
        return readAt != null && !readAt.isBlank();
    }

    public NotificationRecord markRead(String nextReadAt) {
        return new NotificationRecord(
                notificationId,
                recipientEmail,
                recipientRole,
                title,
                message,
                category,
                relatedApplicationId,
                createdAt,
                nextReadAt
        );
    }
}
