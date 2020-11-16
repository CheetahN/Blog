package main.controller;

import main.api.request.SettingsRequest;
import main.api.response.InitResponse;
import main.api.response.SettingsResponse;
import main.api.response.TagResponse;
import main.service.SettingsService;
import main.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class ApiGeneralController {

    private final InitResponse initResponse;
    private final SettingsService settingsService;
    private final TagService tagService;

    public ApiGeneralController(InitResponse initResponse, SettingsService settingsService, TagService tagService) {
        this.initResponse = initResponse;
        this.settingsService = settingsService;
        this.tagService = tagService;
    }
    @GetMapping("/init")
    private InitResponse init() {
        return initResponse;
    }

    @GetMapping("/settings")
    private SettingsResponse settings() {
        return settingsService.getGlobalSettings();
    }

    @GetMapping("/tag")
    private TagResponse getTag(@RequestParam(name = "query", required = false) String query) {
        return tagService.getTag(query);
    }

    @PutMapping("/settings")
    private ResponseEntity changeSettings(@RequestBody SettingsRequest settingsRequest, HttpSession session) {
        if (settingsService.setGlobalSettings(session.getId(), settingsRequest))
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
