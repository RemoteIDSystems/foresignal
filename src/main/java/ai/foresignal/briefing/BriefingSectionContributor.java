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
     */
    record Context(DigestStats stats, List<String> sources, List<String> kinds, BboxFilter bbox, Instant periodFrom,
            Instant periodTo, ZoneId zone, BriefingScope briefing) {}
}
