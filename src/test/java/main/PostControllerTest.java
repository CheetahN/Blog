package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.controller.ApiPostController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
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
    @Sql(value = {"/Clear.sql"}, executionPhase = AFTER_TEST_METHOD)
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
    @Sql(value = {"/Clear.sql"}, executionPhase = AFTER_TEST_METHOD)
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
    @Sql(value = {"/Clear.sql"}, executionPhase = AFTER_TEST_METHOD)
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
    @Sql(value = {"/Clear.sql"}, executionPhase = AFTER_TEST_METHOD)
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
}
