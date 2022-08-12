package app.vaazar.endpoint;

import app.vaazar.TestDataHelper;
import app.vaazar.config.LoggerProducer;
import app.vaazar.config.ModelMapperConfig;
import app.vaazar.domain.address.boundary.AddressService;
import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.approval.boundary.ApprovalService;
import app.vaazar.domain.approval.entity.ApplicationType;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.entity.CompanyType;
import app.vaazar.domain.upload.entity.UploadType;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.SellerRequestDto;
import app.vaazar.endpoint.dto.address.AddressDTO;
import app.vaazar.endpoint.dto.company.CompanyDTO;
import app.vaazar.endpoint.dto.company.ShareholderDTO;
import app.vaazar.endpoint.dto.upload.UploadDTO;
import app.vaazar.endpoint.dto.user.UserDTO;
import app.vaazar.security.JwtTokenUtil;
import app.vaazar.service.FileStorage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InjectionPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;

import static app.vaazar.TestDataHelper.asJson;
import static app.vaazar.TestDataHelper.deepCopy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsersApi.class)
public class UsersApiUnitTests {

    @MockBean
    UserService userService;
    @MockBean
    CompanyService companyService;
    @MockBean
    AddressService addressService;
    @MockBean
    FileStorage fileStorage;
    @MockBean
    ApprovalService approvalService;

    @Autowired
    JwtTokenUtil tokenUtil;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    MockMvc mockMvc;

    final String base = "/users";

    @Test
    public void getUser_returnAllFieldsWhenUsersMatch() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.COMPANY);
        user.setEmail("u13@mail.com");
        when(userService.findById(user.getId())).thenReturn(user);

        RequestBuilder request = get((base + "/{userId}"), user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.profilePhoto").value(user.getProfilePhoto()))
                .andExpect(jsonPath("$.role").value(user.getRole().name()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    public void getUser_whenUserIsWrong_thenThrowAccessDeniedException() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        user.setEmail("u13@mail.com");
        when(userService.findById(user.getId())).thenReturn(user);

        User user1 = deepCopy(UserDTO.class, user);
        user1.setId(1L);

        RequestBuilder request = get((base + "/{userId}"), user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user1));

        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void updateUser_happy() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstname("fn");
        userDTO.setLastname("ln");
        userDTO.setEmail("us@mail.com");
        userDTO.setId(1L);
        userDTO.setUsername("us1");

        when(userService.updateUserInfo(any())).thenAnswer(i -> i.getArguments()[0]);

        RequestBuilder request = put((base + "/{userId}"), userDTO.getId())
                .header("Authorization", "Bearer " + generateToken(modelMapper.map(userDTO, User.class)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(userDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDTO.getId()))
                .andExpect(jsonPath("$.email").value(userDTO.getEmail()))
                .andExpect(jsonPath("$.firstname").value(userDTO.getFirstname()));
    }

    @Test
    public void updateUser_whenMissingInfoFailValidation() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstname("fn");
        userDTO.setLastname("ln");
        userDTO.setEmail("us@mail.com");
        userDTO.setId(1L);
        userDTO.setUsername(null);

        RequestBuilder request = put((base + "/{userId}"), userDTO.getId())
                .header("Authorization", "Bearer " + generateToken(modelMapper.map(userDTO, User.class)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(userDTO));

        mockMvc.perform(request)
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void deleteUser() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        when(userService.findById(user.getId())).thenReturn(null);

        RequestBuilder request = delete(base + "/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().isOk());
    }

    @Test
    public void deleteUser_whenWrongUserReturnForbidden() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        when(userService.findById(user.getId())).thenReturn(null);

        RequestBuilder request = delete(base + "/{userId}", user.getId() + 1)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void addAddressToUser() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressString("as");
        addressDTO.setTitle("t");
        addressDTO.setCountry("de");

        when(addressService.addAddressToUser(any(), eq(user.getId()))).thenAnswer(i ->
        {
            ((Address) i.getArguments()[0]).setId(5L);
            return i.getArguments()[0];
        });

        RequestBuilder request = post(base + "/{userId}/addresses", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(addressDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(addressDTO.getTitle()))
                .andExpect(jsonPath("$.country").value(addressDTO.getCountry()))
                .andExpect(jsonPath("$.addressString").value(addressDTO.getAddressString()));
    }

    @Test
    public void updateAddress() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        AddressDTO addressDTO = new AddressDTO();
        addressDTO.setAddressString("as");
        addressDTO.setTitle("t");
        addressDTO.setCountry("de");
        addressDTO.setId(5L);

        when(addressService.updateAddress(any(), eq(user.getId()))).thenAnswer(i -> i.getArguments()[0]);

        RequestBuilder request = put(base + "/{userId}/addresses/{addressId}", user.getId(), addressDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(addressDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(addressDTO.getTitle()))
                .andExpect(jsonPath("$.country").value(addressDTO.getCountry()))
                .andExpect(jsonPath("$.addressString").value(addressDTO.getAddressString()));
    }

    @Test
    public void deleteAddress() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);

        RequestBuilder request = delete(base + "/{userId}/addresses/{addressId}", user.getId(), 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(addressService).deleteAddress(5L, user.getId());
    }

    @Test
    public void updateCompanyInfo() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        Long companyId = 5L;
        CompanyDTO companyDTO = new CompanyDTO();
        companyDTO.setCompanyName("cn");
        companyDTO.setCompanyType(CompanyType.T00);
        companyDTO.setTaxId("11111111111");
        companyDTO.setAddress(new AddressDTO());


        when(companyService.updateCompany(any(), eq(user.getId()))).thenAnswer(i -> i.getArguments()[0]);

        RequestBuilder request = put(base + "/{userId}/companies/{companyId}", user.getId(), companyId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(companyDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(companyId))
                .andExpect(jsonPath("$.taxId").value(companyDTO.getTaxId()))
                .andExpect(jsonPath("$.companyName").value(companyDTO.getCompanyName()))
                .andExpect(jsonPath("$.companyType").value(companyDTO.getCompanyType().name()));
    }

    @Test
    public void createShareHolder() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        ShareholderDTO shareholderDTO = new ShareholderDTO();
        shareholderDTO.setFirstName("sh-fn");
        shareholderDTO.setLastName("sh-ln");
        shareholderDTO.setIdentity("id.jpg");

        when(companyService.createShareHolder(any(), eq(user.getId()))).thenAnswer(i -> i.getArguments()[0]);

        RequestBuilder request = post(base + "/{userId}/share-holders", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(shareholderDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value(shareholderDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(shareholderDTO.getLastName()))
                .andExpect(jsonPath("$.identity").value(shareholderDTO.getIdentity()));
    }

    @Test
    public void updateShareHolder() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        Long shareholderId = 5L;
        ShareholderDTO shareholderDTO = new ShareholderDTO();
        shareholderDTO.setFirstName("sh-fn");
        shareholderDTO.setLastName("sh-ln");
        shareholderDTO.setIdentity("id.jpg");

        when(companyService.updateShareHolder(any(), eq(user.getId()))).thenAnswer(i -> i.getArguments()[0]);

        RequestBuilder request = put(base + "/{userId}/share-holders/{shareHolderId}", user.getId(), shareholderId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(shareholderDTO));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(shareholderId))
                .andExpect(jsonPath("$.firstName").value(shareholderDTO.getFirstName()))
                .andExpect(jsonPath("$.lastName").value(shareholderDTO.getLastName()))
                .andExpect(jsonPath("$.identity").value(shareholderDTO.getIdentity()));
    }

    @Test
    public void deleteShareHolder() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);

        RequestBuilder request = delete(base + "/{userId}/share-holders/{shareHolderId}", user.getId(), 5L)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(companyService).deleteShareHolder(5L, user.getId());
    }

    @Test
    public void getSellerApprovals() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);

        Approval approval = new Approval();
        approval.setId(3L);
        approval.setRequester(user);
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.PRIVATE);

        when(approvalService.getApprovalsOfUser(user.getId()))
                .thenReturn(List.of(approval));

        RequestBuilder request = get(base + "/{userId}/seller-requests", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(approval.getId()))
                .andExpect(jsonPath("$.[0].requester.id").value(user.getId()))
                .andExpect(jsonPath("$.[0].applicationType").value(approval.getApplicationType().name()))
                .andExpect(jsonPath("$.[1]").doesNotExist());
    }

    @Test
    public void requestSellerApproval() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);

        Approval approval = new Approval();
        approval.setId(3L);
        approval.setRequester(user);
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.PRIVATE);

        SellerRequestDto dto = new SellerRequestDto();
        dto.setApplicationType(approval.getApplicationType());
        dto.setUser(modelMapper.map(user, UserDTO.class));

        when(approvalService.requestSellerApproval(eq(approval.getApplicationType()), any(User.class)))
                .thenReturn(approval);

        RequestBuilder request = post(base + "/{userId}/seller-requests", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(approval.getId()))
                .andExpect(jsonPath("$.requester.id").value(user.getId()))
                .andExpect(jsonPath("$.applicationType").value(approval.getApplicationType().name()));
    }

    @Test
    public void getSellerApproval_whenWrongUserThenReturnForbidden() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        User user2 = deepCopy(UserDTO.class, user);
        user2.setId(18L);

        Approval approval = new Approval();
        approval.setId(3L);
        approval.setRequester(user2);

        when(approvalService.getApproval(approval.getId())).thenReturn(approval);

        RequestBuilder request = get(base + "/{userId}/seller-requests/{approvalId}", user.getId(), approval.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void getSellerApproval_happy() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);

        Approval approval = new Approval();
        approval.setId(3L);
        approval.setRequester(user);
        approval.setApprovalStatus(ApprovalStatus.PENDING);
        approval.setApplicationType(ApplicationType.PRIVATE);

        when(approvalService.getApproval(approval.getId())).thenReturn(approval);

        RequestBuilder request = get(base + "/{userId}/seller-requests/{approvalId}", user.getId(), approval.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(approval.getId()))
                .andExpect(jsonPath("$.requester.id").value(user.getId()))
                .andExpect(jsonPath("$.applicationType").value(approval.getApplicationType().name()));
    }

    @Test
    public void getPresignedLink() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);

        UploadDTO dto = new UploadDTO();
        dto.setExtension("jpg");
        dto.setUploadType(UploadType.GM_IDENTITY);

        when(fileStorage.preSignWithObjectKey(any(), eq(dto.getUploadType()))).thenReturn("link");

        RequestBuilder request = post(base + "/sign-upload-link")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + generateToken(user))
                .content(asJson(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").exists())
                .andExpect(jsonPath("$.signedURL").value("link"));
    }


    private String generateToken(User user) {
        return tokenUtil.generateAccessToken(user);
    }

    @TestConfiguration
    static class Config {
        @Bean
        ModelMapper modelMapper() {
            return new ModelMapperConfig().getDefaultMapper();
        }

        @Bean
        Logger logger(InjectionPoint injectionPoint) {
            return new LoggerProducer().logger(injectionPoint);
        }

        @Bean
        JwtTokenUtil jwtTokenUtil(@Value("${app.jwt-secret}") String jwtSecret) {
            return new JwtTokenUtil(jwtSecret, LoggerFactory.getLogger(JwtTokenUtil.class));
        }

        @Bean
        ObjectMapper objectMapper() {
            return TestDataHelper.getObjectMapper();
        }
    }
}
