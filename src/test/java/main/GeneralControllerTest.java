package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.ModerationRequest;
import main.api.request.SettingsRequest;
import main.controller.ApiGeneralController;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
import main.repository.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yml")
public class GeneralControllerTest {

    @Autowired
    private ApiGeneralController apiGeneralController;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SettingsRepository settingsRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql", "/AddTags.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getTagQueryTest() throws Exception {
        this.mockMvc.perform(get("/api/tag")
                .queryParam("query", "Java"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags[0].weight").value(1.0))
                .andExpect(jsonPath("$.tags[0].name").value("Java"))
                .andExpect(jsonPath("$.tags", hasSize(1)));
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql", "/AddTags.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getTagTest() throws Exception {
        this.mockMvc.perform(get("/api/tag"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.tags[0].weight").value(1.0))
                .andExpect(jsonPath("$.tags[4].weight").value(0.71428573))
                .andExpect(jsonPath("$.tags[4].name").value("F1"))
                .andExpect(jsonPath("$.tags[3].name").value("Chaos"))
                .andExpect(jsonPath("$.tags[2].name").value("Drama"))
                .andExpect(jsonPath("$.tags[1].name").value("Alcohol"))
                .andExpect(jsonPath("$.tags[0].name").value("Java"))
                .andExpect(jsonPath("$.tags", hasSize(5)));
    }

    @Test
    @Sql(value = {"/AddGlobalSettings.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    public void getSettingsTest() throws Exception {
        this.mockMvc.perform(get("/api/settings/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.MULTIUSER_MODE").value("true"))
                .andExpect(jsonPath("$.STATISTICS_IS_PUBLIC").value("false"))
                .andExpect(jsonPath("$.POST_PREMODERATION").value("false"));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddGlobalSettings.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void putSettingsErrorTest() throws Exception {
        SettingsRequest request = new SettingsRequest();
        request.setMultiuserMode(true);
        request.setPostPremoderation(false);
        request.setStatisticsPuplic(true);
        this.mockMvc.perform(put("/api/settings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden());

    }

    @Test
    @WithUserDetails("anna@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddGlobalSettings.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void putSettingsTest() throws Exception {
        SettingsRequest request = new SettingsRequest();
        request.setMultiuserMode(false);
        request.setPostPremoderation(true);
        request.setStatisticsPuplic(true);
        this.mockMvc.perform(put("/api/settings/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

        assertEquals(settingsRepository.findByCode(GlobalSettingCode.MULTIUSER_MODE).getValue(), GlobalSettingValue.NO);
        assertEquals(settingsRepository.findByCode(GlobalSettingCode.STATISTICS_IS_PUBLIC).getValue(), GlobalSettingValue.YES);
        assertEquals(settingsRepository.findByCode(GlobalSettingCode.POST_PREMODERATION).getValue(), GlobalSettingValue.YES);
    }

    @Test
    public void getInitTest() throws Exception {
        this.mockMvc.perform(get("/api/init/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("DevPub"))
                .andExpect(jsonPath("$.subtitle").value("Trash Book"))
                .andExpect(jsonPath("$.phone").value("+77776543210"))
                .andExpect(jsonPath("$.email").value("efee13413fs@gundex.hru"))
                .andExpect(jsonPath("$.copyright").value("Nikita Stoyan"))
                .andExpect(jsonPath("$.copyrightFrom").value("2021"));
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void calendarTest() throws Exception {
        this.mockMvc.perform(get("/api/calendar")
                .queryParam("year", "2020"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.years", hasSize(2)))
                .andExpect(jsonPath("$.years.[0]").value("2020"))
                .andExpect(jsonPath("$.years.[1]").value("2021"))
                .andExpect(jsonPath("$.posts", aMapWithSize(9)))
                .andExpect(jsonPath("$.posts.2020-03-21").value("2"))
                .andExpect(jsonPath("$.posts.2020-01-21").value("2"))
                .andExpect(jsonPath("$.posts.2020-09-21").value("2"))
                .andExpect(jsonPath("$.posts.2020-10-21").value("1"))
                .andExpect(jsonPath("$.posts.2020-11-21").value("2"));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void postModeration1() throws Exception {
        ModerationRequest request = new ModerationRequest(1, "accept");
        this.mockMvc.perform(post("/api/moderation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isForbidden());
        assertTrue(postRepository.findById(27).isPresent());
        assertEquals(postRepository.findById(27).get().getModerationStatus(), ModerationStatus.NEW);
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void postModeration2() throws Exception {
        ModerationRequest request = new ModerationRequest(1, "accept");
        this.mockMvc.perform(post("/api/moderation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
        assertTrue(postRepository.findById(27).isPresent());
        assertEquals(postRepository.findById(27).get().getModerationStatus(), ModerationStatus.NEW);
    }

    @Test
    @WithUserDetails("anna@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void postModeration3() throws Exception {
        ModerationRequest request = new ModerationRequest(27, "accept");
        this.mockMvc.perform(post("/api/moderation")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("true"));

        assertTrue(postRepository.findById(27).isPresent());
        assertEquals(postRepository.findById(27).get().getModerationStatus(), ModerationStatus.ACCEPTED);
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void calendarNoQueryTest() throws Exception {
        this.mockMvc.perform(get("/api/calendar"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.years", hasSize(2)))
                .andExpect(jsonPath("$.years.[0]").value("2020"))
                .andExpect(jsonPath("$.years.[1]").value("2021"))
                .andExpect(jsonPath("$.posts", anEmptyMap()));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void uploadImageTest() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile("image", "test.png","image/jpeg", "Spring Framework".getBytes());
        MvcResult result = this.mockMvc.perform(multipart("/api/image")
                .file(multipartFile))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.valueOf("text/plain;charset=UTF-8")))
                .andReturn();

        String content = result.getResponse().getContentAsString();
        assertTrue(content.matches("/upload/\\w{2}/\\w{2}/\\w{2}/test.png"));
    }
}
