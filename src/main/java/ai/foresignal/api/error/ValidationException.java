package ai.foresignal.api.error;

import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * 400: the request was understood but is not acceptable.
 *
 * <p>Used for validation the framework cannot express — an adapter parameter that must match its
 * descriptor, a geofence that is not a valid polygon — so it reports field errors in the same shape
 * as Bean Validation failures.
 */
public class ValidationException extends ForesignalException {

    private static final long serialVersionUID = 1L;

    private final List<FieldError> errors;

    public ValidationException(String detail) {
        this(detail, List.of());
    }

    public ValidationException(String detail, List<FieldError> errors) {
        this(ErrorCodes.VALIDATION_FAILED, detail, errors);
    }

    /**
     * A 400 with a more specific code than {@code VALIDATION_FAILED}, for example
     * {@code INGEST_POLICY_INVALID}, still carrying every field error.
     *
     * @param code the problem's {@code code}
     * @param detail the human-readable summary
     * @param errors every field-level problem
     */
    public ValidationException(String code, String detail, List<FieldError> errors) {
        super(HttpStatus.BAD_REQUEST, code, detail);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    /** A single field error. */
    public static ValidationException forField(String field, String message) {
        return new ValidationException(
                "The request is not valid.", List.of(new FieldError(field, message)));
    }

    public List<FieldError> getErrors() {
        return errors;
    }
}
