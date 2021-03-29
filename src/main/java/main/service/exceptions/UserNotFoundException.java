package main.service.exceptions;

public class UserNotFoundException extends NullPointerException {
    public UserNotFoundException(int id) {
        super("User not found in database: " + id);
    }
}
