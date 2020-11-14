package main.controller;

import main.api.response.CalendarResponse;
import main.api.response.PostExpandedResponse;
import main.api.response.PostListReponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/search")
    private ResponseEntity<PostListReponse> searchPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "query", required = false) String query) {

        PostListReponse response = postService.searchPosts(offset, limit, query);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/byDate")
    private ResponseEntity<PostListReponse> getPostsByDate(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "date", required = false) String dateQuery) {

        PostListReponse response = postService.getPostsByDate(offset, limit, dateQuery);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/byTag")
    private ResponseEntity<PostListReponse> getPostsByTag(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "tag", required = false) String tag) {

        PostListReponse response = postService.getPostsByTag(offset, limit, tag);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/calendar")
    private ResponseEntity<CalendarResponse> getCalendar(int year) {
        CalendarResponse response = postService.getCalendar(year);
        return new ResponseEntity<CalendarResponse>(response, HttpStatus.OK);
    }

    @GetMapping("/post/{id}")
    private ResponseEntity<PostExpandedResponse> getPost(@PathVariable int id) {
        PostExpandedResponse response = postService.getPost(id);
        return new ResponseEntity<PostExpandedResponse>(response, HttpStatus.OK);
    }
}
