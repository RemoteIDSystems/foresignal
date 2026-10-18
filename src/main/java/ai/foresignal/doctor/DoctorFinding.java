package ai.foresignal.doctor;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * One deterministic diagnosis finding (OpenAPI {@code DoctorFinding}).
 *
 * @param code what was found
 * @param severity how bad it is
 * @param evidence the facts behind it; heuristic evidence (the parser path scan) is marked as such
 * @param path the structure path concerned, or null
 * @param suggestedAction what to do about it
 * @param heuristic true when the finding relies on the static parser scan
 */
public record DoctorFinding(FindingCode code, Severity severity, List<String> evidence, String path,
        String suggestedAction, boolean heuristic) {

    /** Severity, as the OpenAPI {@code Severity} enum. */
    public enum Severity {
        /** For information. */
        INFO,
        /** Worth a look. */
        WARNING,
        /** The source is broken. */
        CRITICAL;

        /** The wire value, lowercase. */
        @JsonValue
        public String wireValue() {
            return name().toLowerCase(Locale.ROOT);
        }

        /** Parses a wire value; null for an unknown one. */
        public static Severity fromWire(String value) {
            for (Severity s : values()) {
                if (s.wireValue().equalsIgnoreCase(value)) {
                    return s;
                }
            }
            return null;
        }
    }

    /** Checks and copies the members. */
    public DoctorFinding {
        Objects.requireNonNull(code, "code");
        Objects.requireNonNull(severity, "severity");
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
        suggestedAction = suggestedAction == null ? "" : suggestedAction;
    }

    /** A finding without a path that relies on no heuristic. */
    public static DoctorFinding of(FindingCode code, Severity severity, List<String> evidence, String action) {
        return new DoctorFinding(code, severity, evidence, null, action, false);
    }
}
