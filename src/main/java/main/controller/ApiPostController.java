package main.controller;

import main.api.request.PostRequest;
import main.api.request.VoteRequest;
import main.api.response.PostExpandedResponse;
import main.api.response.PostListReponse;
import main.api.response.ResultResponse;
import main.service.PostService;
import main.service.VoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/post")
public class ApiPostController {
    private final PostService postService;
    private final VoteService voteService;

    @Autowired
    public ApiPostController(PostService postService, VoteService voteService) {
        this.postService = postService;
        this.voteService = voteService;
    }

    @GetMapping("")
    public ResponseEntity<PostListReponse> getPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "mode", required = false, defaultValue = "recent") String mode) {

        PostListReponse response = postService.getPosts(offset, limit, mode);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PostListReponse> searchPosts(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "query", required = false) String query) {

        PostListReponse response = postService.searchPosts(offset, limit, query);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/byDate")
    public ResponseEntity<PostListReponse> getPostsByDate(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "date", required = false) String dateQuery) {

        PostListReponse response = postService.getPostsByDate(offset, limit, dateQuery);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/byTag")
    public ResponseEntity<PostListReponse> getPostsByTag(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "tag", required = false) String tag) {

        PostListReponse response = postService.getPostsByTag(offset, limit, tag);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/{id}")
    public ResponseEntity<PostExpandedResponse> getPost(@PathVariable int id) {
        PostExpandedResponse response = postService.getPostById(id);
        if (response == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<PostListReponse> getPostsForModeration(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "status", required = false) String status) {

        return ResponseEntity.ok(postService.getPostsForModeration(offset, limit, status));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<PostListReponse> getPostsMy(
            @RequestParam(name = "offset", required = false, defaultValue = "0") int offset,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit,
            @RequestParam(name = "status", required = false) String status) {

        return ResponseEntity.ok(postService.getPostsMy(offset, limit, status));
    }


    @PostMapping("/like")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> like(@RequestBody VoteRequest voteRequest) {
        ResultResponse response = new ResultResponse(
            voteService.like(voteRequest.getPostId()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/dislike")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> dislike(@RequestBody VoteRequest voteRequest) {
        ResultResponse response = new ResultResponse(
                voteService.dislike(voteRequest.getPostId()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> createPost(@RequestBody PostRequest postRequest) {
        return ResponseEntity.ok(postService.createPost(postRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<ResultResponse> changePost(@RequestBody PostRequest postRequest, @PathVariable Integer id) {
        return ResponseEntity.ok(postService.changePost(postRequest, id));
    }
}
