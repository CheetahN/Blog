package main.controller;

import main.api.request.LoginRequest;
import main.api.request.RegistrationRequest;
import main.api.response.AuthResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import main.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

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

    @GetMapping("captcha")
    private ResponseEntity<CaptchaResponse> getCaptcha() {
        return new ResponseEntity<>(authService.getCaptcha(), HttpStatus.OK);
    }

    @PostMapping("register")
    private ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        RegistrationResponse response = authService.register(request);
        if (response == null)       // registration not allowed
            return ResponseEntity.notFound().build();
        else
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
