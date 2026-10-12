package ai.foresignal.llm;

import ai.foresignal.collect.UsageResolution;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.llm.client.TokenUsage;
import jakarta.annotation.PreDestroy;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Counts LLM spend per provider per minute (FR-45), flushed to {@code llm_usage} every 60 s, the same
 * pattern as {@code UsageMeter}: in-memory {@link LongAdder} counters, drained under the key's bin
 * lock so no increment is lost, and reads that merge persisted rows with unflushed counters.
 */
@Component
public class LlmUsageMeter implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(LlmUsageMeter.class);

    static final long FLUSH_INTERVAL_MS = 60_000L;

    /** One (provider, minute)'s counters. */
    static final class Counters {
        final LongAdder requests = new LongAdder();
        final LongAdder inputTokens = new LongAdder();
        final LongAdder outputTokens = new LongAdder();
        final LongAdder cachedTokens = new LongAdder();
        final LongAdder errors = new LongAdder();
        final LongAdder refused = new LongAdder();

        LlmUsagePoint snapshot(Instant t) {
            return new LlmUsagePoint(t, requests.sum(), inputTokens.sum(), outputTokens.sum(), cachedTokens.sum(),
                    errors.sum(), refused.sum());
        }
    }

    /** Identifies one minute of one provider. */
    record Key(String providerId, Instant minute) {}

    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final ConcurrentHashMap<Key, Counters> live = new ConcurrentHashMap<>();

    /** Creates the meter. */
    public LlmUsageMeter(JdbcTemplate jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    /** Records one completed call and its tokens. */
    public void recordCall(String providerId, TokenUsage usage) {
        TokenUsage u = usage == null ? TokenUsage.ZERO : usage;
        update(providerId, c -> {
            c.requests.increment();
            c.inputTokens.add(Math.max(0, u.inputTokens()));
            c.outputTokens.add(Math.max(0, u.outputTokens()));
            c.cachedTokens.add(Math.max(0, u.cachedTokens()));
        });
    }

    /** Records one call that failed at the provider. */
    public void recordError(String providerId) {
        update(providerId, c -> {
            c.requests.increment();
            c.errors.increment();
        });
    }

    /** Records one call the budget refused (never sent). */
    public void recordRefused(String providerId) {
        update(providerId, c -> c.refused.increment());
    }

    private void update(String providerId, Consumer<Counters> change) {
        live.compute(new Key(providerId, clock.instant().truncatedTo(ChronoUnit.MINUTES)), (key, counters) -> {
            Counters target = counters == null ? new Counters() : counters;
            change.accept(target);
            return target;
        });
    }

    /** Writes the live counters to {@code llm_usage} and clears them. */
    @Scheduled(fixedDelay = FLUSH_INTERVAL_MS)
    public void flush() {
        List<Object[]> batch = new ArrayList<>();
        for (Key key : List.copyOf(live.keySet())) {
            Counters counters = live.remove(key);
            if (counters == null) {
                continue;
            }
            LlmUsagePoint p = counters.snapshot(key.minute());
            batch.add(new Object[] {key.providerId(), Timestamp.from(key.minute()), p.requests(), p.inputTokens(),
                p.outputTokens(), p.cachedTokens(), p.errors(), p.refused()});
        }
        if (batch.isEmpty()) {
            return;
        }
        try {
            jdbc.batchUpdate(UPSERT_SQL, batch);
        } catch (RuntimeException e) {
            // Losing a minute of accounting is better than failing the scheduler thread; it widens
            // the daily budget seed after a restart, so it is logged.
            log.warn("Could not flush {} LLM usage row(s); those counts are lost", batch.size(), e);
        }
    }

    /** Flushes on shutdown so the final minute is kept. */
    @PreDestroy
    public void flushOnShutdown() {
        flush();
    }

    private static final String UPSERT_SQL = """
            insert into llm_usage (provider_id, minute, requests, input_tokens, output_tokens, cached_tokens, errors, refused)
            values (?, ?, ?, ?, ?, ?, ?, ?)
            on conflict (provider_id, minute) do update set
                requests      = llm_usage.requests      + excluded.requests,
                input_tokens  = llm_usage.input_tokens  + excluded.input_tokens,
                output_tokens = llm_usage.output_tokens + excluded.output_tokens,
                cached_tokens = llm_usage.cached_tokens + excluded.cached_tokens,
                errors        = llm_usage.errors        + excluded.errors,
                refused       = llm_usage.refused       + excluded.refused
            """;

    /**
     * Input plus output tokens used today (UTC) by {@code providerId}, or by every provider when null,
     * including unflushed counters. Seeds the daily budget after a restart.
     */
    public long tokensToday(String providerId) {
        Instant start = clock.instant().atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
        LlmUsagePoint totals = totals(providerId, start, clock.instant().plusSeconds(60));
        return totals.inputTokens() + totals.outputTokens();
    }

    /** Totals over {@code [from, to)} for one provider, or every provider when null. */
    public LlmUsagePoint totals(String providerId, Instant from, Instant to) {
        LlmUsagePoint total = jdbc.query("""
                select coalesce(sum(requests), 0), coalesce(sum(input_tokens), 0), coalesce(sum(output_tokens), 0),
                       coalesce(sum(cached_tokens), 0), coalesce(sum(errors), 0), coalesce(sum(refused), 0)
                  from llm_usage
                 where (cast(? as varchar) is null or provider_id = ?) and minute >= ? and minute < ?
                """, rs -> rs.next() ? new LlmUsagePoint(from, rs.getLong(1), rs.getLong(2), rs.getLong(3),
                        rs.getLong(4), rs.getLong(5), rs.getLong(6)) : zero(from),
                providerId, providerId, Timestamp.from(from), Timestamp.from(to));
        if (total == null) {
            total = zero(from);
        }
        for (Map.Entry<Key, Counters> entry : live.entrySet()) {
            if (matches(entry.getKey(), providerId, from, to)) {
                total = total.plus(entry.getValue().snapshot(from));
            }
        }
        return total;
    }

    /**
     * Usage per bucket over {@code [from, to)} for one provider, or every provider summed when null.
     * Only non-empty buckets appear.
     */
    public List<LlmUsagePoint> series(String providerId, Instant from, Instant to, UsageResolution resolution) {
        Map<Instant, LlmUsagePoint> buckets = new TreeMap<>();
        jdbc.query("""
                select date_trunc(?, minute, 'UTC') as bucket, sum(requests), sum(input_tokens), sum(output_tokens),
                       sum(cached_tokens), sum(errors), sum(refused)
                  from llm_usage
                 where (cast(? as varchar) is null or provider_id = ?) and minute >= ? and minute < ?
                 group by bucket
                 order by bucket
                """, rs -> {
                    Instant t = rs.getTimestamp(1).toInstant();
                    buckets.put(t, new LlmUsagePoint(t, rs.getLong(2), rs.getLong(3), rs.getLong(4), rs.getLong(5),
                            rs.getLong(6), rs.getLong(7)));
                }, resolution.sqlField(), providerId, providerId, Timestamp.from(from), Timestamp.from(to));
        live.forEach((key, counters) -> {
            if (matches(key, providerId, from, to)) {
                Instant bucket = truncate(key.minute(), resolution);
                buckets.merge(bucket, counters.snapshot(bucket), LlmUsagePoint::plus);
            }
        });
        return List.copyOf(buckets.values());
    }

    private static boolean matches(Key key, String providerId, Instant from, Instant to) {
        return (providerId == null || key.providerId().equals(providerId)) && !key.minute().isBefore(from)
                && key.minute().isBefore(to);
    }

    private static Instant truncate(Instant minute, UsageResolution resolution) {
        return switch (resolution) {
            case MINUTE -> minute.truncatedTo(ChronoUnit.MINUTES);
            case HOUR -> minute.truncatedTo(ChronoUnit.HOURS);
            case DAY -> minute.truncatedTo(ChronoUnit.DAYS);
        };
    }

    private static LlmUsagePoint zero(Instant t) {
        return new LlmUsagePoint(t, 0, 0, 0, 0, 0, 0);
    }

    /** Discards unflushed counters (tests: the database was emptied underneath). */
    @Override
    public void resetState() {
        live.clear();
    }
}
