package app.vaazar.endpoint;

import app.vaazar.endpoint.dto.BasicUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PublicApiIntegrationTests {

    @Autowired
    PublicApi publicApi;

    @Test
    public void isUsernameAvailable() {
        assertFalse(publicApi.isUsernameAvailable("uu1"));
        assertTrue(publicApi.isUsernameAvailable("uu1_"));
        assertFalse(publicApi.isUsernameAvailable("ss3"));
        assertTrue(publicApi.isUsernameAvailable("uu3_"));
    }

    @Test
    public void userInfo() {
        List<BasicUser> resultList = publicApi.getBasicUsers(List.of(1L,2L,3L,4L,5L));
        assertEquals(3, resultList.size());
        assertNotNull(resultList.get(0).getFirstname());
        assertNotNull(resultList.get(1).getUsername());
    }

}
