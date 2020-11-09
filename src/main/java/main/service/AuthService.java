package main.service;

import main.api.response.AuthResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService {

    public AuthResponse check(String httpSession);

    public AuthResponse logout(String httpSession);

    public AuthResponse login(String email, String password, String httpSession);
}
