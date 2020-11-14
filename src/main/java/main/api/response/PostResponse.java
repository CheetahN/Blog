package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PostResponse {
    private int id;
    private long timestamp;
    private UserDTO user;
    private String title;
    private String announce;
    private long likeCount;
    private long dislikeCount;
    private int commentCount;
    private long viewCount;

    @Data
    @AllArgsConstructor
    private static class UserDTO {
        private int id;
        private String name;
    }

    public static class PostResponseBuilder {
        public PostResponseBuilder user(int id, String name) {
            this.user = new UserDTO(id, name);
            return this;
        }
    }
}
