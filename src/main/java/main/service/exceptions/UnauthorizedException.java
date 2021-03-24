package main.service.exceptions;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String email) {
        super("User is not authorised to do this action:" + email);
    }
}
