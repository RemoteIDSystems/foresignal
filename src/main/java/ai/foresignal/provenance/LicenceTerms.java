package ai.foresignal.provenance;

import java.util.Locale;

/**
 * The structured licence terms of a source or a fetched version (prompt 109; Weather-Imagery-Design
 * §2.3, Core-Data-Model §14). The {@code id} is an SPDX id where one exists (for example
 * {@code CC-BY-4.0}, {@code ODbL-1.0}) or a slug for provider-specific terms
 * ({@code us-gov-public-domain}, {@code opensky-terms}, {@code unknown}).
 *
 * <p><strong>Egress-safe defaults.</strong> {@link CommercialUse#UNKNOWN} is treated as
 * {@link CommercialUse#FORBIDDEN} and {@link Redistribution#UNKNOWN} as {@link Redistribution#LOCAL_ONLY}
 * by every guard ({@link #effectiveCommercial()}, {@link #effectiveRedistribution()}).
 *
 * @param id the SPDX id or slug
 * @param name a human name
 * @param commercialUse whether commercial use is allowed
 * @param shareAlike true for a copyleft/share-alike licence
 * @param attributionRequired true when attribution must be shown
 * @param redistribution whether records may be redistributed
 * @param attributionText the attribution line to display
 * @param termsUrl the licence or terms URL, or null
 */
public record LicenceTerms(String id, String name, CommercialUse commercialUse, boolean shareAlike,
        boolean attributionRequired, Redistribution redistribution, String attributionText, String termsUrl) {

    /** Whether commercial use is allowed. */
    public enum CommercialUse {
        ALLOWED, FORBIDDEN, UNKNOWN
    }

    /** Whether and how records may be redistributed. */
    public enum Redistribution {
        ALLOWED, LOCAL_ONLY, FORBIDDEN, UNKNOWN
    }

    /** Validates and normalizes. */
    public LicenceTerms {
        if (id == null || id.isBlank()) {
            id = "unknown";
        }
        name = name == null || name.isBlank() ? id : name;
        commercialUse = commercialUse == null ? CommercialUse.UNKNOWN : commercialUse;
        redistribution = redistribution == null ? Redistribution.UNKNOWN : redistribution;
        termsUrl = termsUrl == null || termsUrl.isBlank() ? null : termsUrl.strip();
    }

    /** The restrictive {@code unknown} terms used for an unresolved id. */
    public static LicenceTerms unknown() {
        return new LicenceTerms("unknown", "Unknown licence", CommercialUse.UNKNOWN, false, false,
                Redistribution.UNKNOWN, "Licence unknown; do not redistribute.", null);
    }

    /** The egress-safe commercial use: {@link CommercialUse#UNKNOWN} reads as {@code FORBIDDEN}. */
    public CommercialUse effectiveCommercial() {
        return commercialUse == CommercialUse.UNKNOWN ? CommercialUse.FORBIDDEN : commercialUse;
    }

    /** The egress-safe redistribution: {@link Redistribution#UNKNOWN} reads as {@code LOCAL_ONLY}. */
    public Redistribution effectiveRedistribution() {
        return redistribution == Redistribution.UNKNOWN ? Redistribution.LOCAL_ONLY : redistribution;
    }

    /** True when the terms forbid redistribution entirely. */
    public boolean forbidsRedistribution() {
        return effectiveRedistribution() == Redistribution.FORBIDDEN;
    }

    /** True when the terms restrict redistribution to this instance ({@code LOCAL_ONLY} or {@code FORBIDDEN}). */
    public boolean localOnly() {
        Redistribution r = effectiveRedistribution();
        return r == Redistribution.LOCAL_ONLY || r == Redistribution.FORBIDDEN;
    }

    /**
     * The per-record MLAT downgrade (prompt 109, task 1): the same terms with redistribution
     * {@code LOCAL_ONLY}, so an MLAT position never leaves on a re-serving path, while the license id,
     * share-alike, attribution requirement, attribution text and terms URL are kept (an ODbL record stays
     * an ODbL record that must be attributed). Terms already {@code LOCAL_ONLY} or {@code FORBIDDEN} are
     * returned unchanged.
     */
    public LicenceTerms downgradedToLocalOnly() {
        Redistribution r = effectiveRedistribution();
        if (r == Redistribution.LOCAL_ONLY || r == Redistribution.FORBIDDEN) {
            return this;
        }
        return new LicenceTerms(id, name, commercialUse, shareAlike, attributionRequired, Redistribution.LOCAL_ONLY,
                attributionText, termsUrl);
    }

    /**
     * The same terms with redistribution {@code ALLOWED}: the operator's {@code OPEN} egress override of an
     * {@code egressOverridable} source lifts the {@code LOCAL_ONLY} default of its own receiver data
     * ({@code docs/Conventions.md}, "Operator choice"). {@code FORBIDDEN} terms are returned unchanged.
     */
    public LicenceTerms openedByOperator() {
        if (effectiveRedistribution() != Redistribution.LOCAL_ONLY) {
            return this;
        }
        return new LicenceTerms(id, name, commercialUse, shareAlike, attributionRequired, Redistribution.ALLOWED,
                attributionText, termsUrl);
    }

    /**
     * A restrictiveness rank for choosing the strictest of several terms: redistribution first
     * ({@code FORBIDDEN} &gt; {@code LOCAL_ONLY}/{@code UNKNOWN} &gt; {@code ALLOWED}), then commercial use,
     * then share-alike, then the attribution requirement. Higher is stricter.
     */
    public int restrictiveness() {
        int rank = switch (effectiveRedistribution()) {
            case FORBIDDEN -> 2;
            case LOCAL_ONLY, UNKNOWN -> 1;
            case ALLOWED -> 0;
        };
        rank = rank * 2 + (effectiveCommercial() == CommercialUse.ALLOWED ? 0 : 1);
        rank = rank * 2 + (shareAlike ? 1 : 0);
        return rank * 2 + (attributionRequired ? 1 : 0);
    }

    /** The policy flag {@code no-nc} excludes this licence. */
    public boolean nonCommercial() {
        return effectiveCommercial() != CommercialUse.ALLOWED;
    }

    /** Parses a {@link CommercialUse} wire value. */
    public static CommercialUse commercial(String value) {
        if (value == null) {
            return CommercialUse.UNKNOWN;
        }
        try {
            return CommercialUse.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return CommercialUse.UNKNOWN;
        }
    }

    /** Parses a {@link Redistribution} wire value. */
    public static Redistribution redistribution(String value) {
        if (value == null) {
            return Redistribution.UNKNOWN;
        }
        try {
            return Redistribution.valueOf(value.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Redistribution.UNKNOWN;
        }
    }
}
