package app.vaazar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class ApiExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> handleUnknownExceptions(Exception e, WebRequest req) {
        log.error("unexpected exception", e);
        return new ResponseEntity<>(new ErrorDetails(e, req), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
