package ai.foresignal.ingest;

import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Caches one {@link SourceIngestProfile} per source and rebuilds it when its inputs change
 * (Core-Data-Model §6.1, "Profiles").
 *
 * <p>Inputs: the registered descriptor, the effective ingest policy and retention from
 * {@link SourceService}, and the global {@link RetentionSettings}. A
 * {@link SourceSettingsChangedEvent} or {@link SourceRegistryChangedEvent} evicts that source; a
 * retention {@link AppSettingChangedEvent} evicts every source, since the global windows feed every
 * profile. Profiles are rebuilt lazily on the next {@link #get}.
 */
@Component
public class SourceIngestProfiles {

    private final SourceRegistry registry;
    private final SourceService sources;
    private final AppSettingService settings;
    private final Map<String, SourceIngestProfile> cache = new ConcurrentHashMap<>();

    /** Creates the cache. */
    public SourceIngestProfiles(SourceRegistry registry, SourceService sources, AppSettingService settings) {
        this.registry = registry;
        this.sources = sources;
        this.settings = settings;
    }

    /**
     * The profile of {@code sourceId}, built on first use.
     *
     * @throws ai.foresignal.api.error.NotFoundException when the source is not registered
     */
    public SourceIngestProfile get(String sourceId) {
        return cache.computeIfAbsent(sourceId, this::build);
    }

    /** Builds a profile from the current descriptor and settings, bypassing the cache. */
    public SourceIngestProfile build(String sourceId) {
        SourceDescriptor descriptor = registry.descriptor(sourceId);
        RetentionSettings retention =
                settings.getOrDefault(SettingKeys.RETENTION, RetentionSettings.class, RetentionSettings::defaults);
        return SourceIngestProfile.of(
                descriptor,
                sources.effectiveIngestPolicy(sourceId),
                SourceIngestProfile.window(descriptor, sources.effectiveRetention(sourceId), retention));
    }

    /** Drops every cached profile. */
    public void invalidateAll() {
        cache.clear();
    }

    /** A source's settings changed: its policy or retention may have. */
    @EventListener
    public void onSourceSettingsChanged(SourceSettingsChangedEvent event) {
        cache.remove(event.sourceId());
    }

    /** A dynamic source appeared or disappeared, or was redefined. */
    @EventListener
    public void onSourceRegistryChanged(SourceRegistryChangedEvent event) {
        cache.remove(event.sourceId());
    }

    /** The global retention settings changed: every window may have. */
    @EventListener
    public void onAppSettingChanged(AppSettingChangedEvent event) {
        if (SettingKeys.RETENTION.equals(event.key())) {
            cache.clear();
        }
    }
}
