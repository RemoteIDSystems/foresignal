package ai.foresignal.evidence;

import java.time.Instant;
import java.util.Map;

/**
 * The resolver of a canonical-event family whose rule is {@code match: "story"} (prompt 133: {@code news-story};
 * prompt 136: {@code social-narrative}):
 * text clusters rather than places and times, so the {@link EvidenceResolver} hands their versions to this SPI on its
 * own cursor instead of matching them itself. The handler writes the 116 tables through {@link EvidenceStore} (events,
 * links, evidence groups) and its own tables; it never modifies an observation (MEM-1).
 */
public interface StoryFamilyHandler {

    /** The match name of the families this handler resolves. */
    String STORY_MATCH = "story";

    /**
     * Whether this handler resolves a family (prompt 136: several story families, one handler each).
     *
     * @param family the family name
     */
    default boolean handles(String family) {
        return true;
    }

    /**
     * Resolves one version, exactly once per seq (the handler skips a version at or below the seq it already linked).
     *
     * @param rule the family rule (its {@code story} parameters)
     * @param observation the version
     */
    void process(CanonicalRules.FamilyRule rule, Observation observation);

    /**
     * One version of a story family's kind.
     *
     * @param seq the seq
     * @param sourceId the source
     * @param externalId the record
     * @param kind the kind
     * @param version the record version, or null
     * @param eventTime the event time ({@code event_time}, else {@code observed_at})
     * @param observedAt {@code observed_at}
     * @param payload the payload
     */
    record Observation(long seq, String sourceId, String externalId, String kind, Integer version, Instant eventTime,
            Instant observedAt, Map<String, Object> payload) {}
}
