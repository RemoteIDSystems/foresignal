package ai.foresignal.store.query;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Translates transaction times into seq bounds through {@code seq_clock} (Core-Data-Model §8).
 *
 * <ul>
 *   <li>{@code cutoffSeq(t)}: the {@code first_seq} of the latest minute at or before {@code t};
 *       every seq below it was ingested before {@code t}. 0 when no minute is that old.
 *   <li>{@code S(T)}: the largest seq whose {@code ingested_at <= T}. With {@code m} the latest
 *       minute at or before {@code T}, it is the largest seq in {@code [first_seq(m),
 *       first_seq(next minute))} with {@code ingested_at <= T}, else {@code first_seq(m) - 1}; 0
 *       when no minute is that old. Exact to the batch, because {@code ingested_at} never decreases
 *       with seq (I3).
 * </ul>
 *
 * <p>Methods take the template explicitly so they run inside the caller's read transaction.
 */
@Component
public class SeqClockQuery {

    static final String CUTOFF =
            "SELECT minute, first_seq FROM seq_clock WHERE minute <= :t ORDER BY minute DESC LIMIT 1";

    static final String NEXT_MINUTE =
            "SELECT first_seq FROM seq_clock WHERE minute > :m ORDER BY minute LIMIT 1";

    static final String MAX_SEQ_BOUNDED =
            "SELECT max(seq) FROM record_version WHERE seq >= :lo AND seq < :hi AND ingested_at <= :t";

    static final String MAX_SEQ_OPEN =
            "SELECT max(seq) FROM record_version WHERE seq >= :lo AND ingested_at <= :t";

    /** {@code cutoffSeq(t)}. */
    public long cutoffSeq(NamedParameterJdbcTemplate jdbc, Instant t) {
        Minute minute = latestMinute(jdbc, t);
        return minute == null ? 0L : minute.firstSeq();
    }

    /** {@code S(T)}. */
    public long seqAt(NamedParameterJdbcTemplate jdbc, Instant t) {
        Minute minute = latestMinute(jdbc, t);
        if (minute == null) {
            return 0L;
        }
        List<Long> next = jdbc.queryForList(
                NEXT_MINUTE, new MapSqlParameterSource("m", utc(minute.minute())), Long.class);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("lo", minute.firstSeq())
                .addValue("t", utc(t));
        Long max;
        if (next.isEmpty() || next.getFirst() == null) {
            max = jdbc.queryForObject(MAX_SEQ_OPEN, params, Long.class);
        } else {
            max = jdbc.queryForObject(MAX_SEQ_BOUNDED, params.addValue("hi", next.getFirst()), Long.class);
        }
        return resolve(minute.firstSeq(), max);
    }

    /**
     * The seq bound for "ingested before {@code t}" (exclusive): every seq at or below the result has
     * {@code ingested_at < t}. Equals {@code S(t - 1 µs)}.
     */
    public long lastSeqBefore(NamedParameterJdbcTemplate jdbc, Instant t) {
        return seqAt(jdbc, t.minusNanos(1000));
    }

    /** {@code coalesce(maxInMinute, firstSeq - 1)}, never negative. */
    static long resolve(long firstSeq, Long maxInMinute) {
        long value = maxInMinute != null ? maxInMinute : firstSeq - 1;
        return Math.max(0L, value);
    }

    private static Minute latestMinute(NamedParameterJdbcTemplate jdbc, Instant t) {
        List<Minute> rows = jdbc.query(
                CUTOFF,
                new MapSqlParameterSource("t", utc(t)),
                (rs, i) -> new Minute(rs.getObject(1, OffsetDateTime.class).toInstant(), rs.getLong(2)));
        return rows.isEmpty() ? null : rows.getFirst();
    }

    private static OffsetDateTime utc(Instant t) {
        return OffsetDateTime.ofInstant(t, ZoneOffset.UTC);
    }

    /** One {@code seq_clock} row. */
    record Minute(Instant minute, long firstSeq) {}
}
