package main.service;

import main.api.request.RegistrationRequest;
import main.api.response.AuthResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import main.api.response.UserResponse;
import main.model.User;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {

    public UserResponse check(String httpSession);

    public AuthResponse logout(String httpSession);

    public AuthResponse login(String email, String password);

    public CaptchaResponse getCaptcha();

    public RegistrationResponse register(RegistrationRequest request);

    public UserResponse convertUserToUserResponse(User user);
}
