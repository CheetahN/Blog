package main.service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import main.api.response.ResultResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice()
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(PostNotFoundException.class)
    protected ResponseEntity<GlobalException> handlePostNotFoundException(PostNotFoundException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<ResultResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(new ResultResponse(false, ex.getErrors()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<GlobalException> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    protected ResponseEntity<ResultResponse> handleImageSizeLimitException() {
        Map<String, String> errors = new HashMap<>();
        errors.put("image", "Размер файла превышает допустимый размер");
        return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TagNotFoundException.class)
    protected ResponseEntity<GlobalException> handleTagNotFoundException(TagNotFoundException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(RegistrationNotAllowedException.class)
    protected ResponseEntity<GlobalException> handleRegistrationNotAllowedException(RegistrationNotAllowedException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    protected ResponseEntity<GlobalException> handleUnauthorizedException(UnauthorizedException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @Data
    @AllArgsConstructor
    private static class GlobalException {
        private String message;
    }

}
