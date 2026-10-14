package ai.foresignal.notify.mesh;

import ai.foresignal.store.NotificationChannelEntity;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * One {@link AirtimeGovernor} per mesh channel (prompt 084), built from the channel's config and kept in memory. A
 * changed config keeps the window ({@link AirtimeGovernor#withLimits}); a restart starts with an empty window, which
 * the minimum spacing and the hourly cap still bound.
 *
 * <p>Counters, tagged {@code channel} with the channel id: {@code foresignal.channel.mesh.airtime_seconds} (estimated
 * airtime admitted), {@code foresignal.channel.mesh.deferred}, {@code foresignal.channel.mesh.folded} (alerts folded
 * into digest lines) and {@code foresignal.channel.mesh.dropped} (transmissions refused for good, and over-rate
 * inbound commands).
 */
@Component
public class AirtimeGovernors {

    /** The airtime counter. */
    public static final String AIRTIME = "foresignal.channel.mesh.airtime_seconds";
    /** The deferral counter. */
    public static final String DEFERRED = "foresignal.channel.mesh.deferred";
    /** The folded-alert counter. */
    public static final String FOLDED = "foresignal.channel.mesh.folded";
    /** The drop counter. */
    public static final String DROPPED = "foresignal.channel.mesh.dropped";

    /**
     * One channel's governor and radio settings.
     *
     * @param fingerprint the config members they were built from
     * @param governor the governor
     * @param radio the radio settings of the time-on-air estimate
     * @param network {@code meshtastic} or {@code meshcore}
     */
    public record Entry(String fingerprint, AirtimeGovernor governor, LoRaAirtime radio, String network) {}

    private final ObjectProvider<MeterRegistry> meters;
    private final Map<UUID, Entry> entries = new ConcurrentHashMap<>();

    /**
     * Creates the registry.
     *
     * @param meters the registry of the counters, resolved lazily
     */
    @Autowired
    public AirtimeGovernors(ObjectProvider<MeterRegistry> meters) {
        this.meters = meters;
    }

    /**
     * The governor of {@code channel}, built or rebuilt from its config.
     *
     * @param type the channel's mesh type
     * @throws IllegalArgumentException when the config does not validate
     */
    public Entry forChannel(NotificationChannelEntity channel, MeshChannelType type) {
        Map<String, Object> config = channel.getConfig() == null ? Map.of() : channel.getConfig();
        MeshChannels.Common common = type.common(config);
        String fingerprint = common.limits() + "|" + common.radio() + "|" + type.network();
        return entries.compute(channel.getId(), (id, old) -> {
            if (old != null && old.fingerprint().equals(fingerprint)) {
                return old;
            }
            AirtimeGovernor governor = old == null ? new AirtimeGovernor(common.limits())
                    : old.governor().withLimits(common.limits());
            return new Entry(fingerprint, governor, common.radio(), type.network());
        });
    }

    /** The governor of {@code channelId}, if one was built. */
    public Optional<Entry> find(UUID channelId) {
        return Optional.ofNullable(entries.get(channelId));
    }

    /** Forgets a deleted channel's governor. */
    public void forget(UUID channelId) {
        entries.remove(channelId);
    }

    /** Counts {@code seconds} of admitted airtime. */
    public void airtime(UUID channelId, double seconds) {
        counter(AIRTIME, channelId).ifPresent(c -> c.increment(seconds));
    }

    /** Counts a deferral. */
    public void deferred(UUID channelId) {
        counter(DEFERRED, channelId).ifPresent(Counter::increment);
    }

    /** Counts {@code count} alerts folded into a digest line. */
    public void folded(UUID channelId, int count) {
        if (count > 0) {
            counter(FOLDED, channelId).ifPresent(c -> c.increment(count));
        }
    }

    /** Counts a dropped transmission or an ignored over-rate command. */
    public void dropped(UUID channelId) {
        counter(DROPPED, channelId).ifPresent(Counter::increment);
    }

    private Optional<Counter> counter(String name, UUID channelId) {
        MeterRegistry registry = meters == null ? null : meters.getIfAvailable();
        if (registry == null) {
            return Optional.empty();
        }
        return Optional.of(Counter.builder(name).tag("channel", channelId.toString()).register(registry));
    }
}
