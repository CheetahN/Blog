package main.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultResponse {
    private boolean result;
    private Map<String, String> errors;

    public ResultResponse(boolean result) {
        this.result = result;
    }
}
