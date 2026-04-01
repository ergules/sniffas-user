package app.vaazar.domain.user.boundary;

import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.deleteAccount.boundary.DeleteAccountService;
import app.vaazar.domain.deleteAccount.boundary.impl.DeleteAccountServiceImpl;
import app.vaazar.domain.deleteAccount.control.DeleteRequestRepository;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.refreshtoken.boundary.RefreshTokenService;
import app.vaazar.domain.user.boundary.impl.UserServiceImpl;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import app.vaazar.endpoint.dto.user.UserDTO;
import app.vaazar.service.firebase.FirebaseAuthService;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static app.vaazar.TestDataHelper.deepCopy;
import static app.vaazar.TestDataHelper.getPrivateSellers;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataJpaTest
@ActiveProfiles("test")
public class UserServiceImplIntegrationTests {
    private final TestEntityManager entityManager;
    private final UserServiceImpl service;
    private final FirebaseAuthService firebaseAuthService;

    @Test
    public void findById() {
        assertEquals(1L, service.findById(1L).getId());
        assertNull(service.findById(99L));
    }

    @Test
    public void isUsernameAvailable() {
        assertFalse(service.isUsernameAvailable("uu1"));
        assertFalse(service.isUsernameAvailable("c1"));
        assertTrue(service.isUsernameAvailable("c1!"));
        assertTrue(service.isUsernameAvailable("dark88"));
        assertTrue(service.isUsernameAvailable("super_boy_96"));
    }

    @Test
    public void saveUser() {
        User user = new User();
        user.setRole(Role.COMPANY);
        user.setFirebaseUid("fbUID");
        user.setUsername("usrName");
        user.setFirstname("fn");
        user.setLastname("ln");
        user.setEmail("e@mail.com");

        User copied = deepCopy(UserDTO.class, user);
        User persisted = service.saveUser(copied);

        assertEquals(user.getRole(), persisted.getRole());
        assertEquals(user.getFirebaseUid(), persisted.getFirebaseUid());
        assertEquals(user.getUsername(), persisted.getUsername());
        assertEquals(user.getEmail(), persisted.getEmail());
    }

    @Test
    public void findBasicUsers() {
        List<BasicUser> resultList = service.findBasicUsers(List.of(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L));
        Set<Long> foundIds = resultList.stream().map(BasicUser::getId).collect(Collectors.toSet());
        assertEquals(6, resultList.size());
        assertFalse(foundIds.contains(4L));
        assertFalse(foundIds.contains(5L));
        assertFalse(foundIds.contains(9L));
        assertTrue(foundIds.contains(1L));
        assertTrue(foundIds.contains(3L));
        assertTrue(foundIds.contains(7L));
    }

    @Test
    public void updateUserInfo() throws AuthorisationException {
        String updatedMail = "_mail@sniffas.com";
        String updatedPhone = "_mobile";
        String updatedUsername = "_username";

        UserRecord mockRecord = Mockito.mock(UserRecord.class);
        when(mockRecord.getEmail()).thenReturn(updatedMail);
        when(mockRecord.getPhoneNumber()).thenReturn(updatedPhone);
        when(firebaseAuthService.getFirebaseRecord(any())).thenReturn(mockRecord);

        User user = entityManager.find(User.class, 1L);
        user.setEmail(updatedMail);
        user.setMobilePhone(updatedPhone);
        user.setUsername(updatedUsername);
        user.setRole(Role.ADMIN);
        entityManager.clear();

        User merged = service.updateUserInfo(user);
        assertNotEquals(user, merged);
        assertEquals(updatedMail, merged.getEmail());
        assertEquals(updatedPhone, merged.getMobilePhone());
        assertEquals(updatedUsername, merged.getUsername());
    }

    @Test
    public void registerUser() throws AuthorisationException {
        String newMail = "_mail@sniffas.com";
        String newPhone = "_mobile";
        String newUsername = "_username";
        String fbUID = "fbUID";

        UserRecord mockRecord = Mockito.mock(UserRecord.class);
        FirebaseToken mockToken = Mockito.mock(FirebaseToken.class);
        when(mockToken.getUid()).thenReturn(fbUID);
        when(mockRecord.getEmail()).thenReturn(newMail);
        when(mockRecord.getPhoneNumber()).thenReturn(newPhone);
        when(firebaseAuthService.getFirebaseRecord(any())).thenReturn(mockRecord);

        User user = new User(null, newUsername, "fn", "ln", "pp.jpg", Role.SELLER);
        user.setMobilePhone(newPhone);
        user.setEmail(newMail);

        User persisted = service.registerUser(user, mockToken);

        assertEquals(newMail, persisted.getEmail());
        assertEquals(newPhone, persisted.getMobilePhone());
        assertEquals(newUsername, persisted.getUsername());
        assertEquals(fbUID, persisted.getFirebaseUid());
        assertEquals(Role.USER, persisted.getRole());
    }

    @Test
    public void deleteUser_whenCustomerDeleteAndReturnNull() {
        DeleteAccountRequest result = service.deleteUser(1L);
        entityManager.flush();
        entityManager.clear();

        assertNull(result);
        User user1 = entityManager.find(User.class, 1L);
        assertTrue(user1.getDeleted());
    }

    @Test
    public void deleteUser_whenSellerSaveAsRequest() {
        User seller = getPrivateSellers(entityManager)
                .stream().filter(u -> !u.getDeleted())
                .findFirst().orElseThrow();
        entityManager.clear();

        DeleteAccountRequest result = service.deleteUser(seller.getId());
        entityManager.flush();
        entityManager.clear();

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(DeleteRequestStatus.NEW, result.getStatus());
        assertNull(result.getAutoDeleteDate());
        assertFalse(entityManager.find(User.class, seller.getId()).getDeleted());
    }

    @Test
    public void findByUid() {
        assertEquals(Optional.empty(), service.findByUid("random_uid"));

        Optional<User> existing = service.findByUid("uid3");
        if (existing.isEmpty()) fail();
        assertEquals(3L, existing.get().getId());
    }

    @Test
    public void findUsers_whenQueryIsEmptyReturnAllSellers() {
        Page<User> resultPage = service
                .findUsers(Optional.empty(), true, PageRequest.ofSize(5));
        assertEquals(3, resultPage.getTotalElements());
    }

    @Test
    public void findUsers_whenQueryIsEmptyReturnAllUsers() {
        Page<User> resultPage = service
                .findUsers(Optional.empty(), false, PageRequest.ofSize(5));
        assertEquals(7, resultPage.getTotalElements()); // undeleted (users + sellers + admin)
        assertEquals(5, resultPage.getContent().size());
    }

    @Test
    public void findUsers_whenQueryIsEmailFindByEmail() {
        Page<User> resultPageForDeleted = service
                .findUsers(Optional.of("5@mail.com"), false, PageRequest.ofSize(5));
        assertEquals(0, resultPageForDeleted.getTotalElements()); // user5 is deleted
        assertEquals(0, resultPageForDeleted.getContent().size());

        Page<User> resultPageForExisting = service
                .findUsers(Optional.of("1@mail.com"), false, PageRequest.ofSize(5));
        assertEquals(3, resultPageForExisting.getTotalElements()); // user, com, seller each 1
        assertEquals(3, resultPageForExisting.getContent().size());
    }

    @Test
    public void findUsers_whenQueryIsStringFindByNameOrUsername() {
        Page<User> resultPageForDeleted = service
                .findUsers(Optional.of("random guy"), false, PageRequest.ofSize(5));
        assertEquals(0, resultPageForDeleted.getTotalElements()); // user5 is deleted
        assertEquals(0, resultPageForDeleted.getContent().size());

        Page<User> resultPageForExistingName = service
                .findUsers(Optional.of("er one"), false, PageRequest.ofSize(5));
        assertEquals(2, resultPageForExistingName.getTotalElements()); // user, seller each 1
        assertEquals(2, resultPageForExistingName.getContent().size());

        Page<User> resultPageForExistingUsername = service
                .findUsers(Optional.of("uu"), false, PageRequest.ofSize(5));
        assertEquals(3, resultPageForExistingUsername.getTotalElements()); // undeleted users
        assertEquals(3, resultPageForExistingUsername.getContent().size());

        Page<User> resultPageForNonExistingUsernameSeller = service
                .findUsers(Optional.of("uu"), true, PageRequest.ofSize(5));
        assertEquals(0, resultPageForNonExistingUsernameSeller.getTotalElements()); // no seller
    }


    @Autowired
    public UserServiceImplIntegrationTests(TestEntityManager entityManager,
                                           UserRepository userRepo,
                                           DeleteRequestRepository deleteRequestRepo) {

        firebaseAuthService = Mockito.mock(FirebaseAuthService.class);
        ApplicationEventPublisher eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
        RefreshTokenService refreshTokenService = Mockito.mock(RefreshTokenService.class);
        Logger dASL = LoggerFactory.getLogger(DeleteAccountServiceImpl.class);
        DeleteAccountService deleteAccountService = new DeleteAccountServiceImpl(deleteRequestRepo, userRepo, refreshTokenService, eventPublisher, dASL);

        this.entityManager = entityManager;
        this.service = new UserServiceImpl(userRepo, firebaseAuthService, deleteAccountService, refreshTokenService, eventPublisher);
    }
}
