package main.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentDTO {
    private int id;
    private long timestamp;
    private String text;
    private CommentUser user;

    @AllArgsConstructor
    @Data
    private static class CommentUser {
        private int id;
        private String name;
        private String photo;
    }

    public static class CommentDTOBuilder {
        public CommentDTOBuilder user(int id, String name, String photo) {
            this.user = new CommentUser(id, name, photo);
            return this;
        }
    }
}
