package app.vaazar.domain.baseEntity;


import app.vaazar.domain.user.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.time.Instant;

public class BaseEntityInsertUpdateListener {

    Logger log = LoggerFactory.getLogger(BaseEntityInsertUpdateListener.class);

    @PrePersist
    public void preCreate(BaseEntity baseEntity) {
        try {
            baseEntity.createdBy = userId();
            baseEntity.createdAt = Instant.now();
        } catch (Exception e) {
            log.error("preCreate error for class " + baseEntity.getClass().getSimpleName(), e);
        }
    }

    @PreUpdate
    public void preUpdate(BaseEntity baseEntity) {
        baseEntity.setUpdatedBy(userId());
        baseEntity.setUpdatedAt(Instant.now());
    }


    private Long userId() {
        try {
            return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        } catch (Exception ignore) {
        }
        return null;
    }
}
