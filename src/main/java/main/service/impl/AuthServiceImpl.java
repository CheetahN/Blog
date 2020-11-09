package main.service.impl;

import main.api.response.UserResponse;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.AuthService;
import main.service.exceptions.NoUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
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
    public Map<String, Object> check(String httpSession) {
        Map<String, Object> response = new HashMap<>();

        int id = activeSessions.getOrDefault(httpSession, -1);
        if ( id < 0 ) {
            response.put("result", false);
        } else {
            User user = userRepository.findById(id).orElseThrow(() -> new NoUserException(id));
            response.put("result", true);
            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .moderation(user.getIsModerator() == 1)
                    .name(user.getName())
                    .moderationCount(user.getIsModerator() == 1 ? 0 : postRepository.countByModerationStatus(ModerationStatus.NEW))
                    .photo(user.getPhoto())
                    .settings(user.getIsModerator() == 1)
                    .build();
            response.put("user", userResponse);
            activeSessions.put(httpSession, user.getId());
        }
        return response;
    }

    @Override
    public Map<String, Object> login(String email, String password, String httpSession) {
        Map<String, Object> response = new HashMap<>();
        User user = userRepository.findByEmail(email);
        if (user == null || !password.equals(user.getPassword())) {
            response.put("result", false);
        } else {
            response.put("result", true);
            UserResponse userResponse = UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .moderation(user.getIsModerator() == 1)
                    .name(user.getName())
                    .moderationCount(user.getIsModerator() == 1 ? 0 : postRepository.countByModerationStatus(ModerationStatus.NEW))
                    .photo(user.getPhoto())
                    .settings(user.getIsModerator() == 1)
                    .build();
            response.put("user", userResponse);
            activeSessions.put(httpSession, user.getId());
        }


        return response;
    }

}
