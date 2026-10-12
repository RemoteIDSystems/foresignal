package ai.foresignal.reference;

import ai.foresignal.api.error.ValidationException;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.SourceDescriptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The per-source region tagging mode ({@code SourceSettings.regionTagging}, prompt 040): the operator's
 * choice in {@code app_setting['regionTagging']}, else {@code eager} for STANDARD and {@code lazy} for
 * TRACK sources.
 */
@Service
public class RegionTaggingService {

    private final AppSettingService settings;
    private final SourceRegistry registry;

    /** Creates the service. */
    public RegionTaggingService(AppSettingService settings, SourceRegistry registry) {
        this.settings = settings;
        this.registry = registry;
    }

    /** The settings in force. */
    public RegionTaggingSettings get() {
        try {
            RegionTaggingSettings value = settings.getOrDefault(SettingKeys.REGION_TAGGING, RegionTaggingSettings.class,
                    RegionTaggingSettings::defaults);
            return value == null ? RegionTaggingSettings.defaults() : value;
        } catch (RuntimeException e) {
            return RegionTaggingSettings.defaults();
        }
    }

    /** The mode of {@code sourceId}; {@link RegionTagMode#OFF} for an unregistered source. */
    public RegionTagMode mode(String sourceId) {
        RegionTagMode override = get().override(sourceId);
        if (override != null) {
            return override;
        }
        return registry.find(sourceId)
                .map(r -> RegionTagMode.defaultFor(r.descriptor().historyTier()))
                .orElse(RegionTagMode.OFF);
    }

    /** The mode of {@code descriptor}'s source. */
    public RegionTagMode mode(SourceDescriptor descriptor) {
        RegionTagMode override = get().override(descriptor.id());
        return override != null ? override : RegionTagMode.defaultFor(descriptor.historyTier());
    }

    /** True when {@code sourceId} is a TRACK source (its retag distance is the TRACK one). */
    public boolean track(String sourceId) {
        return registry.find(sourceId).map(r -> r.descriptor().historyTier() == HistoryTier.TRACK).orElse(false);
    }

    /**
     * Stores {@code sourceId}'s mode; null leaves it unchanged.
     *
     * @throws ValidationException for a value other than {@code eager}, {@code lazy} or {@code off}
     */
    @Transactional
    public void update(String sourceId, String mode) {
        registry.descriptor(sourceId);
        if (mode == null) {
            return;
        }
        if (!RegionTagMode.WIRE_VALUES.contains(mode.strip().toLowerCase(java.util.Locale.ROOT))) {
            throw ValidationException.forField("regionTagging", "must be one of " + RegionTagMode.WIRE_VALUES);
        }
        settings.put(SettingKeys.REGION_TAGGING, get().with(sourceId, RegionTagMode.fromWire(mode)));
    }
}
