package app.vaazar.Endpoint;


import app.vaazar.Domain.User.Boundary.UserService;
import app.vaazar.Domain.User.Entity.User;
import app.vaazar.Endpoint.Dto.RegistrationDto;
import app.vaazar.Security.JwtTokenUtil;
import app.vaazar.Service.Firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;


import java.util.NoSuchElementException;
import java.util.Optional;


@RestController
@RequestMapping("/users/public")
public class PublicApi {

    Logger log;
    UserService userService;
    JwtTokenUtil jwtTokenUtil;
    FirebaseAuthService firebaseService;

    @PostMapping("login")
    public ResponseEntity<User> loginWithFirebaseToken(@RequestBody String token) throws AuthenticationException {
        try {
            FirebaseToken firebaseToken = firebaseService.verifyIdToken(token);
            User user = userService.findByUid(firebaseToken.getUid()).orElseThrow();
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwtTokenUtil.generateAccessToken(user))
                    .body(user);

        } catch (FirebaseAuthException fae) {
            throw new AuthenticationException("token is invalid");
        } catch (NoSuchElementException nse) {
            throw new AuthenticationException("user is not present");
        }
    }

    @PostMapping("/register")
    public User registerWithFirebaseToken(@Validated @RequestBody RegistrationDto registrationDto) throws FirebaseAuthException {
        FirebaseToken firebaseToken = firebaseService.verifyIdToken(registrationDto.getToken());
        return userService.registerUser(registrationDto.getUser(), firebaseToken);
    }


    public PublicApi(Logger log, UserService userService, JwtTokenUtil jwtTokenUtil, FirebaseAuthService firebaseService) {
        this.log = log;
        this.userService = userService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.firebaseService = firebaseService;
    }
}
