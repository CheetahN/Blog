package main.controller;

import main.api.request.AllPostsRequest;
import main.api.response.CalendarResponse;
import main.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiPostController {
    private final PostService postService;

    public ApiPostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/post")
    private ResponseEntity<Map<String, Object>> getPosts(AllPostsRequest allPostsRequest) {
        Map<String, Object> response = postService.getPosts(allPostsRequest.getOffset(), allPostsRequest.getLimit(), allPostsRequest.getMode(), allPostsRequest.getQuery(), null, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/search")
    private ResponseEntity<Map<String, Object>> postsSearch(AllPostsRequest allPostsRequest) {
        Map<String, Object> response = postService.getPosts(allPostsRequest.getOffset(), allPostsRequest.getLimit(), "recent", allPostsRequest.getQuery(), null, null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/byDate")
    private ResponseEntity<Map<String, Object>> postsByDate(AllPostsRequest allPostsRequest) {
        Map<String, Object> response = postService.getPosts(allPostsRequest.getOffset(), allPostsRequest.getLimit(), "recent", null, allPostsRequest.getDate(), null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/post/byTag")
    private ResponseEntity<Map<String, Object>> postsByTag(AllPostsRequest allPostsRequest) {
        Map<String, Object> response = postService.getPosts(allPostsRequest.getOffset(), allPostsRequest.getLimit(), "recent", null, null, allPostsRequest.getTag());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/calendar")
    private ResponseEntity<CalendarResponse> getCalendar(int year) {
        CalendarResponse response = postService.getCalendar(year);
        return new ResponseEntity<CalendarResponse>(response, HttpStatus.OK);
    }
}
