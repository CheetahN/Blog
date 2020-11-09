package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginRequest {
    @JsonProperty("e_mail")
    String email;
    @JsonProperty("password")
    String password;
}
