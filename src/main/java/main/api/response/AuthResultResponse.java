package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResultResponse {
    private boolean result;
    private UserResponse user;

    public AuthResultResponse(boolean result) {
        this.result = result;
    }

    public AuthResultResponse(boolean result, UserResponse user) {
        this.result = result;
        this.user = user;
    }
}
