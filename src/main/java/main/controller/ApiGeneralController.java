package main.controller;

import main.api.request.ModerationRequest;
import main.api.request.SettingsRequest;
import main.api.response.*;
import main.service.FileService;
import main.service.PostService;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagService tagService;
    private final PostService postService;
    private final FileService fileService;

    public ApiGeneralController(InitResponse initResponse, SettingsService settingsService, TagService tagService, PostService postService, FileService fileService) {
        this.initResponse = initResponse;
        this.settingsService = settingsService;
        this.tagService = tagService;
        this.postService = postService;
        this.fileService = fileService;
    }
    @GetMapping("/init")
    public ResponseEntity<InitResponse> init() {
        return ResponseEntity.ok(initResponse);
    }

    @GetMapping("/settings")
    public ResponseEntity<SettingsResponse> settings() {
        return ResponseEntity.ok(settingsService.getGlobalSettings());
    }

    @GetMapping("/tag")
    public ResponseEntity<TagResponse>  getTag(@RequestParam(name = "query", required = false) String query) {
        return ResponseEntity.ok(tagService.getTag(query));
    }

    @PutMapping("/settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity changeSettings(@RequestBody SettingsRequest settingsRequest) {
        if (settingsService.setGlobalSettings(settingsRequest))
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/moderation")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<ResultResponse> moderate(@RequestBody ModerationRequest moderationRequest) {
        ResultResponse response = new ResultResponse(
                postService.moderate(moderationRequest.getPostId(), moderationRequest.getDecision()));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/calendar")
    public ResponseEntity<CalendarResponse> getCalendar(Integer year) {
        CalendarResponse response = postService.getCalendar(year);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/image")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<?> submitImage(@RequestParam MultipartFile image) {
        Object result = fileService.uploadFile(image);
        if (result instanceof String) return ResponseEntity.ok((String) result);
        return ResponseEntity.ok(new ResultResponse(false, (Map<String,String>) result));
    }
}
