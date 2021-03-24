package main.service.impl;

import main.api.request.ProfileRequest;
import main.api.response.ResultResponse;
import main.model.User;
import main.repository.UserRepository;
import main.service.FileService;
import main.service.UserService;
import main.service.exceptions.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final FileService fileService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, FileService fileService) {
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
    public ResultResponse changeMyProfile(ProfileRequest profileRequest) {
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

        if (!errors.isEmpty())
            throw new BadRequestException(errors);

        if (profileRequest.getRemovePhoto() == 1) {
            user.setPhoto("");
            fileService.removeAvatar();
        } else if (profileRequest.getPhoto() != null) {
            user.setPhoto(fileService.uploadAvatar(profileRequest.getPhoto()));
        }

        if (profileRequest.getName() != null )
            user.setName(profileRequest.getName());
        if (profileRequest.getEmail() != null )
            user.setEmail(profileRequest.getEmail());
        if (profileRequest.getPassword() != null) {
            user.setPassword(profileRequest.getPassword());
        }
        userRepository.save(user);
        return new ResultResponse(true);
    }
}
