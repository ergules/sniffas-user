package app.vaazar.config.exception;

public class AuthorisationException extends Exception {

    public AuthorisationException(String message) {
        super(message);
    }

    public AuthorisationException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorisationException(Throwable cause) {
        super(cause);
    }
}
