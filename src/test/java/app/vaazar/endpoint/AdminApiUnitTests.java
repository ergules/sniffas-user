package app.vaazar.endpoint;

import app.vaazar.TestDataHelper;
import app.vaazar.config.LoggerProducer;
import app.vaazar.config.ModelMapperConfig;
import app.vaazar.domain.approval.boundary.ApprovalService;
import app.vaazar.domain.approval.entity.Approval;
import app.vaazar.domain.approval.entity.ApprovalStatus;
import app.vaazar.domain.deleteAccount.boundary.DeleteAccountService;
import app.vaazar.domain.deleteAccount.entity.DeleteAccountRequest;
import app.vaazar.domain.deleteAccount.entity.DeleteRequestStatus;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.approval.ApprovalDTO;
import app.vaazar.endpoint.dto.deleteAccount.DeleteAccountRequestDTO;
import app.vaazar.security.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static app.vaazar.TestDataHelper.asJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminApi.class)
public class AdminApiUnitTests {

    @MockBean
    UserService userService;
    @MockBean
    ApprovalService approvalService;
    @MockBean
    DeleteAccountService deleteAccountService;

    @Autowired
    JwtTokenUtil tokenUtil;
    @Autowired
    MockMvc mockMvc;

    final String base = "/admin";

    @Test
    public void whenNoCredentialsReturnHttpUnauthorized() throws Exception {
        RequestBuilder request = get(base + "/users")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().is(401));
    }

    @Test
    public void whenWrongCredentialsReturnHttpForbidden() throws Exception {
        RequestBuilder request = get(base + "/users")
                .header("Authorization", "Bearer " + getSellerToken())
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().is(403));
    }

    @Test
    public void findUsers_noParam() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        Pageable pageable = PageRequest.of(1, 5);
        Page<User> resultPage = new PageImpl<>(List.of(user), pageable, 6);

        when(userService.findUsers(Optional.empty(), false, pageable))
                .thenReturn(resultPage);

        RequestBuilder request = get(base + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getAdminToken())
                .param("size", "5")
                .param("page", "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(user.getId()))
                .andExpect(jsonPath("$.content[0].username").value(user.getUsername()))
                .andExpect(jsonPath("$.content[0].firstname").value(user.getFirstname()))
                .andExpect(jsonPath("$.content[0].profilePhoto").value(user.getProfilePhoto()))
                .andExpect(jsonPath("$.content[1]").doesNotExist())
                .andExpect(jsonPath("$.pageable.page").value(1))
                .andExpect(jsonPath("$.pageable.size").value(5))
                .andExpect(jsonPath("$.total").value(6));
    }

    @Test
    public void findUsers_withParams() throws Exception {
        when(userService.findUsers(eq(Optional.of("_qry")), eq(true), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        RequestBuilder request = get(base + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getAdminToken())
                .param("query", "_qry")
                .param("seller", "1");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    public void findUser() throws Exception {
        User user = new User(13L, "un", "fn", "ln", "pf", Role.USER);
        user.setEmail("u13@mail.com");
        when(userService.findById(user.getId())).thenReturn(user);

        RequestBuilder request = get((base + "/users/{userId}"), user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getAdminToken());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.profilePhoto").value(user.getProfilePhoto()))
                .andExpect(jsonPath("$.username").value(user.getUsername()));
    }

    @Test
    public void findApprovals() throws Exception {
        when(approvalService.listApprovals(Optional.of(ApprovalStatus.REJECTED)))
                .thenReturn(Collections.emptyList());

        RequestBuilder request = get(base + "/approvals")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + getAdminToken());

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void respondToApproval() throws Exception {
        ApprovalDTO adminResponse = new ApprovalDTO();
        adminResponse.setApprovalStatus(ApprovalStatus.APPROVED);
        adminResponse.setDescription("description");

        when(approvalService.respondToSellerApproval(any())).thenAnswer(i -> i.getArguments()[0]);
        ArgumentCaptor<Approval> approvalCaptor = ArgumentCaptor.forClass(Approval.class);

        RequestBuilder request = put(base + "/approvals/7")
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(adminResponse));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.description").value(adminResponse.getDescription()));

        verify(approvalService).respondToSellerApproval(approvalCaptor.capture());
        assertEquals(7L, approvalCaptor.getValue().getId());
        assertEquals(adminResponse.getDescription(), approvalCaptor.getValue().getDescription());
        assertEquals(adminResponse.getApprovalStatus(), approvalCaptor.getValue().getApprovalStatus());
    }

    @Test
    public void getDeleteAccountRequests() throws Exception {
        List<DeleteRequestStatus> statuses = List.of(DeleteRequestStatus.NEW, DeleteRequestStatus.MANUALLY_DELETED);
        DeleteAccountRequest deleteAccountRequest = new DeleteAccountRequest();
        deleteAccountRequest.setId(3L);
        deleteAccountRequest.setStatus(DeleteRequestStatus.NEW);

        when(deleteAccountService.listDeleteRequests(eq(statuses), any()))
                .thenReturn(new PageImpl<>(List.of(deleteAccountRequest)));

        RequestBuilder request = get(base + "/delete-account-requests")
                .header("Authorization", "Bearer " + getAdminToken())
                .param("statuses", "NEW", "MANUALLY_DELETED")
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(deleteAccountRequest.getId()))
                .andExpect(jsonPath("$.content[0].status").value("NEW"))
                .andExpect(jsonPath("$.content[1]").doesNotExist());
    }

    @Test
    public void acceptDeleteRequest() throws Exception {
        DeleteAccountRequestDTO dto = new DeleteAccountRequestDTO();
        dto.setId(5L);
        dto.setAutoDeleteDate(Instant.now());

        when(deleteAccountService.scheduleDelete(any(DeleteAccountRequest.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        RequestBuilder request = put(base + "/delete-account-requests/{id}", dto.getId())
                .header("Authorization", "Bearer " + getAdminToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(dto.getId()))
                .andExpect(jsonPath("$.autoDeleteDate").exists());
    }

    @Test
    public void discardDeleteRequest() throws Exception {
        Long id = 5L;
        RequestBuilder request = delete(base + "/delete-account-requests/{id}", id)
                .header("Authorization", "Bearer " + getAdminToken());

        mockMvc.perform(request).andExpect(status().isOk());

        verify(deleteAccountService).discardRequest(id);
    }


    private String getAdminToken() {
        return tokenUtil.generateAccessToken(
                new User(1L, "admin@sniffas.com", "root", Role.ADMIN));
    }

    private String getSellerToken() {
        return tokenUtil.generateAccessToken(
                new User(5L, "s1@mail.com", "s1", Role.SELLER));
    }


    @TestConfiguration
    static class Config {
        @Bean
        ModelMapper modelMapper() {
            return new ModelMapperConfig().getDefaultMapper();
        }

        @Bean
        @Scope("prototype")
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
