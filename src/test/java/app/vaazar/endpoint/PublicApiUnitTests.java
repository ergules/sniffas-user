package app.vaazar.endpoint;

import app.vaazar.TestDataHelper;
import app.vaazar.config.LoggerProducer;
import app.vaazar.config.ModelMapperConfig;
import app.vaazar.config.exception.AuthorisationException;
import app.vaazar.domain.user.boundary.UserService;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import app.vaazar.endpoint.dto.BasicUser;
import app.vaazar.endpoint.dto.RegistrationDto;
import app.vaazar.endpoint.dto.user.UserDTO;
import app.vaazar.security.JwtTokenUtil;
import app.vaazar.service.firebase.FirebaseAuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.List;
import java.util.Optional;

import static app.vaazar.TestDataHelper.asJson;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicApi.class)
public class PublicApiUnitTests {

    @MockBean
    UserService userService;
    @MockBean
    FirebaseAuthService firebaseService;
    @Autowired
    MockMvc mockMvc;
    @Captor
    ArgumentCaptor<User> userCaptor;

    final String base = "/users/public";

    @Test
    public void loginWithFirebaseToken_happy() throws Exception {
        String fbToken = "fb-token";
        String uid = "uid";
        Long userId = 37L;
        String username = "un";
        String email = "37@mail.com";
        User user = new User(userId, email, username, Role.USER);

        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        when(firebaseService.verifyIdToken(fbToken)).thenReturn(token);
        when(userService.findByUid(uid)).thenReturn(Optional.of(user));

        RequestBuilder request = post(base + "/login")
                .content(fbToken);

        mockMvc.perform(request)
                .andExpect(header().exists(HttpHeaders.AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    public void loginWithFirebaseToken_whenUidNotFoundReturn404() throws Exception {
        String fbToken = "fb-token";
        String uid = "uid";

        FirebaseToken token = mock(FirebaseToken.class);
        when(token.getUid()).thenReturn(uid);
        when(firebaseService.verifyIdToken(fbToken)).thenReturn(token);
        when(userService.findByUid(uid)).thenReturn(Optional.empty());

        RequestBuilder request = post(base + "/login")
                .content(fbToken);

        mockMvc.perform(request)
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(status().is(404));
    }

    @Test
    public void loginWithFirebaseToken_whenUidNotValidReturn403() throws Exception {

        when(firebaseService.verifyIdToken(any())).thenThrow(new AuthorisationException("fail"));

        RequestBuilder request = post(base + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .content("fb-token");

        mockMvc.perform(request)
                .andExpect(header().doesNotExist(HttpHeaders.AUTHORIZATION))
                .andExpect(status().is(403))
                .andReturn();
    }

    @Test
    public void registerWithFirebaseToken_happy() throws Exception {

        long userId = 37L;
        String username = "un";
        String email = "37@mail.com";
        String firstname = "fn";
        String lastname = "ln";
        String fbToken = "fb-token";

        UserDTO userDTO = new UserDTO();
        userDTO.setId(userId + 15);
        userDTO.setUsername(username);
        userDTO.setEmail(email);
        userDTO.setFirstname(firstname);
        userDTO.setLastname(lastname);
        userDTO.setRole(Role.ADMIN);

        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setToken(fbToken);
        registrationDto.setUser(userDTO);

        User user = new User(userId, email, username, Role.USER);

        FirebaseToken token = mock(FirebaseToken.class);
        when(firebaseService.verifyIdToken(fbToken)).thenReturn(token);
        when(userService.registerUser(any(), eq(token))).thenReturn(user);


        RequestBuilder request = post(base + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(registrationDto));

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.username").value(username));

        verify(userService).registerUser(userCaptor.capture(), any());
        assertEquals(userDTO.getRole(), userCaptor.getValue().getRole());
        assertEquals(userDTO.getUsername(), userCaptor.getValue().getUsername());
    }

    @Test
    public void registerWithFirebaseToken_invalidUser() throws Exception {
        String username = "un";
        String invalidEmail = "37QMail.com";
        String firstname = "fn";
        String lastname = "ln";
        String fbToken = "fb-token";

        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(username);
        userDTO.setEmail(invalidEmail);
        userDTO.setFirstname(firstname);
        userDTO.setLastname(lastname);

        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setToken(fbToken);
        registrationDto.setUser(userDTO);

        FirebaseToken token = mock(FirebaseToken.class);
        when(firebaseService.verifyIdToken(fbToken)).thenThrow(new AuthorisationException("inv tkn"));
        when(userService.registerUser(any(), eq(token))).thenReturn(null);


        RequestBuilder request = post(base + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJson(registrationDto));

        mockMvc.perform(request)
                .andExpect(status().is(406));
    }

    @Test
    public void isUsernameAvailable() throws Exception {
        when(userService.isUsernameAvailable(any())).thenReturn(true);
        when(userService.isUsernameAvailable("existent")).thenReturn(false);

        RequestBuilder existingReq = get(base + "/availableUsernames")
                .contentType(MediaType.APPLICATION_JSON)
                .param("key", "existent");

        mockMvc.perform(existingReq)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));

        RequestBuilder nonExistentReq = get(base + "/availableUsernames")
                .contentType(MediaType.APPLICATION_JSON)
                .param("key", "other");

        mockMvc.perform(nonExistentReq)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    public void getBasicUsers() throws Exception {
        BasicUser u = new BasicUser(13L, "un", "fn", "ln", "pf", "USER");

        when(userService.findBasicUsers(List.of(11L, 13L, 15L))).thenReturn(List.of(u));

        RequestBuilder request = post(base + "/userInfo")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[11,13,15]");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(u.getId()))
                .andExpect(jsonPath("$.[0].username").value(u.getUsername()))
                .andExpect(jsonPath("$.[0].firstname").value(u.getFirstname()))
                .andExpect(jsonPath("$.[0].profilePhoto").value(u.getProfilePhoto()))
                .andExpect(jsonPath("$.[1]").doesNotExist());
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
