package main.service;

import main.model.User;
import main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
}
