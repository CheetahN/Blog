package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ModerationRequest {
    @JsonProperty("post_id")
    private int postId;
    @JsonProperty("decision")
    private String decision;
}
