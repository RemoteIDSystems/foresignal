package ai.foresignal.source;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Locale;

/**
 * A source's derived health (FR-10, OpenAPI {@code HealthStatus}).
 *
 * <p>Ordered from least to most alarming among the statuses of a running source, which is what lets
 * a pluggable {@code HealthRule} contribute a status by "the worse one wins". {@link #DISABLED} and
 * {@link #IDLE} sit outside that comparison: they describe a source that is not running, not one
 * that is running badly.
 */
public enum HealthStatus {
    /** Enabled, attempted, and nothing is wrong. */
    HEALTHY,
    /** Working, but something deserves a look: a recent failure, a silent break, refusals. */
    DEGRADED,
    /** Not working: repeated failures, or held back by an upstream's backoff request. */
    FAILING,
    /** Enabled but not yet attempted. */
    IDLE,
    /** Not enabled. */
    DISABLED;

    /** The wire form, lowercase, as in the OpenAPI enum. */
    @JsonValue
    public String value() {
        return name().toLowerCase(Locale.ROOT);
    }

    /**
     * Parses the wire form, case-insensitively.
     *
     * @throws IllegalArgumentException when {@code value} is not a status
     */
    public static HealthStatus fromValue(String value) {
        return valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    /** True for the statuses of a running source, which can be compared by severity. */
    public boolean isRunning() {
        return this == HEALTHY || this == DEGRADED || this == FAILING;
    }

    /** The more severe of two running statuses; {@code this} when either is not running. */
    public HealthStatus worse(HealthStatus other) {
        if (other == null || !isRunning() || !other.isRunning()) {
            return this;
        }
        return other.ordinal() > ordinal() ? other : this;
    }
}
