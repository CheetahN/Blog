package main.repository;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {
    @Transactional
    public void deleteByTimeBefore(LocalDateTime time);

    public Optional<CaptchaCode> findBySecretCode(String secret);
    @Transactional
    public void deleteBySecretCode(String secret);
}
