package ai.foresignal.notify.action;

import ai.foresignal.api.error.ForesignalException;
import org.springframework.http.HttpStatus;

/** {@code 410 ACTION_EXPIRED}: the capability is unknown, expired or already used (prompt 033). */
public class ActionExpiredException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    /** The problem code. */
    public static final String CODE = ai.foresignal.api.error.ErrorCodes.ACTION_EXPIRED;

    /** Creates the exception. */
    public ActionExpiredException() {
        super(HttpStatus.GONE, CODE, "This action link is unknown, expired or was already used.");
    }
}
