package app.vaazar.config;

import app.vaazar.config.exception.AuthorisationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.NoSuchElementException;

@ControllerAdvice
public class ApiExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(AuthorisationException.class)
    public ResponseEntity<ErrorDetails> handleAuthorisationException(AuthorisationException e, WebRequest req) {
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>(new ErrorDetails(e, req), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDetails> handleAccessDeniedException(AccessDeniedException e, WebRequest req) {
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>(new ErrorDetails(e, req), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorDetails> handleIllegalStateException(Exception e, WebRequest req) {
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>(new ErrorDetails(e, req), HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<ErrorDetails> handleNoSuchElementException(Exception e, WebRequest req) {
        log.error(e.getLocalizedMessage());
        return new ResponseEntity<>(new ErrorDetails(e, req), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleUnknownExceptions(Exception e, WebRequest req) {
        log.error("unexpected exception", e);
        return new ResponseEntity<>(new ErrorDetails(e, req), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
