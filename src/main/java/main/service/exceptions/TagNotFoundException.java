package main.service.exceptions;

public class TagNotFoundException extends NullPointerException {
    public TagNotFoundException(String tag) {
        super("tag does not exist: " + tag);
    }
}
