package app.vaazar.Service.Firebase;

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseAuthService {

    @Value("${app.paths.firebase-keys}")
    String firebaseKeyPath;

    FirebaseAuth auth;

    public FirebaseToken verifyIdToken(String token) throws FirebaseAuthException {
        return auth.verifyIdToken(token);
    }

    public UserRecord getFirebaseRecord(String uid) throws FirebaseAuthException {
        return auth.getUser(uid);
    }

    @PostConstruct
    public void initFirebaseService() {
        try (InputStream serviceAccount =
                     new FileInputStream(firebaseKeyPath)) {
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
