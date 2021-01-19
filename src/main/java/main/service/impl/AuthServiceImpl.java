package main.service.impl;

import com.github.cage.Cage;
import com.github.cage.image.Painter;
import com.github.cage.token.RandomTokenGenerator;
import main.api.request.RegistrationRequest;
import main.api.response.AuthResultResponse;
import main.api.response.CaptchaResponse;
import main.api.response.RegistrationResponse;
import main.api.response.UserResponse;
import main.model.CaptchaCode;
import main.model.User;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.model.enums.ModerationStatus;
import main.repository.*;
import main.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CaptchaRepository captchaRepository;
    private final SessionRepository sessionRepository;
    private final SettingsRepository settingsRepository;
    private final AuthenticationManager authenticationManager;
    @Value("${blog.captcha.lifetime}")
    private long captchaLifetime;
    @Value("${blog.captcha.captcha.height}")
    private int captchaHeight;
    @Value("${blog.captcha.captcha.width}")
    private int captchaWidth;
    @Value("${blog.captcha.captcha.length}")
    private int captchaLength;


    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PostRepository postRepository, CaptchaRepository captchaRepository, SessionRepository sessionRepository, SettingsServiceImpl settingsService, SettingsRepository settingsRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.captchaRepository = captchaRepository;
        this.sessionRepository = sessionRepository;
        this.settingsRepository = settingsRepository;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserResponse check(String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
        return convertUserToUserResponse(currentUser);
    }

    @Override
    public AuthResultResponse logout(HttpServletRequest request) {
        new SecurityContextLogoutHandler().logout(request, null, null);
        return new AuthResultResponse(true);
    }

    @Override
    public AuthResultResponse login(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        org.springframework.security.core.userdetails.User user =  (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        User currentUser = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new UsernameNotFoundException(user.getUsername()));
        AuthResultResponse authResultResponse = new AuthResultResponse();
        authResultResponse.setResult(true);
        authResultResponse.setUser(convertUserToUserResponse(currentUser));
        return authResultResponse;
    }


    public UserResponse convertUserToUserResponse(User user) {
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

        Painter painter = new Painter(captchaWidth, captchaHeight, null,  Painter.Quality.DEFAULT, null, null);
        Cage cage = new Cage(painter, null, null, null,
                Cage.DEFAULT_COMPRESS_RATIO, new RandomTokenGenerator(null, captchaLength), null);
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

    @Override
    public RegistrationResponse register(RegistrationRequest request) {
        if (settingsRepository.findByCode(GlobalSettingCode.MULTIUSER_MODE).getValue() == GlobalSettingValue.NO)
            return null;
        String LATIN = "^\\w*$";
        String CYRILLIC = "^[а-яА-Я0-9_]{3,}$";
        boolean result = true;
        Map<String, String> errors = new HashMap<>();
        RegistrationResponse response = new RegistrationResponse();

        if (!(request.getName().matches(LATIN) || request.getName().matches(CYRILLIC))) {
            result = false;
            errors.put("name", "Имя указано неверно");
        }

        if (userRepository.findByEmail(request.getEmail()) != null) {
            result = false;
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }

        CaptchaCode captchaCode = captchaRepository.findBySecretCode(request.getCaptchaSecret());
        if (captchaCode == null || !captchaCode.getCode().equals(request.getCaptcha())) {
            result = false;
            errors.put("captcha", "Код с картинки введён неверно");
        }

        response.setResult(result);

        if (result) {
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(request.getPassword());
            user.setRegTime(LocalDateTime.now());
            user.setIsModerator((byte) 0);
            userRepository.save(user);
        } else {
            response.setErrors(errors);
        }
        return response;
    }
}
