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
