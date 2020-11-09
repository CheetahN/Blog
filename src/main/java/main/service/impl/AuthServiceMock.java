package main.service.impl;

import main.service.AuthService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Primary
public class AuthServiceMock  implements AuthService {
    @Override
    public Map<String, Object> check(String httpSession) {
        Map<String, Object> response = new HashMap<>();
        boolean isAuthenticated = false;
        response.put("result", isAuthenticated);
        return response;
    }

    @Override
    public Map<String, Object> login(String email, String password, String httpSession) {
        return null;
    }
}
