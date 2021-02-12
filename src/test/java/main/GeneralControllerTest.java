package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.SettingsRequest;
import main.controller.ApiGeneralController;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.model.enums.ModerationStatus;
import main.repository.SettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
}
