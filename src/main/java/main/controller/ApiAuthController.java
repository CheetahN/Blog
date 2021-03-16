package main.controller;

import main.api.request.LoginRequest;
import main.api.request.PasswordRequest;
import main.api.request.RegistrationRequest;
import main.api.response.AuthResultResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import main.api.response.ResultResponse;
import main.repository.UserRepository;
import main.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
    public ResponseEntity<AuthResultResponse> check(Principal principal) {
        if (principal == null)
            return ResponseEntity.ok(new AuthResultResponse(false));
        else
            return ResponseEntity.ok(
                    new AuthResultResponse(
                            true,
                            authService.check(principal.getName())));
    }

    @PostMapping("login")
    public ResponseEntity<AuthResultResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @GetMapping("logout")
    public ResponseEntity<AuthResultResponse> logout(HttpServletRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @GetMapping("captcha")
    public ResponseEntity<CaptchaResponse> getCaptcha() {
        return ResponseEntity.ok(authService.getCaptcha());
    }

    @PostMapping("register")
    public ResponseEntity<ResultResponse> register(@RequestBody RegistrationRequest request) {
        ResultResponse response = authService.register(request);
        if (response == null)       // registration not allowed
            return ResponseEntity.notFound().build();
        else
            return ResponseEntity.ok(response);
    }

    @PostMapping("password")
    public ResponseEntity<ResultResponse> changePassword(@RequestBody PasswordRequest request) {
            return ResponseEntity.ok(authService.changePwd(request));
    }
}
