package main.service.impl;

import com.github.cage.Cage;
import com.github.cage.image.Painter;
import main.api.response.AuthResponse;
import main.api.response.CaptchaResponse;
import main.api.response.UserResponse;
import main.model.CaptchaCode;
import main.model.User;
import main.model.enums.ModerationStatus;
import main.repository.CaptchaRepository;
import main.repository.PostRepository;
import main.repository.UserRepository;
import main.service.AuthService;
import main.service.exceptions.NoUserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private static Map<String, Integer> activeSessions = new HashMap<>();
    private UserRepository userRepository;
    private PostRepository postRepository;
    private CaptchaRepository captchaRepository;
    @Value("${blog.captcha.lifetime}")
    private long captchaLifetime;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PostRepository postRepository, CaptchaRepository captchaRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.captchaRepository = captchaRepository;
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

    @Override
    public CaptchaResponse getCaptcha() {
        captchaRepository.deleteByTimeBefore(LocalDateTime.now().minusMinutes(captchaLifetime));

        Painter painter = new Painter(100, 35, null,  Painter.Quality.DEFAULT, null, null);
        Cage cage = new Cage(painter, null, null, null,
                Cage.DEFAULT_COMPRESS_RATIO, null, null);
        String code = cage.getTokenGenerator().next();
        String captcha = Base64.getEncoder().encodeToString(cage.draw(code));
        String secret = captcha.substring(captcha.length() - 30, captcha.length() - 15);

        CaptchaCode captchaCode = new CaptchaCode();
        captchaCode.setTime(LocalDateTime.now());
        captchaCode.setCode(code);
        captchaCode.setSecretCode(secret);
        captchaRepository.save(captchaCode);


        return new CaptchaResponse(secret, "data:image/png;base64, " + captcha);
    }
}
