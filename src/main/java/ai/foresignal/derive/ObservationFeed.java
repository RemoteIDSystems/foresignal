package ai.foresignal.derive;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Envelope;

/**
 * The merged current view a {@link DerivedGenerator} reads (prompt 099): for each entity, the
 * version payload from the {@code ChangeBus} joined with the freshest live state from the
 * {@code LiveBus} ({@code record_head ⋈ record_live}), as {@link Observation}s.
 *
 * <p>Pull-based: a generator asks for the entities of its input sources and kinds in a window and
 * the feed reads the current records. Because the live state is what the {@code LiveBus} keeps
 * fresh, a generator sees the newest position of a SAMPLED track even when persistence thinned its
 * versions. Nothing here is history; a generator that needs a rolling window keeps it in its own
 * bounded state.
 */
public interface ObservationFeed {

    /**
     * The current observations of the given sources and kinds whose {@code observedAt} is at or
     * after {@code since}, ordered by source and external id.
     *
     * @param sources the source ids (at least one)
     * @param kinds the kinds, or empty for every kind
     * @param since the oldest observation time to include
     * @param limit the most rows returned
     */
    List<Observation> recent(Collection<String> sources, Collection<String> kinds, Instant since, int limit);

    /**
     * Like {@link #recent(Collection, Collection, Instant, int)} but restricted to a bounding box, so
     * a regional generator never loads the world.
     *
     * @param bbox the bounding box, or null for no restriction
     */
    List<Observation> recent(Collection<String> sources, Collection<String> kinds, Instant since, Envelope bbox,
            int limit);

    /** One entity's current observation, or empty when it has no head. */
    Optional<Observation> latest(String sourceId, String externalId);

    /** The newest observation time seen for {@code sourceId}, or null when it has none. */
    Instant lastDataAt(String sourceId);

    /**
     * True when a change-bus gap or a live-bus drop happened at or after {@code since}: any
     * generator whose window reaches back to {@code since} must treat its input as degraded and
     * must not emit an absence-based record from that window.
     */
    boolean degradedSince(Instant since);

    /** The time of the last gap or live drop, or null when none happened in this process. */
    Instant lastDegradedAt();
}
