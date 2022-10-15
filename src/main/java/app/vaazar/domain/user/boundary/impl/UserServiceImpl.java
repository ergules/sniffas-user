package app.vaazar.domain.user.boundary.impl;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.deleteAccount.boundary.DeleteAccountService;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.event.entity.UserCreatedEvent;
import app.vaazar.domain.event.entity.UserDeletedEvent;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import app.vaazar.service.firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    private final FirebaseAuthService firebaseAuthService;
    private final DeleteAccountService deleteAccountService;
    private final ApplicationEventPublisher eventPublisher;
    private final Pattern emailQueryPattern = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+", Pattern.CASE_INSENSITIVE);

    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepo.existsByUsername(username);
    }

    public User saveUser(User user) {
        return userRepo.save(user);
    }

    public List<BasicUser> findBasicUsers(List<Long> idList) {
        return userRepo.findBasicUsersInList(idList);
    }

    public User updateUserInfo(User user) throws AuthorisationException {
        User toUpdate = userRepo.findById(user.getId()).orElseThrow();
        if (toUpdate.getDeleted())
            throw new NoSuchElementException("User does not exist or may be deleted");
        toUpdate.updateBaseFields(user);
        toUpdate.updateSellerFields(user);
        UserRecord firebaseRecord = null;
        if (!toUpdate.getEmail().equalsIgnoreCase(user.getEmail())) {
            firebaseRecord = firebaseAuthService.getFirebaseRecord(toUpdate.getFirebaseUid());
            if (firebaseRecord.getEmail().equals(user.getEmail())) {
                toUpdate.setEmail(user.getEmail());
            }
        }
        if (user.getMobilePhone() != null && !user.getMobilePhone().equals(toUpdate.getMobilePhone())) {
            if (firebaseRecord == null)
                firebaseRecord = firebaseAuthService.getFirebaseRecord(toUpdate.getFirebaseUid());
            if (user.getMobilePhone().equals(firebaseRecord.getPhoneNumber())) {
                toUpdate.setMobilePhone(user.getMobilePhone());
            }
        }
        return userRepo.save(toUpdate);
    }

    public User registerUser(User user, FirebaseToken token) throws AuthorisationException {
        if (token.getEmail() != null && !user.getEmail().equalsIgnoreCase(token.getEmail()))
            throw new IllegalStateException("emails do not match");

        user.setEmail(user.getEmail()); //ensure lowerCase
        user.setFirebaseUid(token.getUid());
        user.setRole(Role.USER);
        if (user.getMobilePhone() != null) {
            UserRecord firebaseRecord = firebaseAuthService.getFirebaseRecord(token.getUid());
            user.setMobilePhone(firebaseRecord.getPhoneNumber());
        } // trust the record on firebase
        User persisted = saveUser(user);
        eventPublisher.publishEvent(new UserCreatedEvent(persisted));
        return persisted;
    }

    public DeleteAccountRequest deleteUser(Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        if (user.getDeleted())
            return null;
        if (user.getRole() == Role.USER) {
            user.setDeleted(true);
            userRepo.save(user);
            eventPublisher.publishEvent(new UserDeletedEvent(user));
        }
        if (user.getRole() == Role.SELLER || user.getRole() == Role.COMPANY) {
            return deleteAccountService.requestToDeleteAccount(user);
        }
        return null;
    }

    public Optional<User> findByUid(String uid) {
        return userRepo.findByFirebaseUid(uid);
    }

    public Page<User> findUsers(Optional<String> query, boolean sellersOnly, Pageable page) {
        if (query.isPresent() && query.get().length() > 0) {
            if (emailQueryPattern.matcher(query.get()).matches())
                return userRepo.findBasicUsersByEmail(sellersOnly, query.get(), page);
            else
                return userRepo.findBasicUsersByName(sellersOnly, query.get(), page);
        } else
            return userRepo.findBasicUsers(sellersOnly, page);
    }

    public UserServiceImpl(UserRepository userRepo, FirebaseAuthService firebaseAuthService, DeleteAccountService deleteAccountService, ApplicationEventPublisher eventPublisher) {
        this.userRepo = userRepo;
        this.firebaseAuthService = firebaseAuthService;
        this.deleteAccountService = deleteAccountService;
        this.eventPublisher = eventPublisher;
    }
}
