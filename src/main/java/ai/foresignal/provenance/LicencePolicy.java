package ai.foresignal.provenance;

import ai.foresignal.api.error.ValidationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A client's licence policy (prompt 109): an immutable set of flags that excludes records by licence.
 * The flags are {@code no-nc} (exclude {@code commercialUse != ALLOWED}), {@code no-sa} (exclude
 * share-alike) and {@code redistributable} (exclude {@code redistribution != ALLOWED}). A token's
 * policy is a floor: a request may add flags but never remove the token's.
 *
 * @param noNc exclude non-commercial-restricted licences
 * @param noSa exclude share-alike licences
 * @param redistributable exclude licences that forbid redistribution
 */
public record LicencePolicy(boolean noNc, boolean noSa, boolean redistributable) {

    /** The empty policy. */
    public static final LicencePolicy NONE = new LicencePolicy(false, false, false);

    /** The accepted flag names. */
    public static final List<String> FLAGS = List.of("no-nc", "no-sa", "redistributable");

    /** True when no flag is set. */
    public boolean isEmpty() {
        return !noNc && !noSa && !redistributable;
    }

    /** The union of two policies (the stricter wins). */
    public LicencePolicy plus(LicencePolicy other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (isEmpty()) {
            return other;
        }
        return new LicencePolicy(noNc || other.noNc, noSa || other.noSa, redistributable || other.redistributable);
    }

    /** The flag names in canonical order. */
    public Set<String> names() {
        Set<String> out = new LinkedHashSet<>();
        if (noNc) {
            out.add("no-nc");
        }
        if (noSa) {
            out.add("no-sa");
        }
        if (redistributable) {
            out.add("redistributable");
        }
        return out;
    }

    /** True when {@code terms} are excluded by this policy. */
    public boolean excludes(LicenceTerms terms) {
        if (terms == null) {
            return false;
        }
        if (noNc && terms.effectiveCommercial() != LicenceTerms.CommercialUse.ALLOWED) {
            return true;
        }
        if (noSa && terms.shareAlike()) {
            return true;
        }
        return redistributable && terms.effectiveRedistribution() != LicenceTerms.Redistribution.ALLOWED;
    }

    /**
     * Parses a comma-separated flag list ({@code no-nc,no-sa}).
     *
     * @throws ValidationException {@code 400 VALIDATION_FAILED} for an unknown flag
     */
    public static LicencePolicy parse(String field, String raw) {
        if (raw == null || raw.isBlank()) {
            return NONE;
        }
        boolean noNc = false;
        boolean noSa = false;
        boolean redistributable = false;
        for (String part : raw.split(",")) {
            String flag = part.strip().toLowerCase(Locale.ROOT);
            if (flag.isEmpty()) {
                continue;
            }
            switch (flag) {
                case "no-nc" -> noNc = true;
                case "no-sa" -> noSa = true;
                case "redistributable" -> redistributable = true;
                default -> throw ValidationException.forField(field,
                        "must be a comma-separated subset of no-nc, no-sa, redistributable");
            }
        }
        return new LicencePolicy(noNc, noSa, redistributable);
    }
}
