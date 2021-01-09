package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private boolean result;
    private UserResponse user;

    public AuthResponse(boolean result) {
        this.result = result;
    }

    public AuthResponse(boolean result, UserResponse user) {
        this.result = result;
        this.user = user;
    }
}
