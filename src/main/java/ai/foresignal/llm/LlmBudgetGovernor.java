package ai.foresignal.llm;

import ai.foresignal.collect.ClockTimeMeter;
import ai.foresignal.ingest.IngestStateReset;
import ai.foresignal.notify.SystemEvent;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.LocalBucket;
import io.github.bucket4j.local.SynchronizationStrategy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * The LLM budgets (FR-45, NFR-13): per provider, a Bucket4j {@code requestsPerMinute} bucket, a
 * daily token budget aligned to UTC midnight, and (API 1.5.0) USD caps per UTC hour, day and month.
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
 *
 * <p>The USD caps count the provider's own metered spend: each call's reported tokens at the
 * provider's prices ({@link LlmPricing}), the figure {@link LlmUsageMeter} stores as
 * {@code llm_usage.cost_usd}. A call is refused once a capped window's spend, plus the estimated cost
 * of the calls still in flight, has reached its cap, with {@code Retry-After} at that window's reset
 * (the latest one when several are spent). The windows reset on UTC boundaries: the top of the hour,
 * 00:00 and the 1st of the month. Each window is seeded from {@code llm_usage} the first time the
 * provider is used after start, as the daily tokens are, and a call settled after its window reset
 * counts in the new one, where the meter records it. The first refusal in a window raises a
 * {@code system} notification. Spend is tracked whether or not a cap is set, so
 * {@link #spend} reports it for every priced provider.
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
    private final Map<String, Spend> spends = new ConcurrentHashMap<>();

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
        return reserve(provider, estimatedTokens, BigDecimal.ZERO);
    }

    /**
     * Reserves {@code estimatedTokens}, {@code estimatedCostUsd} and one request for {@code provider}.
     * The USD caps are checked first, then the daily tokens and the minute's requests.
     *
     * @return the reservation to {@link #settle(Reservation, long, BigDecimal)}
     * @throws LlmBudgetExceededException when a USD cap, the day's tokens or the minute's requests are spent
     */
    public Reservation reserve(LlmProviderConfig provider, long estimatedTokens, BigDecimal estimatedCostUsd) {
        BigDecimal cost = estimatedCostUsd == null || estimatedCostUsd.signum() < 0 ? BigDecimal.ZERO
                : estimatedCostUsd;
        Spend spend = spendOf(provider.id());
        Map<LlmSpendWindow, Instant> windows;
        synchronized (spend) {
            spend.rollOver(clock.instant());
            checkCaps(provider, spend);
            windows = spend.hold(cost);
        }
        try {
            return reserveTokens(provider, estimatedTokens, cost, windows);
        } catch (LlmBudgetExceededException e) {
            synchronized (spend) {
                spend.release(cost, windows);
            }
            throw e;
        }
    }

    private Reservation reserveTokens(LlmProviderConfig provider, long estimatedTokens, BigDecimal cost,
            Map<LlmSpendWindow, Instant> windows) {
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
            return new Reservation(provider.id(), estimatedTokens, entry.day, cost, windows);
        }
    }

    /** Replaces a reservation's estimate with the tokens actually used (zero when the call failed before use). */
    public void settle(Reservation reservation, long actualTokens) {
        settle(reservation, actualTokens, BigDecimal.ZERO);
    }

    /**
     * Replaces a reservation's estimates with the tokens actually used and their cost in USD (zero when
     * the call failed before use).
     */
    public void settle(Reservation reservation, long actualTokens, BigDecimal actualCostUsd) {
        Spend spend = spends.get(reservation.providerId());
        if (spend != null) {
            synchronized (spend) {
                spend.rollOver(clock.instant());
                spend.release(reservation.estimatedCostUsd(), reservation.windows());
                if (actualCostUsd != null && actualCostUsd.signum() > 0) {
                    spend.add(actualCostUsd);
                }
            }
        }
        settleTokens(reservation, actualTokens);
    }

    private void settleTokens(Reservation reservation, long actualTokens) {
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

    /**
     * {@code providerId}'s spend in each window: what was spent (seeded from {@code llm_usage}, then
     * every settled call), what calls in flight hold, and when the window resets.
     */
    public Map<LlmSpendWindow, WindowSpend> spend(String providerId) {
        Spend spend = spendOf(providerId);
        synchronized (spend) {
            spend.rollOver(clock.instant());
            Map<LlmSpendWindow, WindowSpend> out = new EnumMap<>(LlmSpendWindow.class);
            for (LlmSpendWindow window : LlmSpendWindow.values()) {
                Window w = spend.windows.get(window);
                out.put(window, new WindowSpend(window, w.start, window.end(w.start), w.spent, w.held));
            }
            return out;
        }
    }

    /** Discards a provider's state, so the next use rebuilds it from its configuration and the meter. */
    public void invalidate(String providerId) {
        entries.remove(providerId);
        spends.remove(providerId);
    }

    @Override
    public void resetState() {
        entries.clear();
        spends.clear();
    }

    private Spend spendOf(String providerId) {
        return spends.computeIfAbsent(providerId, id -> {
            Instant now = clock.instant();
            Spend fresh = new Spend();
            for (LlmSpendWindow window : LlmSpendWindow.values()) {
                Instant start = window.start(now);
                fresh.windows.put(window, new Window(start, usage.costSince(id, start)));
            }
            return fresh;
        });
    }

    /** {@code usd} to at most 4 decimal places, without trailing zeros. */
    private static String plain(BigDecimal usd) {
        return usd.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    /** Refuses when a capped window's spend plus what is in flight has reached its cap. */
    private void checkCaps(LlmProviderConfig provider, Spend spend) {
        LlmSpendCaps caps = provider.spendCaps();
        if (!caps.isSet()) {
            return;
        }
        Instant now = clock.instant();
        LlmSpendWindow refusedBy = null;
        Instant resetsAt = null;
        for (LlmSpendWindow window : LlmSpendWindow.values()) {
            BigDecimal cap = caps.of(window);
            Window w = spend.windows.get(window);
            if (cap != null && w.spent.add(w.held).compareTo(cap) >= 0) {
                Instant end = window.end(w.start);
                if (resetsAt == null || end.isAfter(resetsAt)) {
                    resetsAt = end;
                    refusedBy = window;
                }
            }
        }
        if (refusedBy == null) {
            return;
        }
        usage.recordRefused(provider.id());
        Window w = spend.windows.get(refusedBy);
        BigDecimal cap = caps.of(refusedBy);
        if (!w.notified) {
            w.notified = true;
            log.warn("LLM provider {} has reached its {} USD cap of {}", provider.id(), refusedBy.wireValue(),
                    cap.toPlainString());
            events.publishEvent(new SystemEvent("system.llm_spend_cap_reached", "warning",
                    "LLM spend cap reached: " + provider.id(),
                    "LLM provider `" + provider.id() + "` has spent its " + refusedBy.wireValue() + "ly cap of $"
                            + cap.toPlainString() + ". Calls are refused until " + resetsAt + ".",
                    "/v1/llm/providers/" + provider.id() + "/budget",
                    Map.of("providerId", provider.id(), "window", refusedBy.wireValue(), "capUsd",
                            cap.toPlainString())));
        }
        throw new LlmBudgetExceededException(provider.id(), "LLM provider '" + provider.id() + "' has spent $"
                + plain(w.spent) + " of its "
                + refusedBy.wireValue() + "ly cap of $" + cap.toPlainString() + "; it resets at " + resetsAt + ".",
                Duration.between(now, resetsAt));
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
     * @param estimatedCostUsd the USD held against the caps
     * @param windows the start of each spend window it was taken in
     */
    public record Reservation(String providerId, long estimatedTokens, LocalDate day, BigDecimal estimatedCostUsd,
            Map<LlmSpendWindow, Instant> windows) {

        /** Copies the windows; a null cost is zero. */
        public Reservation {
            estimatedCostUsd = estimatedCostUsd == null ? BigDecimal.ZERO : estimatedCostUsd;
            windows = windows == null ? Map.of() : Map.copyOf(windows);
        }

        /** A reservation of tokens only. */
        public Reservation(String providerId, long estimatedTokens, LocalDate day) {
            this(providerId, estimatedTokens, day, BigDecimal.ZERO, Map.of());
        }
    }

    /**
     * One window's spend, as {@link #spend} reports it.
     *
     * @param window the window
     * @param start when it began
     * @param resetsAt when it resets
     * @param spentUsd what was spent in it
     * @param reservedUsd the estimated cost of the calls in flight
     */
    public record WindowSpend(LlmSpendWindow window, Instant start, Instant resetsAt, BigDecimal spentUsd,
            BigDecimal reservedUsd) {}

    /** One window's running figures. */
    private static final class Window {
        Instant start;
        BigDecimal spent;
        BigDecimal held = BigDecimal.ZERO;
        boolean notified;

        Window(Instant start, BigDecimal spent) {
            this.start = start;
            this.spent = spent == null ? BigDecimal.ZERO : spent;
        }
    }

    /** One provider's USD spend in every window; guarded by its own monitor. */
    private static final class Spend {
        final Map<LlmSpendWindow, Window> windows = new EnumMap<>(LlmSpendWindow.class);

        /** Starts each window again from zero once its UTC boundary has passed. */
        void rollOver(Instant now) {
            for (LlmSpendWindow window : LlmSpendWindow.values()) {
                Window w = windows.get(window);
                Instant start = window.start(now);
                if (!w.start.equals(start)) {
                    w.start = start;
                    w.spent = BigDecimal.ZERO;
                    w.held = BigDecimal.ZERO;
                    w.notified = false;
                }
            }
        }

        /** Holds {@code cost} in every window; returns each window's start, to release it from. */
        Map<LlmSpendWindow, Instant> hold(BigDecimal cost) {
            Map<LlmSpendWindow, Instant> starts = new EnumMap<>(LlmSpendWindow.class);
            windows.forEach((window, w) -> {
                w.held = w.held.add(cost);
                starts.put(window, w.start);
            });
            return starts;
        }

        /** Releases a hold, from the windows that have not reset since it was taken. */
        void release(BigDecimal cost, Map<LlmSpendWindow, Instant> starts) {
            windows.forEach((window, w) -> {
                if (w.start.equals(starts.get(window))) {
                    w.held = w.held.subtract(cost).max(BigDecimal.ZERO);
                }
            });
        }

        /** Counts {@code cost} as spent in every current window. */
        void add(BigDecimal cost) {
            windows.values().forEach(w -> w.spent = w.spent.add(cost));
        }
    }

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
