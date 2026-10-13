package ai.foresignal.chatops;

import ai.foresignal.collect.ClockTimeMeter;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * The per-chat limits of interactive channels (prompt 061), Bucket4j buckets driven by the application
 * clock: {@code commandRateLimit} commands per 10 minutes and {@code askRateLimitPerHour} questions per hour.
 * A changed limit replaces the chat's bucket.
 */
@Component
public class ChatRateLimiter {

    /** The window of the command limit. */
    public static final Duration COMMAND_WINDOW = Duration.ofMinutes(10);

    /** The window of the Ask limit. */
    public static final Duration ASK_WINDOW = Duration.ofHours(1);

    private record Entry(int capacity, Duration period, Bucket bucket) {}

    private final Map<String, Entry> buckets = new ConcurrentHashMap<>();
    private final ClockTimeMeter time;

    /** Creates the limiter. */
    public ChatRateLimiter(Clock clock) {
        this.time = new ClockTimeMeter(clock);
    }

    /** Takes one command for {@code chat}; returns {@link Duration#ZERO}, or how long until one refills. */
    public Duration command(ChatRef chat, int perTenMinutes) {
        return take("cmd|" + chat.channelId() + "|" + chat.chatId(), Math.max(1, perTenMinutes), COMMAND_WINDOW);
    }

    /**
     * Takes one question for {@code chat}; returns {@link Duration#ZERO}, or how long until one refills. A limit
     * of 0 refuses every question for an hour.
     */
    public Duration ask(ChatRef chat, int perHour) {
        if (perHour <= 0) {
            return ASK_WINDOW;
        }
        return take("ask|" + chat.channelId() + "|" + chat.chatId(), perHour, ASK_WINDOW);
    }

    /** Forgets every bucket (tests). */
    public void reset() {
        buckets.clear();
    }

    private Duration take(String key, int capacity, Duration period) {
        Entry entry = buckets.compute(key, (k, current) -> current != null && current.capacity() == capacity
                && current.period().equals(period) ? current : new Entry(capacity, period, bucket(capacity, period)));
        ConsumptionProbe probe = entry.bucket().tryConsumeAndReturnRemaining(1);
        return probe.isConsumed() ? Duration.ZERO : Duration.ofNanos(Math.max(1, probe.getNanosToWaitForRefill()));
    }

    private Bucket bucket(int capacity, Duration period) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder().capacity(capacity).refillGreedy(capacity, period).build())
                .withCustomTimePrecision(time)
                .build();
    }
}
