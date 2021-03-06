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

    @ExceptionHandler(UserNotFoundException.class)
    protected ResponseEntity<GlobalException> handleUserNotFoundException(UserNotFoundException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    protected ResponseEntity<ResultResponse> handleTagNotFoundException() {
        Map<String, String> errors = new HashMap<>();
        errors.put("image", "Размер файла превышает допустимый размер");
        return new ResponseEntity<>(new ResultResponse(false, errors), HttpStatus.OK);
    }

    @ExceptionHandler(TagNotFoundException.class)
    protected ResponseEntity<GlobalException> handleTagNotFoundException(TagNotFoundException ex) {
        return new ResponseEntity<>(new GlobalException(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @Data
    @AllArgsConstructor
    private static class GlobalException {
        private String message;
    }
}
