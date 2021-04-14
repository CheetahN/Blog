package main.service.impl;

import main.api.request.ProfileMultipartRequest;
import main.api.request.ProfileRequest;
import main.api.response.ResultResponse;
import main.config.SecurityConfig;
import main.model.User;
import main.repository.UserRepository;
import main.service.FileService;
import main.service.UserService;
import main.service.exceptions.BadRequestException;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final FileServiceImpl fileService;
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxImageSize;
    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, FileServiceImpl fileService) {
        this.userRepository = userRepository;
        this.fileService = fileService;
    }

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getPrincipal().equals("anonymousUser"))
            return null;

        Optional<User> user = userRepository.findByEmail(auth.getName());

        if (user.isEmpty()) {
            throw new UsernameNotFoundException(" - User with : " + auth.getName() + " not found");
        }
        return user.get();
    }

    @Override
    public ResultResponse changeMyProfile(ProfileMultipartRequest profileRequest) {
        String LATIN = "^\\w{3,}$";
        String CYRILLIC = "^[а-яА-Я0-9_]{3,}$";
        User user = getCurrentUser();
        Map<String, String> errors = new HashMap<>();

        if (!(profileRequest.getName().matches(LATIN) || profileRequest.getName().matches(CYRILLIC))) {
            errors.put("name", "Имя указано неверно");
        }
        if (userRepository.findByEmail(profileRequest.getEmail()).isPresent()
                && !userRepository.findByEmail(profileRequest.getEmail()).get().getId().equals(user.getId())) {
            errors.put("email", "Этот e-mail уже зарегистрирован");
        }
        if (profileRequest.getPassword() != null && profileRequest.getPassword().length() < 6) {
            errors.put("password", "Пароль короче 6-ти символов");
        }

        if (profileRequest.getPhoto() != null) {
            if (!(profileRequest.getPhoto().getOriginalFilename().endsWith(".jpg") || (profileRequest.getPhoto().getOriginalFilename().endsWith(".png")))) {
                errors.put("image", "Неверный формат файла");
            }
            if (profileRequest.getPhoto().getSize() > DataSize.parse(maxImageSize).toBytes()) {
                errors.put("photo", "Фото должно быть не более 1 Мб");
            }
        }

        if (!errors.isEmpty())
            throw new BadRequestException(errors);

        if (profileRequest.getRemovePhoto() != null && profileRequest.getRemovePhoto() == 1) {
            fileService.removeImage(user.getPhoto());
            user.setPhoto(null);
        } else if (profileRequest.getPhoto() != null) {
            user.setPhoto(fileService.uploadFile(profileRequest.getPhoto()));
            try {
                fileService.cropAndResizeAvatar(user.getPhoto());
            } catch (IOException e) {
                e.printStackTrace();
                errors.put("avatar", "Ошибка изменения файла");
            }
        }

        if (profileRequest.getName() != null )
            user.setName(profileRequest.getName());
        if (profileRequest.getEmail() != null )
            user.setEmail(profileRequest.getEmail());
        if (profileRequest.getPassword() != null) {
            user.setPassword(SecurityConfig.passwordEncoder().encode(profileRequest.getPassword()));
        }
        userRepository.save(user);
        return new ResultResponse(true);
    }

    @Override
    public ResultResponse changeMyProfile(ProfileRequest profileRequest) {
        ProfileMultipartRequest multipartRequest = new ProfileMultipartRequest();
        multipartRequest.setEmail(profileRequest.getEmail());
        multipartRequest.setPassword(profileRequest.getPassword());
        multipartRequest.setRemovePhoto(profileRequest.getRemovePhoto());
        multipartRequest.setName(profileRequest.getName());
        return  changeMyProfile(multipartRequest);
    }
}
