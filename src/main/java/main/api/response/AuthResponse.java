package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private Boolean result;
    private UserResponse user;

    public AuthResponse(Boolean result) {
        this.result = result;
    }

    public AuthResponse(Boolean result, UserResponse user) {
        this.result = result;
        this.user = user;
    }
}
