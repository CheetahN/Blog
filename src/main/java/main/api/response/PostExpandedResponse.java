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
    private UserDTO user;
    private String title;
    private String text;
    private long likeCount;
    private long dislikeCount;
    private long viewCount;
    private List<CommentDTO> comments;
    private List<String> tags;


    @Data
    @AllArgsConstructor
    private static class UserDTO {
        private int id;
        private String name;
    }

    public static class PostExpandedResponseBuilder {
        public PostExpandedResponseBuilder user(int id, String name) {
            this.user = new UserDTO(id, name);
            return this;
        }
    }


}

/*
{
 "id": 34,
 "timestamp": 1592338706,
 "active": true,
 "user":
   {
   	"id": 88,
   	"name": "Дмитрий Петров"
   },
 "title": "Заголовок поста",
 "text": "Текст поста в формате HTML",
 "likeCount": 36,
 "dislikeCount": 3,
 "viewCount": 55,
 "comments": [
   {
     "id": 776,
     "timestamp": 1592338706,
     "text": "Текст комментария в формате HTML",
     "user":
       {
         "id": 88,
         "name": "Дмитрий Петров",
         "photo": "/avatars/ab/cd/ef/52461.jpg"
       }
   },
   {...}
 ],
 "tags": ["Статьи", "Java"]
}
 */