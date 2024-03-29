package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


import java.util.List;

@Data
@Builder
public class PostExpandedResponse {
    private int id;
    private long timestamp;
    private boolean active;
    private PostAuthor user;
    private String title;
    private String text;
    private long likeCount;
    private long dislikeCount;
    private long viewCount;
    private List<CommentResponse> comments;
    private List<String> tags;


    @Data
    @AllArgsConstructor
    private static class PostAuthor {
        private int id;
        private String name;
    }

    public static class PostExpandedResponseBuilder {
        public PostExpandedResponseBuilder user(int id, String name) {
            this.user = new PostAuthor(id, name);
            return this;
        }
    }


}
