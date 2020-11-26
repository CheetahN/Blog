package main.service.exceptions;

public class PostNotFoundException extends NullPointerException {
    public PostNotFoundException(int id) {
        super("Post not found in database: " + id);
    }
}
