package ai.foresignal.security;

import ai.foresignal.api.error.ValidationException;
import java.util.Collection;
import java.util.Locale;

/**
 * A Traffic Light Protocol 2.0 label (prompt 130, OpenAPI {@code Tlp}): how far a work product may travel.
 * Labels are ordered from the least to the most restrictive; {@link #strictest} picks the label a derived product
 * inherits from its inputs (COL-2). The rules per share target are {@link EgressPolicy#tlp(Tlp, TlpTarget)}.
 */
public enum Tlp {

    /** No restriction: public share links are allowed. */
    CLEAR("CLEAR"),

    /** The instance and federated peers; no public links. */
    GREEN("GREEN"),

    /** The owner's organization and the organizations of named recipients on other instances. */
    AMBER("AMBER"),

    /** The owner's organization and named recipients only. */
    AMBER_STRICT("AMBER+STRICT"),

    /** Only principals named in the access list. */
    RED("RED");

    private final String wire;

    Tlp(String wire) {
        this.wire = wire;
    }

    /** The wire value ({@code AMBER+STRICT} for {@link #AMBER_STRICT}). */
    public String wireValue() {
        return wire;
    }

    /** True when this label is more restrictive than {@code other}. */
    public boolean stricterThan(Tlp other) {
        return other == null || ordinal() > other.ordinal();
    }

    /** The more restrictive of two labels (null counts as no label). */
    public static Tlp stricter(Tlp a, Tlp b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.ordinal() >= b.ordinal() ? a : b;
    }

    /** The most restrictive label among {@code labels}, or {@code fallback} when there is none. */
    public static Tlp strictest(Collection<Tlp> labels, Tlp fallback) {
        Tlp out = null;
        if (labels != null) {
            for (Tlp t : labels) {
                out = stricter(out, t);
            }
        }
        return out == null ? fallback : out;
    }

    /** The label of a wire value (case-insensitive; {@code AMBER_STRICT} is accepted too), or null for null. */
    public static Tlp fromWire(String value) {
        if (value == null) {
            return null;
        }
        String v = value.strip().toUpperCase(Locale.ROOT);
        for (Tlp t : values()) {
            if (t.wire.equals(v) || t.name().equals(v)) {
                return t;
            }
        }
        throw new IllegalArgumentException("unknown TLP label " + value);
    }

    /**
     * Parses a request value.
     *
     * @param field the request field, for the problem
     * @param value the value, or null
     * @param fallback returned for null
     * @throws ValidationException when the value is not a TLP 2.0 label
     */
    public static Tlp parse(String field, String value, Tlp fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return fromWire(value);
        } catch (IllegalArgumentException e) {
            throw ValidationException.forField(field, "must be CLEAR, GREEN, AMBER, AMBER+STRICT or RED");
        }
    }
}
