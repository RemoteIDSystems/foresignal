package ai.foresignal.briefing;

import ai.foresignal.api.support.BboxFilter;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Adds a section to digests, briefing markdown and the LLM context (prompt 029). Declare
 * implementations as beans; they run in {@link org.springframework.core.annotation.Order} order
 * after the built-in statistics. Prompt 056 adds "Weather at places".
 *
 * <p>A contributor must not fail the digest: an exception is logged and the section is skipped.
 */
public interface BriefingSectionContributor {

    /**
     * The section for {@code context}, or empty for none.
     *
     * @param context the digest so far and its scope
     */
    Optional<BriefingSection> contribute(Context context);

    /**
     * What a contributor sees.
     *
     * @param stats the built-in statistics (without sections)
     * @param sources the scope's sources (empty for all)
     * @param kinds the scope's kinds (empty for all)
     * @param bbox the scope's area, or null
     * @param periodFrom the period start
     * @param periodTo the period end
     * @param zone the timezone headings use
     * @param briefing the briefing scope when this is a briefing run or preview, else null
     * @param hiddenSources sources the reader may not see (prompt 056: their records are never listed)
     * @param places places of the "Weather at places" section (prompt 056): the briefing scope's, else those of
     *     {@code /v1/digest?places=}; empty for none
     * @param weatherProducts products of that section (prompt 056); empty for the default set
     */
    record Context(DigestStats stats, List<String> sources, List<String> kinds, BboxFilter bbox, Instant periodFrom,
            Instant periodTo, ZoneId zone, BriefingScope briefing, java.util.Set<String> hiddenSources,
            List<String> places, List<String> weatherProducts) {

        /** Copies the collections. */
        public Context {
            hiddenSources = hiddenSources == null ? java.util.Set.of() : java.util.Set.copyOf(hiddenSources);
            places = places == null ? List.of() : List.copyOf(places);
            weatherProducts = weatherProducts == null ? List.of() : List.copyOf(weatherProducts);
        }

        /** A context without the prompt 056 members. */
        public Context(DigestStats stats, List<String> sources, List<String> kinds, BboxFilter bbox,
                Instant periodFrom, Instant periodTo, ZoneId zone, BriefingScope briefing) {
            this(stats, sources, kinds, bbox, periodFrom, periodTo, zone, briefing, java.util.Set.of(), List.of(),
                    List.of());
        }
    }
}
