package app.vaazar.domain.event.entity;

import app.vaazar.domain.user.entity.User;
import app.vaazar.service.model.NotificationData;
import app.vaazar.service.model.NotificationOptions;

import static app.vaazar.service.model.NotificationOptions.NotificationType.USER_REGISTRATION_COMPLETED;

public class UserCreatedEvent implements NotificationEvent {
    private final NotificationOptions options;

    public UserCreatedEvent(User user) {
        NotificationData data = new NotificationData();
        data.setUserId(user.getId());
        data.setUserName(user.getUsername());
        options = new NotificationOptions(USER_REGISTRATION_COMPLETED, data);
    }

    public NotificationOptions.NotificationType getNotificationType() {
        return USER_REGISTRATION_COMPLETED;
    }

    public NotificationOptions getNotificationOptions() {
        return options;
    }
}
