package app.vaazar.domain.user.boundary;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.deleteAccount.boundary.DeleteAccountService;
import app.vaazar.domain.refreshtoken.boundary.RefreshTokenService;
import app.vaazar.domain.user.boundary.impl.UserServiceImpl;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.service.firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class UserServiceImplUnitTests {

    @Mock
    UserRepository userRepo;
    @Mock
    FirebaseAuthService firebaseAuthService;
    @Mock
    DeleteAccountService deleteAccountService;
    @Mock
    RefreshTokenService refreshTokenService;
    @Mock
    ApplicationEventPublisher eventPublisher;

    @InjectMocks
    UserServiceImpl service;


    @Test
    public void findById() {
        long userId = 5;
        service.findById(userId);

        verify(userRepo).findById(userId);
    }

    @Test
    public void isUsernameAvailable() {
        when(userRepo.existsByUsername(any())).thenReturn(false);
        assertTrue(service.isUsernameAvailable(""));
        verify(userRepo).existsByUsername("");
    }

    @Test
    public void saveUser() {
        long userId = 5;
        User user = new User(userId, null, null, Role.USER);

        when(userRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        User saved = service.saveUser(user);
        verify(userRepo).save(user);
        assertEquals((long) saved.getId(), userId);
    }

    @Test
    public void findBasicUsers() {
        List<Long> idList = List.of(1L, 2L, 3L);
        service.findBasicUsers(idList);
        verify(userRepo).findBasicUsersInList(idList);
    }

    @Test
    public void updateUserInfo() throws AuthorisationException {
        long userId = 10;
        String updatedMail = "_mail@sniffas.com";
        String updatedUsername = "_username";
        String updatedPhone = "_mobile";
        UserRecord mockRecord = Mockito.mock(UserRecord.class);

        User toUpdate = new User(userId, "other", "other", Role.USER);
        User user = new User(userId, updatedMail, updatedUsername, Role.ADMIN);
        user.setRole(Role.ADMIN);
        user.setMobilePhone(updatedPhone);

        when(userRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userRepo.findById(userId)).thenReturn(Optional.of(toUpdate));
        when(mockRecord.getEmail()).thenReturn(updatedMail);
        when(mockRecord.getPhoneNumber()).thenReturn(updatedPhone);
        when(firebaseAuthService.getFirebaseRecord(any())).thenReturn(mockRecord);

        User saved = service.updateUserInfo(user);
        assertEquals(saved.getEmail(), updatedMail);
        assertEquals(saved.getUsername(), updatedUsername);
        assertEquals(saved.getMobilePhone(), updatedPhone);
        assertEquals(saved.getRole(), Role.USER);
    }

    @Test
    public void registerUser_happy() throws AuthorisationException {
        long userId = 5;
        String email = "_email";
        String username = "_username";
        String mobilePhone = "_mobile";
        User user = new User(userId, email, username, Role.ADMIN);
        user.setRole(Role.ADMIN);
        user.setMobilePhone(mobilePhone);

        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);
        when(mockToken.getEmail()).thenReturn(email);
        UserRecord mockRecord = Mockito.mock(UserRecord.class);
        when(mockRecord.getPhoneNumber()).thenReturn(mobilePhone);
        when(firebaseAuthService.getFirebaseRecord(any())).thenReturn(mockRecord);

        when(userRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        User saved = service.registerUser(user, mockToken);
        verify(userRepo).save(user);

        assertEquals(saved.getId(), userId);
        assertEquals(saved.getEmail(), email);
        assertEquals(saved.getMobilePhone(), mobilePhone);
        assertEquals(saved.getRole(), Role.USER);
    }

    @Test
    public void deleteUser_customer() {
        User user = new User(0L, "other", "other", Role.USER);

        when(userRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(userRepo.findById(any())).thenReturn(Optional.of(user));

        service.deleteUser(user.getId());
        verify(userRepo).save(user);
        verify(refreshTokenService).deleteByUser(user);
        assertTrue(user.getDeleted());
    }

    @Test
    public void deleteUser_seller() {
        User user = new User(5L, "other5", "other5", Role.COMPANY);
        user.setRole(Role.COMPANY);

        when(userRepo.findById(any())).thenReturn(Optional.of(user));

        service.deleteUser(user.getId());

        verify(deleteAccountService).requestToDeleteAccount(user);
        verify(userRepo, never()).save(user);
        verify(refreshTokenService, never()).deleteByUser(any());
        assertFalse(user.getDeleted());
    }

    @Test
    public void findUsers_noQuery() {
        Pageable page = Pageable.unpaged();
        service.findUsers(Optional.empty(), false, page);
        verify(userRepo).findBasicUsers(false, page);
    }

    @Test
    public void findUsers_nameQuery() {
        Pageable page = Pageable.unpaged();
        String query = "str";
        service.findUsers(Optional.of(query), true, page);
        verify(userRepo).findBasicUsersByName(true, query, page);
    }

    @Test
    public void findUsers_emailQuery() {
        Pageable page = Pageable.unpaged();
        String query = "mail@sniffas.com";
        service.findUsers(Optional.of(query), false, page);
        verify(userRepo).findBasicUsersByEmail(false, query, page);
    }

}
