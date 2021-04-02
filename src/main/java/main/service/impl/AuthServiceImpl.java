package main.service.impl;

import com.github.cage.Cage;
import com.github.cage.image.Painter;
import com.github.cage.token.RandomTokenGenerator;
import main.api.request.EmailRequest;
import main.api.request.PasswordRequest;
import main.api.request.RegistrationRequest;
import main.api.response.AuthResultResponse;
import main.api.response.CaptchaResponse;
import main.api.response.ResultResponse;
import main.api.response.UserResponse;
import main.config.SecurityConfig;
import main.model.CaptchaCode;
import main.model.User;
import main.model.enums.GlobalSettingCode;
import main.model.enums.GlobalSettingValue;
import main.model.enums.ModerationStatus;
import main.repository.CaptchaRepository;
import main.repository.PostRepository;
import main.repository.SettingsRepository;
import main.repository.UserRepository;
import main.service.AuthService;
import main.service.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Primary
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CaptchaRepository captchaRepository;
    private final SettingsRepository settingsRepository;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;
    @Value("${blog.captcha.lifetime}")
    private long captchaLifetime;
    @Value("${blog.captcha.captcha.height}")
    private int captchaHeight;
    @Value("${blog.captcha.captcha.width}")
    private int captchaWidth;
    @Value("${blog.captcha.captcha.length}")
    private int captchaLength;
    @Value("${blog.title}")
    private String blogName;
    @Value("${blog.email}")
    private String blogEmail;
    @Value("${application.host}")
    private String appHost;


    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PostRepository postRepository, CaptchaRepository captchaRepository, SettingsServiceImpl settingsService, SettingsRepository settingsRepository, AuthenticationManager authenticationManager, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.captchaRepository = captchaRepository;
        this.settingsRepository = settingsRepository;
        this.authenticationManager = authenticationManager;
        this.mailSender = mailSender;
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
        AuthResultResponse authResultResponse = new AuthResultResponse();
        authResultResponse.setResult(false);
        org.springframework.security.core.userdetails.User user;
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            user = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
        } catch (Exception e) {
            return authResultResponse;
        }

        if (userRepository.findByEmail(user.getUsername()).isEmpty()) {
            return authResultResponse;
        } else {
            User currentUser = userRepository.findByEmail(user.getUsername()).get();
            authResultResponse.setResult(true);
            authResultResponse.setUser(convertUserToUserResponse(currentUser));
        }
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

        Painter painter = new Painter(captchaWidth, captchaHeight, null, Painter.Quality.DEFAULT, null, null);
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
    public ResultResponse register(RegistrationRequest request) {
        if (settingsRepository.findByCode(GlobalSettingCode.MULTIUSER_MODE).getValue() == GlobalSettingValue.NO)
            return null;
        String LATIN = "^\\w{3,}$";
        String CYRILLIC = "^[а-яА-Я0-9_]{3,}$";
        Map<String, String> errors = new HashMap<>();

        if (!(request.getName().matches(LATIN) || request.getName().matches(CYRILLIC))) {
            errors.put("name", "Имя указано неверно");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }

        CaptchaCode captchaCode = captchaRepository.findBySecretCode(request.getCaptchaSecret());
        if (captchaCode == null || !captchaCode.getCode().equals(request.getCaptcha())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }

        if (errors.isEmpty()) {
            User user = new User();
            user.setName(request.getName());
            user.setEmail(request.getEmail());
            user.setPassword(SecurityConfig.passwordEncoder().encode(request.getPassword()));
            user.setRegTime(LocalDateTime.now());
            user.setIsModerator((byte) 0);
            userRepository.save(user);
        } else {
            return new ResultResponse(false, errors);
        }
        return new ResultResponse(true);
    }

    @Override
    public ResultResponse changePwd(PasswordRequest request) {
        Map<String, String> errors = new HashMap<>();

        if (request.getPassword().length() <= 6) {
            errors.put("password", "Пароль короче 6-ти символов");
        }

        CaptchaCode captchaCode = captchaRepository.findBySecretCode(request.getCaptchaSecret());
        if (captchaCode == null || !captchaCode.getCode().equals(request.getCaptcha())) {
            errors.put("captcha", "Код с картинки введён неверно");
        }

        Optional<User> optionalUser = userRepository.findByCode(request.getCode());
        if (optionalUser.isEmpty()) {
            errors.put("code", "Ссылка для восстановления пароля устарела. <a href=\"/auth/restore\">Запросить ссылку снова</a>");
        }

        if (errors.isEmpty()) {
            User user = optionalUser.get();
            user.setCode(null);
            user.setPassword(SecurityConfig.passwordEncoder().encode(request.getPassword()));
            userRepository.save(user);
        } else {
            return new ResultResponse(false,errors);
        }
        return new ResultResponse(true);
    }

    @Override
    public ResultResponse sendRestorationEmail(EmailRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isEmpty()) {
            return new ResultResponse(false);
        }
        MimeMessage message = mailSender.createMimeMessage();
        User user = userRepository.findByEmail(request.getEmail()).get();
        user.setCode(UtilService.getRandomString(16));
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            message.setContent(createHtmlMessage(user.getName(), user.getCode()), "text/html; charset=utf-8");
            helper.setFrom(blogEmail);
            helper.setTo(request.getEmail());
            helper.setSubject("Reset password " + blogName);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        userRepository.save(user);
        mailSender.send(message);
        return new ResultResponse(true);
    }

    private String createHtmlMessage(String userName, String code) {
        return "<h3>Здравствуйте, " + userName + "!</h3>" +
                "<p><br>&nbsp;&nbsp;&nbsp;&nbsp;От Вашего имени подана заявка на смену пароля в "
                + blogName +". Для сброса пароля пройдите по следующей ссылке:" +
                "<br>&nbsp;&nbsp;&nbsp;&nbsp;" +
                "<a href=" + appHost + "/login/change-password/" + code +
                ">CLICK TO RESET PASSWORD</a>";
    }
}
