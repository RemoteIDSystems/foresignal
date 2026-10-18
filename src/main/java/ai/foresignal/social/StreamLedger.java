package ai.foresignal.social;

import ai.foresignal.security.Ulids;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Collection health of complete social streams (prompt 135): connection state, lag and cursor in memory, and gaps in
 * {@code social_stream_gap}. A streaming adapter reports a drop ({@link #disconnected}), a reconnect
 * ({@link #connected}, saying whether its cursor covered the gap), a gap it found on start ({@link #gap}) and its lag.
 *
 * <p>An hour is <strong>complete</strong> for a source when no unreplayed gap overlaps it and the source is not
 * disconnected now with a gap reaching into it ({@link #completeHour}). A first start without a cursor records a gap
 * from the start of the current hour, so a partly collected hour is never called complete.
 */
@Component
public class StreamLedger {

    private static final Logger log = LoggerFactory.getLogger(StreamLedger.class);

    /**
     * The live state of one stream.
     *
     * @param connectedSince when the current connection started, or null while disconnected
     * @param lagSeconds the newest event's age at receipt, or null
     * @param cursor the stream cursor, or null
     * @param lastGap the newest gap, or null
     */
    public record State(Instant connectedSince, Double lagSeconds, String cursor, Gap lastGap) {}

    /**
     * A gap.
     *
     * @param from when collection stopped
     * @param to when it resumed, or null while still disconnected
     * @param replayed whether the missed events were replayed
     * @param reason why, or null
     */
    public record Gap(Instant from, Instant to, boolean replayed, String reason) {}

    private static final class Live {
        volatile Instant connectedSince;
        volatile Double lagSeconds;
        volatile String cursor;
        volatile String openGapId;
        volatile Gap lastGap;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final Map<String, Live> live = new ConcurrentHashMap<>();

    /** Creates the ledger. */
    public StreamLedger(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private Live live(String sourceId) {
        return live.computeIfAbsent(sourceId, k -> new Live());
    }

    /**
     * A stream connected.
     *
     * @param sourceId the source
     * @param at when
     * @param replayed true when the stream resumed from a cursor that covers the time it was down
     */
    public void connected(String sourceId, Instant at, boolean replayed) {
        Live l = live(sourceId);
        l.connectedSince = at;
        String open = l.openGapId;
        try {
            if (open != null) {
                jdbc.update("UPDATE social_stream_gap SET gap_to = :to, replayed = :r WHERE id = :id",
                        new MapSqlParameterSource().addValue("to", Timestamp.from(at)).addValue("r", replayed)
                                .addValue("id", open));
                Gap g = l.lastGap;
                l.lastGap = g == null ? null : new Gap(g.from(), at, replayed, g.reason());
                l.openGapId = null;
            } else {
                jdbc.update("UPDATE social_stream_gap SET gap_to = :to, replayed = :r WHERE source_id = :s AND gap_to IS"
                        + " NULL", new MapSqlParameterSource().addValue("to", Timestamp.from(at))
                        .addValue("r", replayed).addValue("s", sourceId));
            }
        } catch (RuntimeException e) {
            log.debug("Could not close the stream gap of {}: {}", sourceId, e.toString());
        }
    }

    /**
     * A stream dropped.
     *
     * @param sourceId the source
     * @param at when
     * @param reason why, or null
     */
    public void disconnected(String sourceId, Instant at, String reason) {
        Live l = live(sourceId);
        if (l.openGapId != null) {
            return;
        }
        l.connectedSince = null;
        String id = Ulids.next(at);
        try {
            jdbc.update("INSERT INTO social_stream_gap (id, source_id, gap_from, gap_to, replayed, reason, created_at)"
                    + " VALUES (:id, :s, :from, NULL, false, :reason, :at)", new MapSqlParameterSource()
                    .addValue("id", id).addValue("s", sourceId).addValue("from", Timestamp.from(at))
                    .addValue("reason", clip(reason)).addValue("at", Timestamp.from(at)));
            l.openGapId = id;
        } catch (RuntimeException e) {
            log.debug("Could not record the stream gap of {}: {}", sourceId, e.toString());
        }
        l.lastGap = new Gap(at, null, false, clip(reason));
    }

    /**
     * Records a closed gap found by an adapter (for example the time between the stored cursor and now on start).
     *
     * @param sourceId the source
     * @param from when collection stopped
     * @param to when it resumed
     * @param replayed whether the missed events were replayed
     * @param reason why, or null
     */
    public void gap(String sourceId, Instant from, Instant to, boolean replayed, String reason) {
        if (from == null || to == null || !from.isBefore(to)) {
            return;
        }
        Live l = live(sourceId);
        try {
            jdbc.update("INSERT INTO social_stream_gap (id, source_id, gap_from, gap_to, replayed, reason, created_at)"
                    + " VALUES (:id, :s, :from, :to, :r, :reason, :at)", new MapSqlParameterSource()
                    .addValue("id", Ulids.next(to)).addValue("s", sourceId).addValue("from", Timestamp.from(from))
                    .addValue("to", Timestamp.from(to)).addValue("r", replayed).addValue("reason", clip(reason))
                    .addValue("at", Timestamp.from(to)));
        } catch (RuntimeException e) {
            log.debug("Could not record the stream gap of {}: {}", sourceId, e.toString());
        }
        l.lastGap = new Gap(from, to, replayed, clip(reason));
    }

    /**
     * Marks gaps inside a replayed window as replayed (Network Replay backfill).
     *
     * @return the gaps marked
     */
    public int replayed(String sourceId, Instant from, Instant to) {
        try {
            return jdbc.update("UPDATE social_stream_gap SET replayed = true WHERE source_id = :s AND gap_from >= :from"
                    + " AND gap_to IS NOT NULL AND gap_to <= :to", new MapSqlParameterSource().addValue("s", sourceId)
                    .addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to)));
        } catch (RuntimeException e) {
            return 0;
        }
    }

    /** Reports the newest event's lag. */
    public void lag(String sourceId, double seconds) {
        live(sourceId).lagSeconds = seconds;
    }

    /** Reports the stream cursor. */
    public void cursor(String sourceId, String cursor) {
        live(sourceId).cursor = cursor;
    }

    /** The live state of a stream (never null). */
    public State state(String sourceId) {
        Live l = live.get(sourceId);
        if (l == null) {
            return new State(null, null, null, lastGap(sourceId));
        }
        Gap g = l.lastGap != null ? l.lastGap : lastGap(sourceId);
        return new State(l.connectedSince, l.lagSeconds, l.cursor, g);
    }

    /** True while the stream is connected in this process. */
    public boolean connected(String sourceId) {
        Live l = live.get(sourceId);
        return l != null && l.connectedSince != null;
    }

    /**
     * Whether collection of a source was complete over {@code [from, to)}: no unreplayed gap overlaps it (an open gap
     * reaches to now).
     */
    public boolean complete(String sourceId, Instant from, Instant to) {
        try {
            Integer n = jdbc.queryForObject("SELECT count(*) FROM social_stream_gap WHERE source_id = :s AND NOT"
                    + " replayed AND gap_from < :to AND (gap_to IS NULL OR gap_to > :from)", new MapSqlParameterSource()
                    .addValue("s", sourceId).addValue("from", Timestamp.from(from)).addValue("to", Timestamp.from(to)),
                    Integer.class);
            return n == null || n == 0;
        } catch (RuntimeException e) {
            return false;
        }
    }

    /** Whether one hour of a source was complete. */
    public boolean completeHour(String sourceId, Instant hour) {
        return complete(sourceId, hour, hour.plus(Duration.ofHours(1)));
    }

    /** The newest stored gap of a source, or null. */
    public Gap lastGap(String sourceId) {
        try {
            var rows = jdbc.query("SELECT gap_from, gap_to, replayed, reason FROM social_stream_gap WHERE source_id = :s"
                    + " ORDER BY gap_from DESC LIMIT 1", new MapSqlParameterSource().addValue("s", sourceId),
                    (rs, n) -> new Gap(rs.getTimestamp("gap_from").toInstant(), rs.getTimestamp("gap_to") == null
                            ? null : rs.getTimestamp("gap_to").toInstant(), rs.getBoolean("replayed"),
                            rs.getString("reason")));
            return rows.isEmpty() ? null : rows.get(0);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String clip(String reason) {
        return reason == null ? null : reason.length() > 200 ? reason.substring(0, 200) : reason;
    }
}
