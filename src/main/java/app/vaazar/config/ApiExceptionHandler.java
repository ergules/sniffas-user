package app.vaazar.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<app.vaazar.config.ErrorDetails> handleAccessDeniedException(AccessDeniedException e, WebRequest req) {
        return new ResponseEntity<>(new app.vaazar.config.ErrorDetails(e, req), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<app.vaazar.config.ErrorDetails> handleIllegalStateException(Exception e, WebRequest req) {
        return new ResponseEntity<>(new app.vaazar.config.ErrorDetails(e, req), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<app.vaazar.config.ErrorDetails> handleUnknownExceptions(Exception e, WebRequest req) {
        return new ResponseEntity<>(new app.vaazar.config.ErrorDetails(e, req), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
