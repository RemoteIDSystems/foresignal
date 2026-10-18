package ai.foresignal.chronicle.network;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ErrorCodes;
import ai.foresignal.api.error.ForesignalException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.security.Tlp;
import java.util.Locale;

/**
 * A chronicle's discoverability (prompt 134), chosen by its owner and capped by its TLP label: {@code RED} → none;
 * {@code AMBER+STRICT} → none or instance; {@code AMBER}, {@code GREEN} and {@code CLEAR} → any. The default is
 * {@link #NONE}.
 */
public enum DiscoverabilityLevel {

    /** Not discoverable: relationships appear only to people who can see both chronicles. */
    NONE("none"),

    /** Discoverable to principals on this instance, as opaque links. */
    INSTANCE("instance"),

    /** Also discoverable to consenting peers through signed sketches. */
    NETWORK("network");

    private final String wire;

    DiscoverabilityLevel(String wire) {
        this.wire = wire;
    }

    /** The wire value. */
    public String wireValue() {
        return wire;
    }

    /** The highest level a TLP label allows. */
    public static DiscoverabilityLevel maxAllowed(Tlp tlp) {
        if (tlp == null) {
            return NETWORK;
        }
        return switch (tlp) {
            case RED -> NONE;
            case AMBER_STRICT -> INSTANCE;
            default -> NETWORK;
        };
    }

    /** {@code level} lowered to what {@code tlp} allows. */
    public static DiscoverabilityLevel cap(DiscoverabilityLevel level, Tlp tlp) {
        DiscoverabilityLevel max = maxAllowed(tlp);
        DiscoverabilityLevel l = level == null ? NONE : level;
        return l.ordinal() <= max.ordinal() ? l : max;
    }

    /** True when this level is at least {@code other}. */
    public boolean atLeast(DiscoverabilityLevel other) {
        return ordinal() >= other.ordinal();
    }

    /** The level of a stored value; unknown or null values are {@link #NONE}. */
    public static DiscoverabilityLevel fromWire(String value) {
        if (value == null) {
            return NONE;
        }
        for (DiscoverabilityLevel l : values()) {
            if (l.wire.equals(value.strip().toLowerCase(Locale.ROOT))) {
                return l;
            }
        }
        return NONE;
    }

    /**
     * Parses a request value.
     *
     * @throws ValidationException when it is missing or unknown
     */
    public static DiscoverabilityLevel parse(String field, String value) {
        if (value == null || value.isBlank()) {
            throw ValidationException.forField(field, "is required: none, instance or network");
        }
        for (DiscoverabilityLevel l : values()) {
            if (l.wire.equals(value.strip())) {
                return l;
            }
        }
        throw ValidationException.forField(field, "must be none, instance or network");
    }

    /** The TLP rule that caps discoverability, as named in {@code DISCOVERABILITY_NOT_ALLOWED}. */
    public static String rule(Tlp tlp) {
        if (tlp == Tlp.RED) {
            return "TLP:RED chronicles are never discoverable";
        }
        if (tlp == Tlp.AMBER_STRICT) {
            return "TLP:AMBER+STRICT chronicles are discoverable at most within the instance";
        }
        return "TLP:" + (tlp == null ? "AMBER" : tlp.wireValue()) + " allows any discoverability";
    }

    /**
     * Checks a requested level against a TLP label.
     *
     * @throws ForesignalException {@code 409 DISCOVERABILITY_NOT_ALLOWED} with the {@code rule}
     */
    public static void check(DiscoverabilityLevel requested, Tlp tlp) {
        if (requested.ordinal() > maxAllowed(tlp).ordinal()) {
            throw new ConflictException(ErrorCodes.DISCOVERABILITY_NOT_ALLOWED, "The chronicle's TLP label does not"
                    + " allow discoverability '" + requested.wire + "': " + rule(tlp) + ".")
                    .withProperty("rule", rule(tlp));
        }
    }
}
