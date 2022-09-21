package app.vaazar.service.firebase;

import app.vaazar.config.exception.AuthorisationException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseAuthService {

    @Value("${app.firebase-token}")
    String firebaseToken;

    FirebaseAuth auth;

    public FirebaseToken verifyIdToken(String token) throws AuthorisationException {
        try {
            return auth.verifyIdToken(token);
        } catch (FirebaseAuthException e) {
            throw new AuthorisationException(e.getMessage());
        }
    }

    public UserRecord getFirebaseRecord(String uid) throws AuthorisationException {
        try {
            return auth.getUser(uid);
        } catch (FirebaseAuthException e) {
            throw new AuthorisationException(e.getMessage());
        }
    }

    @PostConstruct
    public void initFirebaseService() {
        try (InputStream serviceAccount =
                     new ByteArrayInputStream(firebaseToken.getBytes())) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            auth = FirebaseAuth.getInstance();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
