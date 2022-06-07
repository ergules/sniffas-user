package app.vaazar.endpoint;

import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import app.vaazar.endpoint.dto.RegistrationDto;
import app.vaazar.security.JwtTokenUtil;
import app.vaazar.service.firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/users/public")
public class PublicApi {

    Logger log;
    UserService userService;
    JwtTokenUtil jwtTokenUtil;
    FirebaseAuthService firebaseService;

    @PostMapping("login")
    public ResponseEntity<User> loginWithFirebaseToken(@RequestBody String token) {
        try {
            FirebaseToken firebaseToken = firebaseService.verifyIdToken(token);
            User user = userService.findByUid(firebaseToken.getUid()).orElseThrow();
            return ResponseEntity.ok()
                    .header("Access-Control-Expose-Headers", "Authorization")
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateAccessToken(user))
                    .body(user);

        } catch (FirebaseAuthException fae) {
            throw new AccessDeniedException("token is invalid");
        } catch (NoSuchElementException nse) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/register")
    public User registerWithFirebaseToken(@RequestBody @Valid RegistrationDto registrationDto) throws FirebaseAuthException {
        FirebaseToken firebaseToken = firebaseService.verifyIdToken(registrationDto.getToken());
        return userService.registerUser(registrationDto.getUser(), firebaseToken);
    }


    @GetMapping("/availableUsernames")
    public boolean isUsernameAvailable(@RequestParam String key) {
        return userService.isUsernameAvailable(key);
    }

    @PostMapping("/userInfo")
    public List<BasicUser> getBasicUsers(@RequestBody List<Long> idList) {
        return userService.findBasicUsers(idList);
    }

    public PublicApi(Logger log, UserService userService, JwtTokenUtil jwtTokenUtil, FirebaseAuthService firebaseService) {
        this.log = log;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.firebaseService = firebaseService;
    }
}
