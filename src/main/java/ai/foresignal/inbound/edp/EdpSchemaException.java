package ai.foresignal.inbound.edp;

import java.util.List;

/**
 * A payload schema that breaks the EDP §7 safety rules (remote references, oversized documents,
 * unsafe patterns) or cannot be compiled.
 */
public class EdpSchemaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient List<EdpSchemaError> errors;

    /** Creates the exception with every problem found. */
    public EdpSchemaException(List<EdpSchemaError> errors) {
        super(summary(errors));
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    /** Every problem, with JSON Pointer paths relative to the schema root. */
    public List<EdpSchemaError> errors() {
        return errors;
    }

    private static String summary(List<EdpSchemaError> errors) {
        if (errors == null || errors.isEmpty()) {
            return "The payload schema is not acceptable";
        }
        EdpSchemaError first = errors.get(0);
        return "The payload schema is not acceptable: " + first.path() + " " + first.message()
                + (errors.size() > 1 ? " (and " + (errors.size() - 1) + " more)" : "");
    }
}
