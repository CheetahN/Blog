package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PostListReponse {
    private long count;
    private List<PostResponse> posts;
}
