package ai.foresignal.media.outlet;

import java.time.Instant;
import java.util.List;

/**
 * One rating as a provider import reads it, before its outlet is resolved (prompt 133).
 *
 * @param domain the outlet's domain
 * @param dimension the dimension ({@link MediaVocabulary#DIMENSIONS} or {@code {provider}:{axis}})
 * @param valueNorm the normalized value (lean −2…+2, factuality 1…5), or null for a label-only rating
 * @param valueRaw the provider's raw value, or null
 * @param label the provider's label, or null
 * @param ratedAt the provider's rating date, or null
 * @param license the row's license (operator rows), or null for the provider's
 * @param attribution the row's attribution (operator rows), or null for the provider's
 * @param url a link to the rating, or null
 * @param outlet registry facts the provider gives with the rating, or null
 */
public record RatingRow(String domain, String dimension, Double valueNorm, String valueRaw, String label,
        Instant ratedAt, String license, String attribution, String url, OutletFacts outlet) {

    /**
     * Registry facts (never ratings) a provider gives about an outlet.
     *
     * @param name the outlet's name, or null
     * @param country the ISO 3166-1 alpha-2 country, or null
     * @param languages the language codes
     * @param ownerOrg the owning organization's name, or null
     */
    public record OutletFacts(String name, String country, List<String> languages, String ownerOrg) {

        /** Copies the list. */
        public OutletFacts {
            languages = languages == null ? List.of() : List.copyOf(languages);
        }
    }
}
