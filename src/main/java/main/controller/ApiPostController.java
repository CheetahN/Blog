package main.controller;

import main.api.response.CalendarResponse;
import main.api.response.PostExpandedResponse;
import main.api.response.PostListReponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ApiPostController {
    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/post")
    private ResponseEntity<PostListReponse> getPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "mode", required = false, defaultValue = "recent") String mode) {

        PostListReponse response = postService.getPosts(offset, limit, mode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/search")
    private ResponseEntity<PostListReponse> searchPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "query", required = false) String query) {

        PostListReponse response = postService.searchPosts(offset, limit, query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/byDate")
    private ResponseEntity<PostListReponse> getPostsByDate(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "date", required = false) String dateQuery) {

        PostListReponse response = postService.getPostsByDate(offset, limit, dateQuery);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/byTag")
    private ResponseEntity<PostListReponse> getPostsByTag(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "tag", required = false) String tag) {

        PostListReponse response = postService.getPostsByTag(offset, limit, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    private ResponseEntity<CalendarResponse> getCalendar(int year) {
        CalendarResponse response = postService.getCalendar(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{id}")
    private ResponseEntity<PostExpandedResponse> getPost(HttpSession session, @PathVariable int id) {
        PostExpandedResponse response = postService.getPost(id, session.getId());
        if (response == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("post/moderation")
    private ResponseEntity<PostListReponse> getPostsForModeration(
            HttpSession session,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "status", required = false) String status) {

        PostListReponse response = postService.getPostsForModeration(offset, limit, status, session.getId());
        if (response == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(response);
    }
}
