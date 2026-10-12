package ai.foresignal.stats;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.SourceDescriptor;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads and writes {@link StatsSettings} and decides which sources the rollup covers (prompt 038).
 *
 * <p>A source is rolled up when it is registered, its {@code statsEnabled} is true, and it is not
 * private (descriptor flag or the operator's tightening, prompt 039) unless {@code statsIncludePrivate}
 * is true. Sources that
 * are not registered (a deleted custom source) are never rolled up.
 */
@Service
public class StatsSettingsService {

    /** The shared private-source visibility (prompt 039); null only in hand-built unit tests. */
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private ai.foresignal.security.visibility.SourceVisibility visibility;


    private final AppSettingService settings;
    private final SourceRegistry registry;

    /** Creates the service. */
    public StatsSettingsService(AppSettingService settings, SourceRegistry registry) {
        this.settings = settings;
        this.registry = registry;
    }

    /** The settings in force. */
    public StatsSettings get() {
        return settings.getOrDefault(SettingKeys.STATS, StatsSettings.class, StatsSettings::defaults);
    }

    /** Stores the source's flags; a null flag leaves it unchanged. */
    @Transactional
    public StatsSettings update(String sourceId, Boolean statsEnabled, Boolean statsIncludePrivate) {
        registry.descriptor(sourceId);
        if (statsEnabled == null && statsIncludePrivate == null) {
            return get();
        }
        StatsSettings next = get().with(sourceId, statsEnabled, statsIncludePrivate);
        settings.put(SettingKeys.STATS, next);
        return next;
    }

    /** True when {@code descriptor}'s records are rolled up. */
    public boolean rolledUp(SourceDescriptor descriptor, StatsSettings value) {
        if (!value.enabled(descriptor.id())) {
            return false;
        }
        boolean isPrivate = visibility == null ? descriptor.privateSource() : visibility.isPrivate(descriptor.id());
        return !isPrivate || value.includePrivate(descriptor.id());
    }

    /** The registered sources the rollup covers, sorted. */
    public List<String> rolledUpSources() {
        StatsSettings value = get();
        Set<String> out = new TreeSet<>();
        for (SourceDescriptor descriptor : registry.descriptors()) {
            if (rolledUp(descriptor, value)) {
                out.add(descriptor.id());
            }
        }
        return List.copyOf(out);
    }
}
