package app.vaazar.domain.event.entity;

import app.vaazar.domain.user.entity.User;
import lombok.Getter;

@Getter
public class UserDeletedEvent {
    private final User user;

    public UserDeletedEvent(User user) {
        this.user = user;
    }
}
