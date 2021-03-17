package main.service;

import main.api.request.EmailRequest;
import main.api.request.PasswordRequest;
import main.api.request.RegistrationRequest;
import main.api.response.*;
import main.model.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public interface AuthService {

    public UserResponse check(String httpSession);

    public AuthResultResponse logout(HttpServletRequest request);

    public AuthResultResponse login(String email, String password);

    public CaptchaResponse getCaptcha();

    public ResultResponse register(RegistrationRequest request);

    public UserResponse convertUserToUserResponse(User user);

    public ResultResponse changePwd(PasswordRequest request);

    public ResultResponse sendRestorationEmail(EmailRequest request);
}
