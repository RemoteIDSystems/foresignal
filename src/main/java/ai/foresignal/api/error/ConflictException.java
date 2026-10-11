package ai.foresignal.api.error;

import org.springframework.http.HttpStatus;

/**
 * 409: the request is well formed but the current state will not allow it.
 *
 * <p>The code is supplied by the caller because the useful distinction is the reason, not the
 * status: {@code CREDENTIALS_REQUIRED}, {@code ALREADY_RUNNING} and {@code IN_BACKOFF} all mean
 * different things to a client.
 */
public class ConflictException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    public static final String CREDENTIALS_REQUIRED = "CREDENTIALS_REQUIRED";
    public static final String ALREADY_RUNNING = "ALREADY_RUNNING";
    public static final String IN_BACKOFF = "IN_BACKOFF";

    public ConflictException(String code, String detail) {
        super(HttpStatus.CONFLICT, code, detail);
    }
}
