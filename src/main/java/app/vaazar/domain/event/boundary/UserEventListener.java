package app.vaazar.domain.event.boundary;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.event.entity.UserDeletedEvent;
import app.vaazar.service.firebase.FirebaseAuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class UserEventListener {

    private final FirebaseAuthService firebaseAuthService;

    public UserEventListener(FirebaseAuthService firebaseAuthService) {
        this.firebaseAuthService = firebaseAuthService;
    }

    @Async
    @TransactionalEventListener
    public void handleNotification(UserDeletedEvent event) throws AuthorisationException {
        log.info("delete user from firebase: {}", event.getUser());
        firebaseAuthService.deleteFirebaseRecord(event.getUser().getFirebaseUid());
    }

}
