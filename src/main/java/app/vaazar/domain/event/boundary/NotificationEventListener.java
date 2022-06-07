package app.vaazar.domain.event.boundary;

import app.vaazar.domain.event.entity.NotificationEvent;
import app.vaazar.service.NotificationSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import static app.vaazar.service.model.NotificationOptions.NotificationType.USER_REGISTRATION_COMPLETED;

@Component
public class NotificationEventListener {

    private final NotificationSender notificationSender;

    @Async
    @TransactionalEventListener
    public void handleNotification(NotificationEvent event) throws InterruptedException {
        if (event.getNotificationType() == USER_REGISTRATION_COMPLETED) {
            Thread.sleep(8000);
            notificationSender.sendNotification(event.getNotificationOptions());
        }

    }

    public NotificationEventListener(NotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }
}
