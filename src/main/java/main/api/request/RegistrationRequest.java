package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RegistrationRequest {
    @JsonProperty("e_mail")
    String email;
    @JsonProperty("password")
    String password;
    @JsonProperty("name")
    String name;
    @JsonProperty("captcha")
    String captcha;
    @JsonProperty("captcha_secret")
    String captchaSecret;

}
