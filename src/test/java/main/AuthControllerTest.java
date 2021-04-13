package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.LoginRequest;
import main.controller.ApiAuthController;
import main.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yml")
public class AuthControllerTest {
    @Autowired
    private ApiAuthController apiAuthController;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getCheckNotLoggedTest() throws Exception {
        this.mockMvc.perform(get("/api/auth/check"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getCheckLoggedTest() throws Exception {
        this.mockMvc.perform(get("/api/auth/check"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"))
                .andExpect(jsonPath("$.user.id").value("101"))
                .andExpect(jsonPath("$.user.name").value("Pasha"))
                .andExpect(jsonPath("$.user.photo").value("https://99px.ru/sstorage/1/2020/09/image_10709200906261210328.jpg"))
                .andExpect(jsonPath("$.user.email").value("Pasha@mail.ru"))
                .andExpect(jsonPath("$.user.moderation").value("false"))
                .andExpect(jsonPath("$.user.moderationCount").value("0"))
                .andExpect(jsonPath("$.user.settings").value("false"));
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getLoginTest() throws Exception {
        LoginRequest request = new LoginRequest("pasha@mail.ru", "123456");
        this.mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"))
                .andExpect(jsonPath("$.user.id").value("101"))
                .andExpect(jsonPath("$.user.name").value("Pasha"))
                .andExpect(jsonPath("$.user.photo").value("https://99px.ru/sstorage/1/2020/09/image_10709200906261210328.jpg"))
                .andExpect(jsonPath("$.user.email").value("Pasha@mail.ru"))
                .andExpect(jsonPath("$.user.moderation").value("false"))
                .andExpect(jsonPath("$.user.moderationCount").value("0"))
                .andExpect(jsonPath("$.user.settings").value("false"));
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getLoginErrorTest() throws Exception {
        LoginRequest request = new LoginRequest("pasha@mail.ru", "1234567");
        this.mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.user.id").doesNotExist());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getLogoutTest() throws Exception {
        this.mockMvc.perform(get("/api/auth/logout"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"));
    }
}
