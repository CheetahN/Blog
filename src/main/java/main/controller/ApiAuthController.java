package main.controller;

import main.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    private AuthService authService;

    public ApiAuthController(AuthService authService) {
        this.authService = authService;
    }

//    @GetMapping("check")
//    private Map<String, Object> check() {
//        return authService.check();
//    }
}
