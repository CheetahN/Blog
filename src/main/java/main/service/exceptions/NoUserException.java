package main.service.exceptions;

public class NoUserException extends NullPointerException {
    public NoUserException(int id) {
        super("User does not exist in database: " + id);
    }
}
