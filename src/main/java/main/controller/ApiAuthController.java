package main.controller;

import main.api.request.LoginRequest;
import main.api.request.RegistrationRequest;
import main.api.response.AuthResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import main.api.response.UserResponse;
import main.repository.UserRepository;
import main.service.AuthService;
import main.service.exceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class ApiAuthController {
    private AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;


    public ApiAuthController(AuthService authService, AuthenticationManager authenticationManager, UserRepository userRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @GetMapping("check")
    public ResponseEntity<AuthResponse> check(Principal principal) {
        if (principal == null)
            return ResponseEntity.ok(new AuthResponse(false));
        else
            return ResponseEntity.ok(
                    new AuthResponse(
                            true,
                            authService.check(principal.getName())));
    }

    @PostMapping("login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @GetMapping("logout")
    public AuthResponse logout(HttpSession session) {
        return authService.logout(session.getId());
    }

    @GetMapping("captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        return new ResponseEntity<>(authService.getCaptcha(), HttpStatus.OK);
    }

    @PostMapping("register")
    public ResponseEntity<RegistrationResponse> register(@RequestBody RegistrationRequest request) {
        RegistrationResponse response = authService.register(request);
        if (response == null)       // registration not allowed
            return ResponseEntity.notFound().build();
        else
            return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
