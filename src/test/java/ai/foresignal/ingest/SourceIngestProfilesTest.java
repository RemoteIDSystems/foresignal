package ai.foresignal.ingest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.AppSettingChangedEvent;
import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceRegistryChangedEvent;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsChangedEvent;
import ai.foresignal.source.spi.IngestPolicy;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** The profile cache and its invalidation events. */
class SourceIngestProfilesTest {

    private static final String ID = "test-events";

    private SourceRegistry registry;
    private SourceService sources;
    private AppSettingService settings;
    private SourceIngestProfiles profiles;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        registry = mock(SourceRegistry.class);
        sources = mock(SourceService.class);
        settings = mock(AppSettingService.class);
        when(registry.descriptor(ID)).thenReturn(IngestFixtures.eventsDescriptor().build());
        when(sources.effectiveIngestPolicy(ID)).thenReturn(IngestPolicy.all(Duration.ofMinutes(4)));
        when(sources.effectiveRetention(ID)).thenReturn(Optional.empty());
        when(settings.getOrDefault(eq(SettingKeys.RETENTION), eq(RetentionSettings.class), any()))
                .thenReturn(new RetentionSettings(Duration.ofDays(20), null, null, Map.of(), null));
        profiles = new SourceIngestProfiles(registry, sources, settings);
    }

    @Test
    @DisplayName("builds from descriptor, effective policy and retention, then caches")
    void buildsAndCaches() {
        SourceIngestProfile profile = profiles.get(ID);

        assertThat(profile.keyframe()).isEqualTo(Duration.ofMinutes(4));
        assertThat(profile.window()).isEqualTo(Duration.ofDays(20));
        assertThat(profiles.get(ID)).isSameAs(profile);
        verify(registry, times(1)).descriptor(ID);
    }

    @Test
    @DisplayName("settings, registry and retention events evict; other settings do not")
    void eviction() {
        SourceIngestProfile first = profiles.get(ID);

        profiles.onAppSettingChanged(new AppSettingChangedEvent(SettingKeys.CORS));
        assertThat(profiles.get(ID)).isSameAs(first);

        profiles.onSourceSettingsChanged(new SourceSettingsChangedEvent(ID, null, null));
        SourceIngestProfile second = profiles.get(ID);
        assertThat(second).isNotSameAs(first);

        profiles.onSourceRegistryChanged(SourceRegistryChangedEvent.registered(ID));
        SourceIngestProfile third = profiles.get(ID);
        assertThat(third).isNotSameAs(second);

        profiles.onAppSettingChanged(new AppSettingChangedEvent(SettingKeys.RETENTION));
        SourceIngestProfile fourth = profiles.get(ID);
        assertThat(fourth).isNotSameAs(third);

        profiles.invalidateAll();
        assertThat(profiles.get(ID)).isNotSameAs(fourth);
    }
}
