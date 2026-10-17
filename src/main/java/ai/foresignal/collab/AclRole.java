package ai.foresignal.collab;

import ai.foresignal.api.error.ValidationException;
import java.util.Locale;

/**
 * An access-list role (prompt 130), weakest first: a viewer reads, a commenter also comments, an editor also edits
 * and shares, an owner also deletes and transfers ownership.
 */
public enum AclRole {

    /** Reads the object. */
    VIEWER,

    /** Reads and comments. */
    COMMENTER,

    /** Reads, comments, edits and shares. */
    EDITOR,

    /** Everything, including delete and ownership transfer. */
    OWNER;

    /** The wire value. */
    public String wireValue() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** True when this role is at least {@code other}. */
    public boolean atLeast(AclRole other) {
        return other == null || ordinal() >= other.ordinal();
    }

    /** The stronger of two roles (null counts as no role). */
    public static AclRole stronger(AclRole a, AclRole b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    /** The role of a wire value, or null for null. */
    public static AclRole fromWire(String value) {
        return value == null ? null : valueOf(value.strip().toUpperCase(Locale.ROOT));
    }

    /**
     * Parses a request value.
     *
     * @throws ValidationException when it is not viewer, commenter, editor or owner
     */
    public static AclRole parse(String field, String value, AclRole fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return fromWire(value);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField(field, "must be viewer, commenter, editor or owner");
        }
    }
}
