package main.api.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class PostResponse {
    private int id;
    private long timestamp;
    private User user;
    @AllArgsConstructor
    @Data
    private class User {
        private int id;
        private String name;
    }
    private String title;
    private String announce;
    private long likeCount;
    private long dislikeCount;
    private int commentCount;
    private long viewCount;

    public void addUser(int id, String name) {
        user = new User(id, name);
    }


}
