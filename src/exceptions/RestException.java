package exceptions;

import lombok.Getter;

import java.io.Serial;

@Getter
public class RestException extends RuntimeException {

    private final int statusCode;

    @Serial
    private static final long serialVersionUID = 1L;

    public RestException(String message) {
        super(message);
        this.statusCode = 500;
    }

    public RestException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public RestException(Throwable cause) {
        super(cause);
        this.statusCode = 500;
    }
}
