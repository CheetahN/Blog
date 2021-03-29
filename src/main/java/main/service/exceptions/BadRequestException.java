package main.service.exceptions;

import lombok.Data;

import java.util.Map;

@Data
public class BadRequestException extends IllegalArgumentException {
    Map<String, String> errors;
    public BadRequestException(Map<String, String> errors) {
        super();
        this.errors = errors;
    }
}
