package main;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.CommentRequest;
import main.api.request.PostRequest;
import main.controller.ApiPostController;
import main.model.Comment;
import main.model.Post;
import main.model.Tag;
import main.model.TagToPost;
import main.model.enums.ModerationStatus;
import main.repository.CommentRepository;
import main.repository.PostRepository;
import main.repository.TagRepository;
import main.repository.TagToPostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static main.service.TimeService.getTimestamp;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagToPostRepository tagToPostRepository;
    @Autowired
    private CommentRepository commentRepository;


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
    @WithUserDetails("anna@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getModerationDeclinedTest() throws Exception {
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
    @WithUserDetails("anna@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getModerationNewTest() throws Exception {
        this.mockMvc.perform(get("/api/post/moderation")
                .queryParam("offset", "0")
                .queryParam("limit", "10")
                .queryParam("status", "new"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(jsonPath("$.count").value("5"))
                .andExpect(jsonPath("$.posts[0].id").value("27"))
                .andExpect(jsonPath("$.posts[4].id").value("23"));

    }

    @Test
    @WithUserDetails("anna@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getModerationAcceptedTest() throws Exception {
        this.mockMvc.perform(get("/api/post/moderation")
                .queryParam("offset", "0")
                .queryParam("limit", "10")
                .queryParam("status", "accepted"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(jsonPath("$.count").value("1"))
                .andExpect(jsonPath("$.posts[0].id").value("42"));
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

    @Test
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql", "/AddLikes.sql", "/AddTags.sql", "/AddTestComments.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostIdTest() throws Exception {
        this.mockMvc.perform(get("/api/post/10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.timestamp").value("1603238752"))
                .andExpect(jsonPath("$.active").value("true"))
                .andExpect(jsonPath("$.user.name").value("Fedor"))
                .andExpect(jsonPath("$.title").value("POST TEN"))
                .andExpect(jsonPath("$.text", startsWith("Даниэль Риккардо: У меня хорошие воспоминания ")))
                .andExpect(jsonPath("$.likeCount").value(5))
                .andExpect(jsonPath("$.dislikeCount").value(2))
                .andExpect(jsonPath("$.tags[0]").value("Java"))
                .andExpect(jsonPath("$.tags[1]").value("F1"))
                .andExpect(jsonPath("$.comments[0].id").value(5))
                .andExpect(jsonPath("$.comments[1].id").value(6))
                .andExpect(jsonPath("$.viewCount").value(1000));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsMyPendingTest() throws Exception {
        this.mockMvc.perform(get("/api/post/my")
                .queryParam("offset", "0")
                .queryParam("status", "pending")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("5"))
                .andExpect(jsonPath("$.posts[0].id").value("27"))
                .andExpect(jsonPath("$.posts[4].id").value("23"));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsMyPublishedTest() throws Exception {
        this.mockMvc.perform(get("/api/post/my")
                .queryParam("offset", "0")
                .queryParam("status", "published")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("5"))
                .andExpect(jsonPath("$.posts[0].id").value("9"))
                .andExpect(jsonPath("$.posts[4].id").value("12"));
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddTestPosts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getPostsMyDeclinedTest() throws Exception {
        this.mockMvc.perform(get("/api/post/my")
                .queryParam("offset", "0")
                .queryParam("status", "declined")
                .queryParam("limit", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").value("14"))
                .andExpect(jsonPath("$.posts[0].id").value("37"))
                .andExpect(jsonPath("$.posts[9].id").value("32"))
                .andExpect(jsonPath("$.posts[10]").doesNotExist());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createPost() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("F1");
        tags.add("MOLOKO");
        PostRequest request = new PostRequest(
                12222222223L,
                (byte) 1,
                "start",
                tags,
                "test text TEXT text 1234567890 1234567890 1234567890 1234567890");
        this.mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"))
                .andExpect(jsonPath("$.errors").doesNotExist());

        List<Post> posts = postRepository.findAll();
        assertEquals(1, posts.size());
        assertEquals(request.getActive(), posts.get(0).getIsActive());
        assertEquals(request.getTimestamp(), getTimestamp(posts.get(0).getTime()));
        assertEquals(request.getText(), posts.get(0).getText());
        assertEquals(request.getTitle(), posts.get(0).getTitle());
        assertEquals(ModerationStatus.NEW, posts.get(0).getModerationStatus());
        List<Tag> tagsResult = tagRepository.findAll();
        assertEquals(2, tagsResult.size());
        assertEquals(tags.get(0), tagsResult.get(0).getName());
        assertEquals(tags.get(1), tagsResult.get(1).getName());
        List<TagToPost> tagsToPostResult = tagToPostRepository.findAll();
        assertEquals(2, tagsToPostResult.size());
        assertEquals(posts.get(0).getId(), tagsToPostResult.get(0).getPost().getId());
        assertEquals(posts.get(0).getId(), tagsToPostResult.get(1).getPost().getId());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createPostPastTime() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("F1");
        tags.add("MOLOKO");
        PostRequest request = new PostRequest(
                1222223L,
                (byte) 1,
                "start",
                tags,
                "test text TEXT text 1234567890 1234567890 1234567890 1234567890");
        this.mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"))
                .andExpect(jsonPath("$.errors").doesNotExist());

        List<Post> posts = postRepository.findAll();
        assertEquals(1, posts.size());
        assertEquals(Instant.now().getEpochSecond(), getTimestamp(posts.get(0).getTime()), 10);
        assertEquals(request.getText(), posts.get(0).getText());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createPostError() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("F1");
        tags.add("MOLOKO");
        PostRequest request = new PostRequest(
                12222222223L,
                (byte) 1,
                "s",
                tags,
                "");
        this.mockMvc.perform(post("/api/post")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors.title").value("Заголовок слишком короткий"))
                .andExpect(jsonPath("$.errors.text").value("Поле текст не заполнено"));

        assertEquals(0, postRepository.findAll().size());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddPost.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void changePostError() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("F1");
        tags.add("MOLOKO");
        PostRequest request = new PostRequest(
                12222222223L,
                (byte) 1,
                "s",
                tags,
                "");
        this.mockMvc.perform(put("/api/post/42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("false"))
                .andExpect(jsonPath("$.errors.title").value("Заголовок слишком короткий"))
                .andExpect(jsonPath("$.errors.text").value("Поле текст не заполнено"));

        assertEquals(1, postRepository.findAll().size());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddPost.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void changePost() throws Exception {
        List<String> tags = new ArrayList<>();
        tags.add("F1");
        tags.add("MOLOKO");
        PostRequest request = new PostRequest(
                12222222223L,
                (byte) 1,
                "start",
                tags,
                "test text TEXT text 1234567890 1234567890 1234567890 1234567890");
        this.mockMvc.perform(put("/api/post/42")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.result").value("true"))
                .andExpect(jsonPath("$.errors").doesNotExist());

        List<Post> posts = postRepository.findAll();
        assertEquals(1, posts.size());
        assertEquals(request.getActive(), posts.get(0).getIsActive());
        assertEquals(request.getTimestamp(), getTimestamp(posts.get(0).getTime()));
        assertEquals(request.getText(), posts.get(0).getText());
        assertEquals(request.getTitle(), posts.get(0).getTitle());
        assertEquals(ModerationStatus.NEW, posts.get(0).getModerationStatus());
        List<Tag> tagsResult = tagRepository.findAll();
        tagsResult.forEach(System.out::println);
        assertEquals(3, tagsResult.size());
        assertEquals(tags.get(0), tagsResult.get(1).getName());
        assertEquals(tags.get(1), tagsResult.get(2).getName());
        List<TagToPost> tagsToPostResult = tagToPostRepository.findAll();
        assertEquals(2, tagsToPostResult.size());
        assertEquals(posts.get(0).getId(), tagsToPostResult.get(0).getPost().getId());
        assertEquals(posts.get(0).getId(), tagsToPostResult.get(1).getPost().getId());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddPost.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addCommentToPostTest() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setPostId(42);
        request.setText("krutota!");
        this.mockMvc.perform(post("/api/comment/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());

        Comment comment = commentRepository.findAll().get(0);
        assertEquals(request.getText(), comment.getText());
        assertEquals(request.getPostId(), comment.getPost().getId());
        assertNull(comment.getComment());
    }

    @Test
    @WithUserDetails("pasha@mail.ru")
    @Sql(value = {"/AddTestUsers.sql", "/AddPost.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/Clear.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addCommentToPostErrorTest() throws Exception {
        CommentRequest request = new CommentRequest();
        request.setPostId(42);
        request.setText("kr");
        this.mockMvc.perform(post("/api/comment/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(authenticated())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("errors.text").value("Текст комментария не задан или слишком короткий"));

        assertTrue(commentRepository.findAll().isEmpty());
    }
}

