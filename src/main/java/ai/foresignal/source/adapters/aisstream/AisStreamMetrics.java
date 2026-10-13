package ai.foresignal.source.adapters.aisstream;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * The aisstream meters (prompt 063).
 *
 * <ul>
 *   <li>{@value #MESSAGES}{@code {type}}: recognized AIS envelopes by {@code MessageType};
 *   <li>{@value #DROPPED}{@code {reason}}: messages dropped, with {@code reason} one of {@code untimed},
 *       {@code oversize}, {@code malformed}, {@code unknown_type} and {@code no_mmsi};
 *   <li>{@value #VESSELS}: vessels held in the merged state.
 * </ul>
 */
public final class AisStreamMetrics {

    /** Recognized envelopes by type. */
    public static final String MESSAGES = "foresignal.aisstream.messages";
    /** Dropped messages by reason. */
    public static final String DROPPED = "foresignal.aisstream.dropped";
    /** Vessels held. */
    public static final String VESSELS = "foresignal.aisstream.vessels";

    /** A recognized envelope without a parseable time. */
    public static final String UNTIMED = "untimed";
    /** A message above the size cap. */
    public static final String OVERSIZE = "oversize";
    /** Not a JSON object. */
    public static final String MALFORMED = "malformed";
    /** A {@code MessageType} outside aisstream's enum, or no body under it. */
    public static final String UNKNOWN_TYPE = "unknown_type";
    /** No MMSI in the metadata or the body. */
    public static final String NO_MMSI = "no_mmsi";

    private final MeterRegistry registry;
    private final Map<String, Counter> messages = new ConcurrentHashMap<>();
    private final Map<String, Counter> dropped = new ConcurrentHashMap<>();

    /**
     * Registers the meters.
     *
     * @param vessels the vessel count the gauge reads
     */
    public AisStreamMetrics(MeterRegistry registry, Supplier<Number> vessels) {
        this.registry = registry;
        Gauge.builder(VESSELS, vessels).description("Vessels held in the aisstream merged state").register(registry);
    }

    /** Counts one recognized envelope of {@code type}. */
    public void message(String type) {
        messages.computeIfAbsent(type, t -> Counter.builder(MESSAGES).tag("type", t)
                .description("Recognized aisstream envelopes").register(registry)).increment();
    }

    /** Counts one dropped message. */
    public void dropped(String reason) {
        dropped.computeIfAbsent(reason, r -> Counter.builder(DROPPED).tag("reason", r)
                .description("aisstream messages dropped").register(registry)).increment();
    }

    /** The count of dropped messages for {@code reason} (tests). */
    public double droppedCount(String reason) {
        Counter counter = dropped.get(reason);
        return counter == null ? 0 : counter.count();
    }

    /** The count of recognized envelopes of {@code type} (tests). */
    public double messageCount(String type) {
        Counter counter = messages.get(type);
        return counter == null ? 0 : counter.count();
    }
}
