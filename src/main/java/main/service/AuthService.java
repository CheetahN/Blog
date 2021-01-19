package main.service;

import main.api.request.RegistrationRequest;
import main.api.response.AuthResultResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import main.api.response.UserResponse;
import main.model.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface AuthService {

    public UserResponse check(String httpSession);

    public AuthResultResponse logout(HttpServletRequest request);

    public AuthResultResponse login(String email, String password);

    public CaptchaResponse getCaptcha();

    public RegistrationResponse register(RegistrationRequest request);

    public UserResponse convertUserToUserResponse(User user);
}
