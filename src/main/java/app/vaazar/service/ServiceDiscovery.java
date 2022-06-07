package app.vaazar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServiceDiscovery {
    private final String notificationServicePath;

    // static injection for notification service path. after implementing a config server or eureka, update here
    public String getNotificationServicePath() {
        return notificationServicePath;
    }

    public ServiceDiscovery(@Value("${app.paths.notification-service}") String notificationServicePath) {
        this.notificationServicePath = notificationServicePath;
    }
}
