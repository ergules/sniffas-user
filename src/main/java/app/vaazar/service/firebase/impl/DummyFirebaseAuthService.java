package app.vaazar.service.firebase.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.service.firebase.FirebaseAuthService;

@Service
@ConditionalOnMissingBean(value = FirebaseAuthService.class, ignored = DummyFirebaseAuthService.class)
public class DummyFirebaseAuthService implements FirebaseAuthService {   // dummy service for local

    @Override
    public FirebaseToken verifyIdToken(String token) throws AuthorisationException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyIdToken'");
    }

    @Override
    public UserRecord getFirebaseRecord(String uid) throws AuthorisationException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getFirebaseRecord'");
    }

    @Override
    public void deleteFirebaseRecord(String uid) throws AuthorisationException {
    }
    
}
