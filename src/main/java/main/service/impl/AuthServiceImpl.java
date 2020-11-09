package main.service.impl;

import main.api.response.AuthResponse;
import main.api.response.UserResponse;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.AuthService;
import main.service.exceptions.NoUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private static Map<String, Integer> activeSessions = new HashMap<>();
    private UserRepository userRepository;
    private PostRepository postRepository;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PostRepository postRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
    }

    @Override
    public AuthResponse check(String httpSession) {
        AuthResponse authResponse;

        int id = activeSessions.getOrDefault(httpSession, -1);
        if ( id < 0 ) {
            authResponse = new AuthResponse(false);
        } else {
            User user = userRepository.findById(id).orElseThrow(() -> new NoUserException(id));
            authResponse = new AuthResponse(true, convertUserToUserResponse(user));
        }
        return authResponse;
    }

    @Override
    public AuthResponse logout(String httpSession) {
        activeSessions.remove(httpSession);
        return new AuthResponse(true);
    }

    @Override
    public AuthResponse login(String email, String password, String sessionId) {
        AuthResponse authResponse;
        User user = userRepository.findByEmail(email);
        if (user == null || !password.equals(user.getPassword())) {
            authResponse = new AuthResponse(false);
        } else {
            activeSessions.put(sessionId, user.getId());
            authResponse = new AuthResponse(true, convertUserToUserResponse(user));
        }

        return authResponse;
    }

    private UserResponse convertUserToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .moderation(user.getIsModerator() == 1)
                .name(user.getName())
                .moderationCount(user.getIsModerator() == 0 ? 0 : postRepository.countByModerationStatus(ModerationStatus.NEW))
                .photo(user.getPhoto())
                .settings(user.getIsModerator() == 1)
                .build();
    }

}
