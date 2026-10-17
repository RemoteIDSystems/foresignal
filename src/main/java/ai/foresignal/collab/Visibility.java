package ai.foresignal.collab;

import ai.foresignal.api.error.ValidationException;
import java.util.Locale;

/**
 * Who sees a work product beyond its owner (prompt 130, OpenAPI {@code Visibility}): {@link #PRIVATE} the owner (and
 * admins) only, {@link #SHARED} the owner and the access list, {@link #INSTANCE} also every principal of the instance
 * as a viewer.
 */
public enum Visibility {

    /** The owner and admins only; the access list is kept but not applied. */
    PRIVATE,

    /** The owner and the access list. */
    SHARED,

    /** The access list, plus every principal of the instance as a viewer. */
    INSTANCE;

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** The visibility of a wire value. */
    public static Visibility fromWire(String value) {
        return valueOf(value.strip().toUpperCase(Locale.ROOT));
    }

    /**
     * Parses a request value.
     *
     * @throws ValidationException when it is not private, shared or instance
     */
    public static Visibility parse(String field, String value, Visibility fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return fromWire(value);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField(field, "must be private, shared or instance");
        }
    }
}
