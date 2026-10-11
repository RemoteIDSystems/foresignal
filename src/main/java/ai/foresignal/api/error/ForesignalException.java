package ai.foresignal.api.error;

import org.springframework.http.HttpStatus;

/**
 * Base class for errors that map directly to an API problem response.
 *
 * <p>Carrying the status and code on the exception keeps the decision where the context is — the
 * service that knows why something failed — instead of in a handler that would have to infer it.
 */
public class ForesignalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;
    private final String code;

    public ForesignalException(HttpStatus status, String code, String detail) {
        this(status, code, detail, null);
    }

    public ForesignalException(HttpStatus status, String code, String detail, Throwable cause) {
        super(detail, cause);
        this.status = status;
        this.code = code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    /** The stable machine code placed in the problem's {@code code} property. */
    public String getCode() {
        return code;
    }
}
