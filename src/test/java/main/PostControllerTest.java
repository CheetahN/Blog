package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.controller.ApiPostController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.yml")
public class PostControllerTest {

    @Autowired
    private ApiPostController apiPostController;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;


    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsRecentTest() throws Exception {
        this.mockMvc.perform(get("/api/post")
                .queryParam("offset", "0")
                .queryParam("mode", "recent")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("15"))
                .andExpect(jsonPath("$.posts[0].id").value("11"))
                .andExpect(jsonPath("$.posts[0].title").value("POST ELEVEN"))
                .andExpect(jsonPath("$.posts[0].user.name").value("Fedor"))
                .andExpect(jsonPath("$.posts[0].likeCount").value("0"))
                .andExpect(jsonPath("$.posts[0].viewCount").value("100"))
                .andExpect(jsonPath("$.posts[0].timestamp").value("1605917152"))
                .andExpect(jsonPath("$.posts[9].viewCount").value("888"))
                .andExpect(jsonPath("$.posts[10].viewCount").doesNotExist());
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsEarlyTest() throws Exception {
        this.mockMvc.perform(get("/api/post")
                .queryParam("offset", "0")
                .queryParam("mode", "early")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("15"))
                .andExpect(jsonPath("$.posts[0].id").value("1"))
                .andExpect(jsonPath("$.posts[0].title").value("POST ONE"))
                .andExpect(jsonPath("$.posts[10].viewCount").doesNotExist());
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql", "/AddTestComments.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsPopularTest() throws Exception {
        this.mockMvc.perform(get("/api/post")
                .queryParam("offset", "0")
                .queryParam("mode", "popular")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("15"))
                .andExpect(jsonPath("$.posts[0].id").value("3"))
                .andExpect(jsonPath("$.posts[0].title").value("POST Three"))
                .andExpect(jsonPath("$.posts[1].id").value("10"))
                .andExpect(jsonPath("$.posts[1].title").value("POST TEN"))
                .andExpect(jsonPath("$.posts[10].viewCount").doesNotExist());
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql", "/AddTestVotes.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsBestTest() throws Exception {
        this.mockMvc.perform(get("/api/post")
                .queryParam("offset", "0")
                .queryParam("mode", "best")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("15"))
                .andExpect(jsonPath("$.posts[0].id").value("10"))
                .andExpect(jsonPath("$.posts[1].id").value("9"))
                .andExpect(jsonPath("$.posts[0].title").value("POST TEN"))
                .andExpect(jsonPath("$.posts[10].viewCount").doesNotExist());
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void searchPostsTest() throws Exception {
        this.mockMvc.perform(get("/api/post/search/")
                .queryParam("offset", "0")
                .queryParam("query", "место")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("2"))
                .andExpect(jsonPath("$.posts[0].id").value("11"))
                .andExpect(jsonPath("$.posts[0].title").value("POST ELEVEN"))
                .andExpect(jsonPath("$.posts[1].title").value("Эклер"))
                .andExpect(jsonPath("$.posts[2]").doesNotExist());
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void searchByDateTest() throws Exception {
        this.mockMvc.perform(get("/api/post/byDate")
                .queryParam("offset", "0")
                .queryParam("date", "2020-11-21")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("2"))
                .andExpect(jsonPath("$.posts[0].id").value("11"))
                .andExpect(jsonPath("$.posts[1].id").value("22"))
                .andExpect(jsonPath("$.posts[0].title").value("POST ELEVEN"))
                .andExpect(jsonPath("$.posts[1].title").value("Булочки"))
                .andExpect(jsonPath("$.posts[2]").doesNotExist());
    }

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql", "/AddTags.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void searchByTagTest() throws Exception {
        this.mockMvc.perform(get("/api/post/byTag")
                .queryParam("offset", "0")
                .queryParam("tag", "F1")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("10"))
                .andExpect(jsonPath("$.posts[0].id").value("22"))
                .andExpect(jsonPath("$.posts[1].id").value("10"))
                .andExpect(jsonPath("$.posts[9].id").value("14"))
                .andExpect(jsonPath("$.posts[1].title").value("POST TEN"))
                .andExpect(jsonPath("$.posts[9].title").value("Булье дали слово"))
                .andExpect(jsonPath("$.posts[0].title").value("Булочки"))
                .andExpect(jsonPath("$.posts[10]").doesNotExist());
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
    @WithUserDetails("anna@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getModeration() throws Exception {
        this.mockMvc.perform(get("/api/post/moderation")
                .queryParam("offset", "0")
                .queryParam("limit", "10")
                .queryParam("status", "declined"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(jsonPath("$.count").value("14"))
                .andExpect(jsonPath("$.posts[0].id").value("37"))
                .andExpect(jsonPath("$.posts[1].id").value("35"))
                .andExpect(jsonPath("$.posts[9].id").value("32"))
                .andExpect(jsonPath("$.posts[10]").doesNotExist());;
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getModerationError() throws Exception {
        this.mockMvc.perform(get("/api/post/moderation")
                .queryParam("offset", "0")
                .queryParam("limit", "10")
                .queryParam("status", "declined"))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isForbidden());
    }
}

