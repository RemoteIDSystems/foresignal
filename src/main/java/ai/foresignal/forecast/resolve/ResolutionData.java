package ai.foresignal.forecast.resolve;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * What the resolvers read (prompt 122): occurrences, series aggregates, situation states, coverage and detectability.
 * {@link ResolverEngine} is a pure function of a question, the time and this port, so resolvers are testable without a
 * database; {@link JdbcResolutionData} is the production implementation.
 */
public interface ResolutionData {

    /**
     * One occurrence found by a selector.
     *
     * @param at when it happened
     * @param ref its object reference (or a synthetic one for forms without objects)
     */
    record Occurrence(Instant at, String ref) {}

    /**
     * A series aggregate over a range.
     *
     * @param value the aggregate, or null without usable points
     * @param gapShare the share of the range's buckets without a covered point (0–1)
     * @param evidence the refs to pin
     */
    record Aggregate(Double value, double gapShare, List<String> evidence) {}

    /**
     * A situation's state at a time.
     *
     * @param status its status
     * @param severity its severity
     */
    record SituationState(String status, String severity) {}

    /**
     * What a resolver's evidence is observed by, for coverage and detectability.
     *
     * @param family the 116 observability family, or null when the selector has none
     * @param sources the sources (may be empty: resolved from the kinds)
     * @param kinds the record kinds (may be empty)
     */
    record Scope(String family, List<String> sources, List<String> kinds) {

        /** True when coverage can be judged. */
        public boolean judgeable() {
            return !sources.isEmpty() || !kinds.isEmpty();
        }
    }

    /**
     * A detectability verdict.
     *
     * @param ok true when an absence may be read as "nothing happened"
     * @param reason why not, when not ok
     */
    record Absence(boolean ok, String reason) {}

    /** The occurrences of a selector in an area (or everywhere) within {@code [from, to]}. */
    List<Occurrence> occurrences(Map<String, Object> selector, Map<String, Object> area, Instant from, Instant to);

    /** A series aggregate ({@code last}, {@code mean}, {@code max}, {@code sum}) over {@code [from, to)} as of a time. */
    Aggregate seriesAggregate(String ref, Instant from, Instant to, String aggregate, Instant asOf);

    /** A situation's state at {@code at}, or null when the situation is unknown. */
    SituationState situationAt(String situationId, Instant at);

    /** The observability scope of a resolver. */
    Scope scope(Map<String, Object> resolver);

    /** The covered share (0–1) of {@code [from, to)} for a scope, or NaN when it cannot be judged. */
    double coverage(Scope scope, Instant from, Instant to);

    /** Whether "nothing happened" may be concluded for a scope over an area and window (116 {@code canSeeAbsence}). */
    Absence canSeeAbsence(Scope scope, Map<String, Object> area, Instant from, Instant to);

    /** The publication lag of the resolver's sources, in days. */
    double releaseLagDays(Map<String, Object> resolver);
}
