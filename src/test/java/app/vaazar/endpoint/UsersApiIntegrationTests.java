package app.vaazar.endpoint;

import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.ShareHolder;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.SellerRequestDto;
import app.vaazar.endpoint.dto.address.AddressDTO;
import app.vaazar.endpoint.dto.approval.ApprovalDTO;
import app.vaazar.endpoint.dto.company.CompanyDTO;
import app.vaazar.endpoint.dto.company.ShareholderDTO;
import app.vaazar.endpoint.dto.user.UserDTO;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static app.vaazar.TestDataHelper.getPrivateSellers;
import static app.vaazar.TestDataHelper.registerToSecurityContext;
import static app.vaazar.domain.approval.entity.ApplicationType.PRIVATE;
import static app.vaazar.domain.approval.entity.ApprovalStatus.PENDING;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UsersApiIntegrationTests {

    @Autowired
    UsersApi usersApi;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager entityManager;


    @Test
    public void getUser() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        entityManager.clear();

        UserDTO response = usersApi.getUser(userId, getAsAuthToken(user));

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getRole(), response.getRole());
        assertEquals(user.getFirebaseUid(), response.getFirebaseUid());
        assertEquals(user.getFirstname(), response.getFirstname());
        assertEquals(user.getLastname(), response.getLastname());
        assertEquals(user.getUsername(), response.getUsername());
    }

    @Test
    @SneakyThrows
    public void updateUser() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        UserDTO dto = modelMapper.map(user, UserDTO.class);
        entityManager.clear();

        dto.setUsername("updated un");
        dto.setFirstname("updated fn");
        dto.setRole(Role.ADMIN);

        UserDTO responseDTO = usersApi.updateUser(userId, dto, getAsAuthToken(user));
        entityManager.flush();
        entityManager.clear();

        assertEquals(dto.getUsername(), responseDTO.getUsername());
        assertEquals(dto.getFirstname(), responseDTO.getFirstname());
        assertNotEquals(dto.getRole(), responseDTO.getRole());

        User updated = entityManager.find(User.class, userId);
        assertEquals(dto.getUsername(), updated.getUsername());
        assertEquals(dto.getFirstname(), updated.getFirstname());
        assertNotEquals(dto.getRole(), updated.getRole());
    }

    @Test
    public void deleteUser_whenRoleUserMarkDeleted() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        entityManager.clear();

        usersApi.deleteUser(userId, getAsAuthToken(user));
        entityManager.flush();
        entityManager.clear();

        User updated = entityManager.find(User.class, userId);
        assertTrue(updated.getDeleted());
    }

    @Test
    public void deleteUser_whenRoleSellerCreateDeleteRequest() {
        User user = getPrivateSellers(entityManager).get(0);
        Long userId = user.getId(); // first seller is not deleted in data.sql
        entityManager.clear();

        DeleteAccountRequest result = (DeleteAccountRequest) usersApi
                .deleteUser(userId, getAsAuthToken(user)).getBody();
        entityManager.flush();
        entityManager.clear();

        assert result != null;
        assertNotNull(result.getId());

        User updated = entityManager.find(User.class, userId);
        assertFalse(updated.getDeleted());
    }

    @Test
    public void addAddressToUser() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        entityManager.clear();

        AddressDTO dto = new AddressDTO();
        dto.setCountry("de");
        dto.setTitle("int test title");
        dto.setAddressString("address line 1");

        AddressDTO result = usersApi.addAddressToUser(userId, dto, getAsAuthToken(user));
        entityManager.flush();
        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(dto.getTitle(), result.getTitle());
        assertEquals(dto.getAddressString(), result.getAddressString());
    }

    @Test
    public void updateAddress() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        Address address = user.getAddresses().get(0);
        entityManager.clear();

        AddressDTO dto = modelMapper.map(address, AddressDTO.class);
        dto.setTitle("updated title");
        dto.setAddressString("updated address line");

        AddressDTO result = usersApi.updateAddress(userId, address.getId(), dto, getAsAuthToken(user));
        entityManager.flush();

        assertEquals(dto.getId(), result.getId());
        assertEquals(userId, result.getUserId());
        assertEquals(dto.getTitle(), result.getTitle());
        assertEquals(dto.getAddressString(), result.getAddressString());
    }

    @Test
    public void deleteAddress() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        Address address = user.getAddresses().get(0);
        entityManager.clear();

        usersApi.deleteAddress(userId, address.getId(), getAsAuthToken(user));
        entityManager.flush();
        entityManager.clear();

        Address deleted = entityManager.find(Address.class, address.getId());
        assertTrue(deleted.getDeleted());
    }

    @Test
    public void updateCompanyInfo() {
        Company company = entityManager.find(Company.class, 1L);
        CompanyDTO dto = modelMapper.map(company, CompanyDTO.class);
        User user = company.getUser();
        entityManager.clear();

        dto.setCompanyName("updated name");
        dto.setTaxId("updated1111");

        CompanyDTO updated = usersApi.updateCompanyInfo(user.getId(), company.getId(), dto, getAsAuthToken(user));
        entityManager.flush();
        entityManager.clear();

        assertEquals(dto.getTaxId(), updated.getTaxId());
        assertEquals(dto.getCompanyName(), updated.getCompanyName());
    }

    @Test
    public void createShareHolder() {
        Company company = entityManager.find(Company.class, 1L);
        User user = company.getUser();
        entityManager.clear();

        ShareholderDTO dto = new ShareholderDTO();
        dto.setFirstName("sh-fn");
        dto.setLastName("sh-ln");
        dto.setIdentity("id.jpg");

        ShareholderDTO result = usersApi.createShareHolder(user.getId(), dto, getAsAuthToken(user));
        assertNotNull(result.getId());
        assertEquals(dto.getFirstName(), result.getFirstName());
        assertEquals(dto.getIdentity(), result.getIdentity());
        assertEquals(company.getId(), result.getCompanyId());
    }

    @Test
    public void updateShareHolder() {
        Company company = entityManager.find(Company.class, 1L);
        User user = company.getUser();
        Long shareholderId = addShareholder(company);
        entityManager.clear();

        ShareholderDTO dto = new ShareholderDTO();
        dto.setFirstName("updated-fn");
        dto.setLastName("updated-ln");
        dto.setIdentity("updated-id.jpg");

        ShareholderDTO result = usersApi.updateShareHolder(user.getId(), shareholderId, dto, getAsAuthToken(user));
        assertNotNull(result.getId());
        assertEquals(dto.getFirstName(), result.getFirstName());
        assertEquals(dto.getIdentity(), result.getIdentity());
        assertEquals(company.getId(), result.getCompanyId());
    }

    @Test
    public void deleteShareHolder() {
        Company company = entityManager.find(Company.class, 1L);
        User user = company.getUser();
        Long shareholderId = addShareholder(company);
        entityManager.clear();

        usersApi.deleteShareHolder(user.getId(), shareholderId, getAsAuthToken(user));
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(ShareHolder.class, shareholderId));
    }

    private Long addShareholder(Company company) {
        ShareHolder shareHolder = new ShareHolder();
        shareHolder.setCompany(company);

        shareHolder.setFirstName("sh-fn");
        shareHolder.setLastName("sh-ln");
        shareHolder.setIdentity("id.jpg");
        entityManager.persist(shareHolder);

        entityManager.flush();
        return shareHolder.getId();
    }

    @Test
    public void requestSellerApproval() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);

        SellerRequestDto dto = new SellerRequestDto();
        dto.setApplicationType(PRIVATE);
        dto.setUser(modelMapper.map(user, UserDTO.class));
        dto.getUser().setIBAN("DE02040608101214");
        entityManager.clear();

        ApprovalDTO result = usersApi
                .requestSellerApproval(userId, dto, getAsAuthToken(user)).getBody();
        assert result != null;
        assertNotNull(result.getId());
        assertEquals(PENDING, result.getApprovalStatus());
        assertEquals(PRIVATE, result.getApplicationType());
    }

    @Test
    public void getSellerApproval() {
        Long userId = 1L;
        User user = entityManager.find(User.class, userId);
        Approval approval = new Approval();
        approval.setRequester(user);
        approval.setApplicationType(PRIVATE);
        approval.setApprovalStatus(PENDING);
        entityManager.persist(approval);
        entityManager.flush();
        entityManager.clear();

        ApprovalDTO result = usersApi.getSellerApproval(userId, approval.getId(), getAsAuthToken(user));
        assertEquals(userId, result.getRequester().getId());
        assertEquals(PENDING, result.getApprovalStatus());
        assertEquals(PRIVATE, result.getApplicationType());
    }


    private UsernamePasswordAuthenticationToken getAsAuthToken(User user) {
        registerToSecurityContext(user);
        return new UsernamePasswordAuthenticationToken(user, null);
    }
}
