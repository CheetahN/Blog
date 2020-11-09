package main.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService {

    public Map<String, Object> check(String httpSession);

    public Map<String, Object> login(String email, String password, String httpSession);
}
