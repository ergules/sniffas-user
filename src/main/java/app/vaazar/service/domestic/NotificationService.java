package app.vaazar.service.domestic;

import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.security.JwtTokenUtil;
import app.vaazar.service.NotificationSender;
import app.vaazar.service.ServiceDiscovery;
import app.vaazar.service.model.NotificationOptions;
import org.slf4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Service
public class NotificationService implements NotificationSender {

    private final RestTemplate restTemplate;
    private final JwtTokenUtil jwtTokenUtil;
    private final ServiceDiscovery serviceDiscovery;
    private final Logger log;

    private HttpHeaders headers;
    private Instant headerExpires;

    @Async
    public void sendNotification(NotificationOptions options) {
        log.info("send notification: {}", options.getData());
        HttpEntity<NotificationOptions> entity = new HttpEntity<>(options, getHeaders());
        restTemplate.exchange(serviceDiscovery.getNotificationServicePath() + "/notifications/notifyUsers",
                HttpMethod.POST, entity, String.class);
    }

    private HttpHeaders getHeaders() {
        if (headerExpires == null || Instant.now().isAfter(headerExpires)) {
            headerExpires = Instant.now().plusSeconds(JwtTokenUtil.EXPIRATION_IN_SECONDS - 15); // 15 seconds in case
            headers = new HttpHeaders();
            headers.setBearerAuth(jwtTokenUtil.generateAccessToken(
                    new User(0L, null, "user-service", Role.SERVICE)));
        }
        return headers;
    }

    public NotificationService(JwtTokenUtil jwtTokenUtil, ServiceDiscovery serviceDiscovery, Logger log) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.serviceDiscovery = serviceDiscovery;
        this.log = log;
        this.restTemplate = new RestTemplate();
    }
}
