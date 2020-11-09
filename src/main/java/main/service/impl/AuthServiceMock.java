package main.service.impl;

import main.api.response.AuthResponse;
import main.service.AuthService;
import org.springframework.stereotype.Service;

@Service

public class AuthServiceMock  implements AuthService {
    @Override
    public AuthResponse check(String httpSession) {
        return new AuthResponse(false);
    }

    @Override
    public AuthResponse login(String email, String password, String httpSession) {
        return new AuthResponse(false);
    }

    @Override
    public AuthResponse logout(String httpSession) {
        return new AuthResponse(true);
    }
}
