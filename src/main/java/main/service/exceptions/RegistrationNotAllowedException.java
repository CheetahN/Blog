package main.service.exceptions;

public class RegistrationNotAllowedException extends IllegalArgumentException {

    public RegistrationNotAllowedException() {
        super("Registration not allowed");
    }
}
