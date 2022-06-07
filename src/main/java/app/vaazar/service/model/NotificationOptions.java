package app.vaazar.service.model;

public class NotificationOptions {
    private NotificationType notificationType;
    private NotificationData data;

    public NotificationOptions() {
    }

    public NotificationOptions(NotificationType notificationType, NotificationData data) {
        this.notificationType = notificationType;
        this.data = data;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public NotificationData getData() {
        return data;
    }

    public void setData(NotificationData data) {
        this.data = data;
    }

    public enum NotificationType {
        USER_REGISTRATION_COMPLETED
    }
}
