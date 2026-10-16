package ai.foresignal.intel;

import ai.foresignal.derive.HeadSnapshot;
import java.time.Instant;
import java.util.List;
import org.locationtech.jts.geom.Envelope;

/**
 * Stored record versions inside a time window (prompt 105 post-review): what an {@link EventAnnotator}
 * reads to see the weather <em>at the time of</em> an event, not only the records' current heads. A
 * warning that has since been replaced, or a storm track whose newest version is hours later, still
 * counts when one of its versions falls inside the event's ±3-hour window.
 */
public interface RecordWindowReader {

    /**
     * The non-tombstone versions of one source and kind observed in {@code [from, to]} whose geometry
     * intersects {@code bbox}, newest first.
     *
     * @param sourceId the source
     * @param kind the record kind
     * @param from the window start
     * @param to the window end
     * @param bbox the area of interest (WGS84)
     * @param limit the most versions returned
     * @return each version as a snapshot (its {@code seq} and {@code version} pin the member)
     */
    List<HeadSnapshot> versions(String sourceId, String kind, Instant from, Instant to, Envelope bbox, int limit);
}
