package app.vaazar.config;

import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

public class ErrorDetails {
    private Instant timestamp;
    private String error;
    private String message;
    private String details;

    private ErrorDetails() {
        timestamp = Instant.now();
    }

    public ErrorDetails(Exception e, WebRequest req) {
        this();
        message = e.getMessage();
        error = e.getClass().getSimpleName();
        details = req.getDescription(false);
    }
}
