package main.repository;

import main.model.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {
    @Transactional
    public void deleteByTimeBefore(LocalDateTime time);

    public CaptchaCode findBySecretCode(String secret);
}
