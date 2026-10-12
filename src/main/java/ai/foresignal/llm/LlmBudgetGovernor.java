package ai.foresignal.llm;

import ai.foresignal.collect.ClockTimeMeter;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.notify.SystemEvent;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * The LLM budgets (FR-45, NFR-13): per provider, a Bucket4j {@code requestsPerMinute} bucket and a
 * daily token budget aligned to UTC midnight.
 *
 * <p>Before a call, {@link #reserve} estimates its tokens (prompt characters / 4, plus the output
 * cap) and refuses with {@link LlmBudgetExceededException} when the estimate exceeds what is left
 * today, or when the minute's requests are spent. A refused call is not sent and not queued. The
 * estimate is held against the day until {@link #settle} replaces it with the tokens the provider
 * reported.
 *
 * <p>The day's figure is seeded from {@code llm_usage} (through {@link LlmUsageMeter}) the first time
 * a provider is used after start (or after its budget changes), so a restart cannot reset the
 * allowance; at UTC midnight the day starts again from zero. The first refusal of a day raises a {@code system} notification.
 */
@Component
public class LlmBudgetGovernor implements IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(LlmBudgetGovernor.class);

    /** Characters per estimated token. */
    public static final int CHARS_PER_TOKEN = 4;

    private final LlmUsageMeter usage;
    private final ApplicationEventPublisher events;
    private final Clock clock;
    private final ClockTimeMeter timeMeter;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();

    /** Creates the governor. */
    public LlmBudgetGovernor(LlmUsageMeter usage, ApplicationEventPublisher events, Clock clock) {
        this.usage = usage;
        this.events = events;
        this.clock = clock;
        this.timeMeter = new ClockTimeMeter(clock);
    }

    /** The estimated tokens of a call: {@code promptCharacters / 4} rounded up, plus {@code maxOutputTokens}. */
    public static long estimate(long promptCharacters, long maxOutputTokens) {
        return (promptCharacters + CHARS_PER_TOKEN - 1) / CHARS_PER_TOKEN + Math.max(0, maxOutputTokens);
    }

    /**
     * Reserves {@code estimatedTokens} and one request for {@code provider}.
     *
     * @return the reservation to {@link #settle}
     * @throws LlmBudgetExceededException when the day's tokens or the minute's requests are spent
     */
    public Reservation reserve(LlmProviderConfig provider, long estimatedTokens) {
        Entry entry = entry(provider);
        synchronized (entry) {
            rollOver(entry);
            long remaining = Math.max(0, entry.tokensPerDay - entry.usedToday);
            if (estimatedTokens > remaining) {
                refused(provider.id(), entry);
                throw new LlmBudgetExceededException(provider.id(), "The call to LLM provider '" + provider.id()
                        + "' needs an estimated " + estimatedTokens + " tokens; " + remaining + " of its "
                        + entry.tokensPerDay + " tokens per day remain.", untilMidnight());
            }
            if (!entry.bucket.tryConsume(1)) {
                long nanos = entry.bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
                usage.recordRefused(provider.id());
                throw new LlmBudgetExceededException(provider.id(), "LLM provider '" + provider.id()
                        + "' has used its " + entry.requestsPerMinute + " requests per minute.",
                        Duration.ofNanos(Math.max(nanos, 1_000_000_000L)));
            }
            entry.usedToday += estimatedTokens;
            return new Reservation(provider.id(), estimatedTokens, entry.day);
        }
    }

    /** Replaces a reservation's estimate with the tokens actually used (zero when the call failed before use). */
    public void settle(Reservation reservation, long actualTokens) {
        Entry entry = entries.get(reservation.providerId());
        if (entry == null) {
            return;
        }
        synchronized (entry) {
            if (!entry.day.equals(reservation.day())) {
                // Reserved yesterday: today's seed already counts what the meter recorded.
                return;
            }
            entry.usedToday = Math.max(0, entry.usedToday - reservation.estimatedTokens() + Math.max(0, actualTokens));
        }
    }

    /** Tokens counted against today for {@code providerId}, reservations included. */
    public long usedToday(LlmProviderConfig provider) {
        Entry entry = entry(provider);
        synchronized (entry) {
            rollOver(entry);
            return entry.usedToday;
        }
    }

    /** Discards a provider's state, so the next use rebuilds it from its configuration and the meter. */
    public void invalidate(String providerId) {
        entries.remove(providerId);
    }

    @Override
    public void resetState() {
        entries.clear();
    }

    private Entry entry(LlmProviderConfig provider) {
        Entry entry = entries.compute(provider.id(), (id, existing) -> {
            if (existing != null && existing.requestsPerMinute == provider.requestsPerMinute()
                    && existing.tokensPerDay == provider.tokensPerDay()) {
                return existing;
            }
            Entry fresh = new Entry(provider.requestsPerMinute(), provider.tokensPerDay(), bucket(provider), today());
            fresh.usedToday = existing != null && existing.day.equals(fresh.day) ? existing.usedToday
                    : usage.tokensToday(id);
            return fresh;
        });
        return entry;
    }

    private LocalBucket bucket(LlmProviderConfig provider) {
        Bandwidth perMinute = Bandwidth.builder()
                .capacity(provider.requestsPerMinute())
                .refillGreedy(provider.requestsPerMinute(), Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(perMinute)
                .withCustomTimePrecision(timeMeter)
                .withSynchronizationStrategy(SynchronizationStrategy.LOCK_FREE)
                .build();
    }

    private void rollOver(Entry entry) {
        LocalDate today = today();
        if (!entry.day.equals(today)) {
            entry.day = today;
            entry.notified = false;
            entry.usedToday = 0;
        }
    }

    private void refused(String providerId, Entry entry) {
        usage.recordRefused(providerId);
        if (!entry.notified) {
            entry.notified = true;
            log.warn("LLM provider {} has exhausted its daily token budget of {}", providerId, entry.tokensPerDay);
            events.publishEvent(new SystemEvent("system.llm_budget_exhausted", "warning",
                    "LLM budget exhausted: " + providerId,
                    "LLM provider `" + providerId + "` has used its daily budget of " + entry.tokensPerDay
                            + " tokens. Calls are refused until 00:00 UTC.",
                    "/v1/llm/providers/" + providerId,
                    Map.of("providerId", providerId, "tokensPerDay", entry.tokensPerDay)));
        }
    }

    private LocalDate today() {
        return clock.instant().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private Duration untilMidnight() {
        Instant now = clock.instant();
        Instant midnight = today().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        return Duration.between(now, midnight);
    }

    /**
     * A held reservation.
     *
     * @param providerId the provider
     * @param estimatedTokens the tokens held
     * @param day the UTC day it was taken on
     */
    public record Reservation(String providerId, long estimatedTokens, LocalDate day) {}

    /** One provider's budget state; guarded by its own monitor. */
    private static final class Entry {
        final int requestsPerMinute;
        final long tokensPerDay;
        final LocalBucket bucket;
        LocalDate day;
        long usedToday;
        boolean notified;

        Entry(int requestsPerMinute, long tokensPerDay, LocalBucket bucket, LocalDate day) {
            this.requestsPerMinute = requestsPerMinute;
            this.tokensPerDay = tokensPerDay;
            this.bucket = bucket;
            this.day = day;
        }
    }
}
