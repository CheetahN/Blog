package main.controller;

import main.api.request.ModerationRequest;
import main.api.request.VoteRequest;
import main.api.response.CalendarResponse;
import main.api.response.PostExpandedResponse;
import main.api.response.PostListReponse;
import main.api.response.ResultResponse;
import main.service.PostService;
import main.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ApiPostController {
    private final PostService postService;
    private final VoteService voteService;

    @Autowired
    public ApiPostController(PostService postService, VoteService voteService) {
        this.postService = postService;
        this.voteService = voteService;
    }

    @GetMapping("/post")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostListReponse> getPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "mode", required = false, defaultValue = "recent") String mode) {

        PostListReponse response = postService.getPosts(offset, limit, mode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/search")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostListReponse> searchPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "query", required = false) String query) {

        PostListReponse response = postService.searchPosts(offset, limit, query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/byDate")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostListReponse> getPostsByDate(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "date", required = false) String dateQuery) {

        PostListReponse response = postService.getPostsByDate(offset, limit, dateQuery);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/byTag")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostListReponse> getPostsByTag(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "tag", required = false) String tag) {

        PostListReponse response = postService.getPostsByTag(offset, limit, tag);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<CalendarResponse> getCalendar(int year) {
        CalendarResponse response = postService.getCalendar(year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostExpandedResponse> getPost(HttpSession session, @PathVariable int id) {
        PostExpandedResponse response = postService.getPostById(id, session.getId());
        if (response == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("post/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<PostListReponse> getPostsForModeration(
            HttpSession session,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "status", required = false) String status) {

        PostListReponse response = postService.getPostsForModeration(offset, limit, status, session.getId());
        if (response == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostListReponse> getPostsMy(
            HttpSession session,
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "status", required = false) String status) {

        PostListReponse response = postService.getPostsMy(offset, limit, status, session.getId());
        if (response == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> moderate(HttpSession session, @RequestBody ModerationRequest moderationRequest) {
        ResultResponse response = new ResultResponse(
                postService.moderate(session.getId(), moderationRequest.getPostId(), moderationRequest.getDecision()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> like(HttpSession session, @RequestBody VoteRequest voteRequest) {
        ResultResponse response = new ResultResponse(
            voteService.like(session.getId(), voteRequest.getPostId()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/post/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> dislike(HttpSession session, @RequestBody VoteRequest voteRequest) {
        ResultResponse response = new ResultResponse(
                voteService.dislike(session.getId(), voteRequest.getPostId()));
        return ResponseEntity.ok(response);
    }
}
