package ai.foresignal.api.error;

import org.springframework.http.HttpStatus;

/**
 * 400: a value supplied by a caller could not be parsed or is out of range.
 *
 * <p>Exists to separate "the caller sent nonsense" from "Foresignal has a bug". Input parsers used to
 * throw {@link IllegalArgumentException}, which the handler mapped to 400 — so an
 * {@code IllegalArgumentException} thrown by a library deep inside a service was also reported to
 * the caller as a validation failure, with no stack trace logged. Parsers now throw this, and a
 * bare {@code IllegalArgumentException} is treated as the internal error it probably is.
 *
 * <p>Thrown by {@link ai.foresignal.settings.Durations}, {@link ai.foresignal.source.ParamValidator} and the
 * other parsers that read caller input.
 */
public class InvalidInputException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    public InvalidInputException(String detail) {
        super(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, detail);
    }

    public InvalidInputException(String detail, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, detail, cause);
    }
}
