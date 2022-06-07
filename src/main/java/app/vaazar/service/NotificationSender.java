package app.vaazar.service;

import app.vaazar.service.model.NotificationOptions;

public interface NotificationSender {
    void sendNotification(NotificationOptions options);
}
