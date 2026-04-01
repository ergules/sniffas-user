package app.vaazar.service.firebase;

import app.vaazar.config.exception.AuthorisationException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;

public interface FirebaseAuthService {

    FirebaseToken verifyIdToken(String token) throws AuthorisationException;

    UserRecord getFirebaseRecord(String uid) throws AuthorisationException;

    void deleteFirebaseRecord(String uid) throws AuthorisationException;

}
