package main.service;

import main.api.request.RegistrationRequest;
import main.api.response.AuthResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    public AuthResponse check(String httpSession);

    public AuthResponse logout(String httpSession);

    public AuthResponse login(String email, String password, String httpSession);

    public CaptchaResponse getCaptcha();

    public RegistrationResponse register(RegistrationRequest request);
}
