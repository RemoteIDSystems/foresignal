package ai.foresignal.social;

import ai.foresignal.security.Ulids;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Platform quotas in rolling windows (prompt 135): YouTube units (10,000 a day) and search calls (100 a day), Threads
 * keyword queries (2,200 per rolling 24 hours), Reddit queries (per minute) and Instagram's 30 unique hashtags per
 * rolling 7 days. Rolling windows are conservative for calendar quotas: they never allow more than the calendar
 * window would.
 */
@Component
public class QuotaLedger {

    /** How long uses are kept. */
    static final Duration KEEP = Duration.ofDays(8);

    private final NamedParameterJdbcTemplate jdbc;
    private final Clock clock;

    /** Creates the ledger. */
    public QuotaLedger(NamedParameterJdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** The amount used in the window ending now. */
    public long used(String sourceId, String unit, Duration window) {
        Instant now = clock.instant();
        Long n = jdbc.queryForObject("SELECT coalesce(sum(amount), 0) FROM social_quota_use WHERE source_id = :s AND"
                + " unit = :u AND at > :since", new MapSqlParameterSource().addValue("s", sourceId).addValue("u", unit)
                .addValue("since", Timestamp.from(now.minus(window))), Long.class);
        return n == null ? 0 : n;
    }

    /**
     * Uses {@code amount} when it fits under {@code limit} in the window.
     *
     * @return true when used, false when the quota would be exceeded (nothing recorded)
     */
    public synchronized boolean tryUse(String sourceId, String unit, long limit, Duration window, long amount) {
        if (used(sourceId, unit, window) + amount > limit) {
            return false;
        }
        record(sourceId, unit, null, amount);
        return true;
    }

    /** Records a use. */
    public void record(String sourceId, String unit, String key, long amount) {
        Instant now = clock.instant();
        jdbc.update("INSERT INTO social_quota_use (id, source_id, unit, use_key, amount, at) VALUES (:id, :s, :u, :k,"
                + " :a, :at)", new MapSqlParameterSource().addValue("id", Ulids.next(now)).addValue("s", sourceId)
                .addValue("u", unit).addValue("k", key).addValue("a", amount).addValue("at", Timestamp.from(now)));
    }

    /** The distinct keys used in the window ending now. */
    public List<String> keys(String sourceId, String unit, Duration window) {
        Instant now = clock.instant();
        return jdbc.queryForList("SELECT DISTINCT use_key FROM social_quota_use WHERE source_id = :s AND unit = :u AND"
                + " at > :since AND use_key IS NOT NULL ORDER BY use_key", new MapSqlParameterSource()
                .addValue("s", sourceId).addValue("u", unit).addValue("since", Timestamp.from(now.minus(window))),
                String.class);
    }

    /**
     * Uses a keyed slot (an Instagram hashtag): a key already used in the window is free; a new key needs room
     * under {@code limit} distinct keys.
     *
     * @return true when the key may be used
     */
    public synchronized boolean tryKey(String sourceId, String unit, String key, long limit, Duration window) {
        List<String> used = keys(sourceId, unit, window);
        if (used.contains(key)) {
            return true;
        }
        if (used.size() >= limit) {
            return false;
        }
        record(sourceId, unit, key, 1);
        return true;
    }

    /** When the oldest use in the window leaves it, or null when the window is empty. */
    public Instant resetsAt(String sourceId, String unit, Duration window) {
        Instant now = clock.instant();
        List<Timestamp> oldest = jdbc.queryForList("SELECT min(at) FROM social_quota_use WHERE source_id = :s AND unit ="
                + " :u AND at > :since", new MapSqlParameterSource().addValue("s", sourceId).addValue("u", unit)
                .addValue("since", Timestamp.from(now.minus(window))), Timestamp.class);
        return oldest.isEmpty() || oldest.get(0) == null ? null : oldest.get(0).toInstant().plus(window);
    }

    /** Deletes uses older than eight days. */
    public int prune() {
        return jdbc.update("DELETE FROM social_quota_use WHERE at < :before", new MapSqlParameterSource()
                .addValue("before", Timestamp.from(clock.instant().minus(KEEP))));
    }
}
