package app.vaazar.domain.event.entity;

import app.vaazar.service.model.NotificationOptions;

public interface NotificationEvent {
    NotificationOptions.NotificationType getNotificationType();
    NotificationOptions getNotificationOptions();
}
