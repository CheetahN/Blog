package main.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "captcha_codes")
public class CaptchaCode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(length = 255, columnDefinition = "code", nullable = false)
    private String code;

    @Column(length = 255, columnDefinition = "secret_code", nullable = false)
    private String secretCode;
}
