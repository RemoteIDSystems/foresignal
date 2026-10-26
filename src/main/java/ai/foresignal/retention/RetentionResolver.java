package ai.foresignal.retention;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.Durations;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.TreeSet;
import org.springframework.stereotype.Component;

/**
 * Resolves retention windows (Core-Data-Model §13, Weather-Imagery-Design §2.4, prompt 019).
 *
 * <p>{@link #window(String, String)}: a TRACK source always uses {@code retention.trackWindow}. A
 * STANDARD source, in order:
 *
 * <ol>
 *   <li>{@code retention.kinds.{kind}};
 *   <li>the per-source override ({@code source_settings.retention}, shown as
 *       {@code retention.sources.{id}});
 *   <li>the descriptor's {@code kindRetention[kind]};
 *   <li>the descriptor's {@code defaultRetention};
 *   <li>{@code retention.default}.
 * </ol>
 *
 * <p>A source id that is no longer registered uses {@code retention.default}. Adapters set their
 * defaults by analytic class (Conventions "Analytic retention"); the operator can always override.
 *
 * <p><strong>Cap history (prompt R2-015).</strong> When {@code retention.capHistory} is on with {@code maxDays},
 * every window this class resolves (STANDARD and TRACK) is clamped to that many days
 * ({@link RetentionSettings#capped}). The cap only ever shortens a window; the cutoffs it produces still pass through
 * the consumer clamp of the run.
 *
 * <p>Also owns the asOf horizon math of Core-Data-Model §12, which prompt 014 computed inline:
 * {@link #effectiveKeyframe} and {@link #horizon(Instant, Duration, Duration)}.
 */
@Component
public class RetentionResolver {

    /** Margin added to the asOf horizon (Core-Data-Model §12). */
    public static final Duration HORIZON_MARGIN = Duration.ofHours(1);

    private final SourceRegistry registry;
    private final SourceSettingsRepository sourceSettings;
    private final AppSettingService settings;

    /** Creates the resolver. */
    public RetentionResolver(
            SourceRegistry registry, SourceSettingsRepository sourceSettings, AppSettingService settings) {
        this.registry = registry;
        this.sourceSettings = sourceSettings;
        this.settings = settings;
    }

    /** True when {@code sourceId} is a registered source. */
    public boolean isRegistered(String sourceId) {
        return registry.contains(sourceId);
    }

    /** The global retention settings (stored, else the shipped defaults). */
    public RetentionSettings settings() {
        return settings.getOrDefault(SettingKeys.RETENTION, RetentionSettings.class, RetentionSettings::defaults);
    }

    /**
     * The window of records of {@code kind} from {@code sourceId}.
     *
     * @param sourceId the source; an unregistered id resolves to {@code retention.default}
     * @param kind the kind, or null for the source-level window (steps 2, 4 and 5 only)
     */
    public Duration window(String sourceId, String kind) {
        RetentionSettings global = settings();
        Optional<SourceDescriptor> descriptor = registry.find(sourceId).map(r -> r.descriptor());
        if (descriptor.isEmpty()) {
            return global.capped(global.defaultRetention());
        }
        return resolve(descriptor.get(), kind, override(sourceId), global);
    }

    /**
     * The pure resolution order.
     *
     * @param descriptor the source's descriptor
     * @param kind the kind, or null for the source-level window
     * @param override the per-source override, if any
     * @param global the global settings
     */
    public static Duration resolve(
            SourceDescriptor descriptor, String kind, Optional<Duration> override, RetentionSettings global) {
        return global.capped(uncapped(descriptor, kind, override, global));
    }

    /** The resolution order before the cap-history clamp (prompt R2-015). */
    private static Duration uncapped(
            SourceDescriptor descriptor, String kind, Optional<Duration> override, RetentionSettings global) {
        if (descriptor.isTrack()) {
            return global.trackWindow();
        }
        if (kind != null && global.kinds().containsKey(kind)) {
            return global.kinds().get(kind);
        }
        if (override.isPresent()) {
            return override.get();
        }
        if (kind != null && descriptor.kindRetention().containsKey(kind)) {
            return descriptor.kindRetention().get(kind);
        }
        if (descriptor.defaultRetention() != null) {
            return descriptor.defaultRetention();
        }
        return global.defaultRetention();
    }

    /**
     * The window per declared kind of {@code sourceId}, ordered by kind. A TRACK source maps every
     * kind to {@code trackWindow}.
     */
    public Map<String, Duration> windowsByKind(String sourceId) {
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        RetentionSettings global = settings();
        Optional<Duration> override = override(sourceId);
        Map<String, Duration> out = new TreeMap<>();
        for (String kind : descriptor.kinds()) {
            out.put(kind, resolve(descriptor, kind, override, global));
        }
        return out;
    }

    /**
     * The shortest window that applies to any record of {@code sourceId}: over its declared kinds and
     * the source-level window. The ingest profile derives its retention keyframe from it, so every
     * kind keeps a version newer than a quarter of its own window (I6); asOf horizons use it too.
     */
    public Duration shortestWindow(String sourceId) {
        Optional<SourceDescriptor> found = registry.find(sourceId).map(r -> r.descriptor());
        RetentionSettings global = settings();
        if (found.isEmpty()) {
            return global.capped(global.defaultRetention());
        }
        SourceDescriptor descriptor = found.get();
        Optional<Duration> override = override(sourceId);
        Duration shortest = resolve(descriptor, null, override, global);
        for (String kind : descriptor.kinds()) {
            Duration window = resolve(descriptor, kind, override, global);
            if (window.compareTo(shortest) < 0) {
                shortest = window;
            }
        }
        return shortest;
    }

    /**
     * The source-level window of a STANDARD source from its own settings: the override, else the
     * descriptor's {@code defaultRetention}; empty when {@code retention.default} applies, and always
     * empty for TRACK (the tier fixes it). {@code SourceService.effectiveRetention} delegates here.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     */
    public Optional<Duration> sourceWindow(String sourceId) {
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        if (descriptor.isTrack()) {
            return Optional.empty();
        }
        Optional<Duration> override = override(sourceId);
        return override.isPresent() ? override : Optional.ofNullable(descriptor.defaultRetention());
    }

    /** The per-source override stored in {@code source_settings.retention}, if any (never for TRACK). */
    public Optional<Duration> override(String sourceId) {
        return sourceSettings.findById(sourceId)
                .map(SourceSettingsEntity::getRetention)
                .filter(value -> !value.isBlank())
                .map(Durations::parse);
    }

    /** Every stored per-source override of a registered STANDARD source, ordered by id. */
    public Map<String, Duration> overrides() {
        Map<String, Duration> out = new TreeMap<>();
        for (SourceSettingsEntity row : sourceSettings.findAll()) {
            String value = row.getRetention();
            if (value == null || value.isBlank()) {
                continue;
            }
            Optional<SourceDescriptor> descriptor = registry.find(row.getSourceId()).map(r -> r.descriptor());
            if (descriptor.isPresent() && !descriptor.get().isTrack()) {
                out.put(row.getSourceId(), Durations.parse(value));
            }
        }
        return out;
    }

    /**
     * The largest window in force anywhere: {@code trackWindow}, {@code retention.default}, every
     * per-kind window and every registered source's windows. {@code seq_clock} keeps minutes back to
     * this plus a day.
     */
    public Duration largestWindow() {
        RetentionSettings global = settings();
        Duration largest = max(global.effectiveTrackWindow(), global.capped(global.defaultRetention()));
        for (Duration window : global.kinds().values()) {
            largest = max(largest, global.capped(window));
        }
        for (SourceDescriptor descriptor : registry.descriptors()) {
            largest = max(largest, largestStandardWindow(descriptor, global));
        }
        return largest;
    }

    /** The windows of every registered STANDARD source keyed by source id and then kind (null kind = source level). */
    public Map<String, Map<String, Duration>> standardWindows() {
        return standardWindows(settings());
    }

    /**
     * {@link #standardWindows()} under {@code global} rather than the stored settings: a retention run resolves its
     * windows from the settings it read at its start, which the database-size cap may have tightened (prompt R2-015).
     *
     * @param global the settings of the run
     * @return the windows keyed by source id and then kind (null kind = source level)
     */
    public Map<String, Map<String, Duration>> standardWindows(RetentionSettings global) {
        Map<String, Duration> overrides = overrides();
        Map<String, Map<String, Duration>> out = new LinkedHashMap<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (descriptor.isTrack()) {
                continue;
            }
            Optional<Duration> override = Optional.ofNullable(overrides.get(descriptor.id()));
            Map<String, Duration> windows = new LinkedHashMap<>();
            windows.put(null, resolve(descriptor, null, override, global));
            for (String kind : new TreeSet<>(descriptor.kinds())) {
                windows.put(kind, resolve(descriptor, kind, override, global));
            }
            out.put(descriptor.id(), windows);
        }
        return out;
    }

    private Duration largestStandardWindow(SourceDescriptor descriptor, RetentionSettings global) {
        if (descriptor.isTrack()) {
            return global.effectiveTrackWindow();
        }
        Optional<Duration> override = override(descriptor.id());
        Duration largest = resolve(descriptor, null, override, global);
        for (String kind : descriptor.kinds()) {
            largest = max(largest, resolve(descriptor, kind, override, global));
        }
        return largest;
    }

    /** {@code min(policy keyframe, window/4)}; a missing keyframe means {@code window/4}. */
    public static Duration effectiveKeyframe(Duration keyframe, Duration window) {
        Duration quarter = window.dividedBy(4);
        return keyframe == null || keyframe.compareTo(quarter) > 0 ? quarter : keyframe;
    }

    /** {@code now − window + effectiveKeyframe + 1 h}: the earliest asOf a source supports (Core §12). */
    public static Instant horizon(Instant now, Duration window, Duration keyframe) {
        return now.minus(window).plus(effectiveKeyframe(keyframe, window)).plus(HORIZON_MARGIN);
    }

    private static Duration max(Duration a, Duration b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}
