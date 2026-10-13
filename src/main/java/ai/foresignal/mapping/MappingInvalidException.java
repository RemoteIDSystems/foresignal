package ai.foresignal.mapping;

import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.FieldError;
import ai.foresignal.api.error.ValidationException;
import java.util.List;

/**
 * {@code 400 MAPPING_INVALID}: a {@code RecordMapping} did not compile (prompt 059). Every problem is listed with
 * its path, such as {@code mapping.fields[3].unit.from}.
 */
public class MappingInvalidException extends ValidationException {

    private static final long serialVersionUID = 1L;

    /** A failure listing {@code errors}. */
    public MappingInvalidException(List<FieldError> errors) {
        super(ErrorCodes.MAPPING_INVALID, "The mapping is not valid.", errors);
    }
}
