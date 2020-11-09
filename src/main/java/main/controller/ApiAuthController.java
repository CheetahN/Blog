package main.controller;

import main.api.request.LoginRequest;
import main.api.response.AuthResponse;
import main.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    private AuthService authService;


    public ApiAuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("check")
    private AuthResponse check(HttpSession session) {
        return authService.check(session.getId());
    }

    @PostMapping("login")
    private AuthResponse login(HttpSession session, @RequestBody LoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword(), session.getId());
    }

    @GetMapping("logout")
    private AuthResponse login(HttpSession session) {
        return authService.logout(session.getId());
    }
}
