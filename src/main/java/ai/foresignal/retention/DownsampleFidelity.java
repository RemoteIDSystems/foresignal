package ai.foresignal.retention;

import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.history.HistoryFidelity;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * The {@link HistoryFidelity} of downsampled history (prompt 044), replacing the raw fallback of
 * prompt 037.
 *
 * <ul>
 *   <li>{@link #downsampled}: a TRACK seq range overlaps a DONE range of {@link DownsampleRanges}.
 *       STANDARD history is never downsampled.
 *   <li>{@link #validityWidening}: for a TRACK source, {@code max(ttl, interval) − ttl}, so the asOf
 *       resolver's {@code ttl + widening} is {@code max(ttl, effectiveDownsampleInterval)} wherever it
 *       applies the widening (only over a thinned range). Zero for STANDARD and for a source without a
 *       TTL (its versions never expire by TTL).
 *   <li>{@link #finalBefore}: while downsampling is enabled, TRACK history older than
 *       {@code now − trackWindow + 1 h} is never rewritten (it is dropped instead), and later history
 *       may still be; with downsampling disabled, or for STANDARD, history is final everywhere.
 * </ul>
 *
 * <p>Per-source interval overrides are cached and forgotten on {@link SourceSettingsChangedEvent}; the
 * global interval comes from the (cached) retention settings on every call.
 */
@Component
public class DownsampleFidelity implements HistoryFidelity, ai.foresignal.ingest.IngestStateReset {

    private static final Logger log = LoggerFactory.getLogger(DownsampleFidelity.class);

    private final DownsampleRanges ranges;
    private final RetentionResolver resolver;
    private final SourceRegistry registry;
    private final ObjectProvider<SourceService> sources;
    private final Clock clock;
    private final Map<String, Optional<Duration>> overrides = new ConcurrentHashMap<>();

    /**
     * Creates the hook.
     *
     * @param sources resolved lazily, because the read paths that depend on this hook are built before
     *     some of the source service's own dependencies
     */
    public DownsampleFidelity(DownsampleRanges ranges, RetentionResolver resolver, SourceRegistry registry,
            ObjectProvider<SourceService> sources, Clock clock) {
        this.ranges = ranges;
        this.resolver = resolver;
        this.registry = registry;
        this.sources = sources;
        this.clock = clock;
    }

    @Override
    public boolean downsampled(HistoryTier tier, long fromSeq, long toSeq) {
        return tier == HistoryTier.TRACK && ranges.overlaps(tier, fromSeq, toSeq);
    }

    @Override
    public Duration validityWidening(String sourceId) {
        Optional<SourceDescriptor> descriptor = registry.find(sourceId).map(r -> r.descriptor());
        if (descriptor.isEmpty() || !descriptor.get().isTrack() || descriptor.get().recordTtl() == null) {
            return Duration.ZERO;
        }
        Duration ttl = descriptor.get().recordTtl();
        Duration interval = interval(sourceId);
        return interval.compareTo(ttl) > 0 ? interval.minus(ttl) : Duration.ZERO;
    }

    @Override
    public Instant finalBefore(HistoryTier tier) {
        if (tier != HistoryTier.TRACK) {
            return Instant.MAX;
        }
        RetentionSettings settings = resolver.settings();
        if (!settings.downsamplingEnabled()) {
            return Instant.MAX;
        }
        return clock.instant().minus(settings.trackWindow()).plus(Duration.ofHours(1));
    }

    /** {@code SourceService.effectiveDownsampleInterval}, with the override cached. */
    public Duration interval(String sourceId) {
        Optional<Duration> override = overrides.computeIfAbsent(sourceId, this::loadOverride);
        return override.orElseGet(() -> resolver.settings().trackDownsampleInterval());
    }

    /** Forgets a source's cached override when its settings change. */
    @EventListener
    public void onSourceSettingsChanged(SourceSettingsChangedEvent event) {
        overrides.remove(event.sourceId());
    }

    /** Forgets every cached override (the test cleaner truncates {@code source_settings}). */
    @Override
    public void resetState() {
        overrides.clear();
    }

    private Optional<Duration> loadOverride(String sourceId) {
        SourceService service = sources.getIfAvailable();
        if (service == null) {
            return Optional.empty();
        }
        try {
            return service.downsampleIntervalOverride(sourceId);
        } catch (RuntimeException e) {
            log.debug("Could not read the downsample interval of {}; using the global one", sourceId, e);
            return Optional.empty();
        }
    }
}
