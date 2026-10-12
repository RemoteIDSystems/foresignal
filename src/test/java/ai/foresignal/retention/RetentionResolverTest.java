package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.AppSettingService;
import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.store.SourceSettingsEntity;
import ai.foresignal.store.SourceSettingsRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** The five-step resolution order, the TRACK rule and the horizon math (prompt 019). */
class RetentionResolverTest {

    static final Duration D7 = Duration.ofDays(7);

    static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id).name(id).kinds("quake", "felt").mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 1000));
    }

    static RetentionSettings global(Duration defaultWindow, Map<String, Duration> kinds) {
        return new RetentionSettings(defaultWindow, null, null, null, Duration.ofDays(3), null, null, null, null, kinds);
    }

    @Nested
    @DisplayName("resolve")
    class Resolve {

        final SourceDescriptor full = base("s")
                .defaultRetention(Duration.ofDays(30))
                .kindRetention("felt", Duration.ofDays(90))
                .build();

        @Test
        @DisplayName("1: retention.kinds wins over everything")
        void kindsFirst() {
            RetentionSettings g = global(D7, Map.of("felt", Duration.ofDays(400)));
            assertThat(RetentionResolver.resolve(full, "felt", Optional.of(Duration.ofDays(2)), g))
                    .isEqualTo(Duration.ofDays(400));
        }

        @Test
        @DisplayName("2: the per-source override comes next")
        void overrideSecond() {
            RetentionSettings g = global(D7, Map.of());
            assertThat(RetentionResolver.resolve(full, "felt", Optional.of(Duration.ofDays(2)), g))
                    .isEqualTo(Duration.ofDays(2));
        }

        @Test
        @DisplayName("3: then the descriptor's kindRetention")
        void kindRetentionThird() {
            assertThat(RetentionResolver.resolve(full, "felt", Optional.empty(), global(D7, Map.of())))
                    .isEqualTo(Duration.ofDays(90));
        }

        @Test
        @DisplayName("4: then the descriptor's defaultRetention")
        void defaultRetentionFourth() {
            assertThat(RetentionResolver.resolve(full, "quake", Optional.empty(), global(D7, Map.of())))
                    .isEqualTo(Duration.ofDays(30));
        }

        @Test
        @DisplayName("5: then retention.default")
        void globalDefaultLast() {
            SourceDescriptor bare = base("b").build();
            assertThat(RetentionResolver.resolve(bare, "quake", Optional.empty(), global(Duration.ofDays(11), Map.of())))
                    .isEqualTo(Duration.ofDays(11));
        }

        @Test
        @DisplayName("a null kind skips the kind steps")
        void sourceLevel() {
            RetentionSettings g = global(D7, Map.of("felt", Duration.ofDays(400)));
            assertThat(RetentionResolver.resolve(full, null, Optional.empty(), g)).isEqualTo(Duration.ofDays(30));
        }

        @Test
        @DisplayName("TRACK always uses trackWindow")
        void trackFixed() {
            SourceDescriptor track = base("t").historyTier(HistoryTier.TRACK).build();
            RetentionSettings g = global(D7, Map.of("quake", Duration.ofDays(400)));
            assertThat(RetentionResolver.resolve(track, "quake", Optional.of(Duration.ofDays(2)), g))
                    .isEqualTo(Duration.ofDays(3));
            assertThat(RetentionResolver.resolve(track, null, Optional.empty(), g)).isEqualTo(Duration.ofDays(3));
        }
    }

    @Nested
    @DisplayName("horizon")
    class Horizon {

        final Instant now = Instant.parse("2026-06-01T00:00:00Z");

        @Test
        @DisplayName("effectiveKeyframe = min(keyframe, window/4); none means window/4")
        void effectiveKeyframe() {
            assertThat(RetentionResolver.effectiveKeyframe(Duration.ofMinutes(4), D7)).isEqualTo(Duration.ofMinutes(4));
            assertThat(RetentionResolver.effectiveKeyframe(Duration.ofDays(3), D7)).isEqualTo(D7.dividedBy(4));
            assertThat(RetentionResolver.effectiveKeyframe(null, D7)).isEqualTo(D7.dividedBy(4));
        }

        @Test
        @DisplayName("horizon = now − window + effectiveKeyframe + 1 h")
        void horizonMath() {
            assertThat(RetentionResolver.horizon(now, D7, Duration.ofMinutes(4)))
                    .isEqualTo(now.minus(D7).plus(Duration.ofMinutes(4)).plus(Duration.ofHours(1)));
            assertThat(RetentionResolver.horizon(now, D7, null))
                    .isEqualTo(now.minus(D7).plus(D7.dividedBy(4)).plus(Duration.ofHours(1)));
        }
    }

    @Nested
    @DisplayName("with a registry")
    class WithRegistry {

        final SourceRegistry registry = mock(SourceRegistry.class);
        final SourceSettingsRepository rows = mock(SourceSettingsRepository.class);
        final AppSettingService settings = mock(AppSettingService.class);
        final RetentionResolver resolver = new RetentionResolver(registry, rows, settings);

        final SourceDescriptor kinds = base("k").defaultRetention(Duration.ofDays(30))
                .kindRetention("felt", Duration.ofDays(2)).build();
        final SourceDescriptor track = base("t").historyTier(HistoryTier.TRACK).build();

        WithRegistry() {
            when(settings.getOrDefault(eq(SettingKeys.RETENTION), eq(RetentionSettings.class), any()))
                    .thenReturn(global(D7, Map.of()));
            when(registry.find("k")).thenReturn(Optional.of(new SourceRegistry.Registration(kinds, null, false)));
            when(registry.find("t")).thenReturn(Optional.of(new SourceRegistry.Registration(track, null, false)));
            when(registry.find("gone")).thenReturn(Optional.empty());
            when(registry.descriptor("k")).thenReturn(kinds);
            when(registry.descriptor("t")).thenReturn(track);
            when(registry.descriptors()).thenReturn(List.of(kinds, track));
            when(rows.findById("k")).thenReturn(Optional.empty());
            when(rows.findById("t")).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("an unregistered source uses retention.default")
        void unregistered() {
            assertThat(resolver.window("gone", "quake")).isEqualTo(D7);
            assertThat(resolver.shortestWindow("gone")).isEqualTo(D7);
        }

        @Test
        @DisplayName("shortestWindow is the minimum over the kinds and the source level")
        void shortest() {
            assertThat(resolver.shortestWindow("k")).isEqualTo(Duration.ofDays(2));
            assertThat(resolver.shortestWindow("t")).isEqualTo(Duration.ofDays(3));
        }

        @Test
        @DisplayName("the override is read from source_settings, and sourceWindow falls back to the descriptor")
        void overrideFromSourceSettings() {
            SourceSettingsEntity row = new SourceSettingsEntity("k");
            row.setRetention("45d");
            when(rows.findById("k")).thenReturn(Optional.of(row));

            assertThat(resolver.override("k")).contains(Duration.ofDays(45));
            assertThat(resolver.sourceWindow("k")).contains(Duration.ofDays(45));
            assertThat(resolver.window("k", "quake")).isEqualTo(Duration.ofDays(45));
            assertThat(resolver.sourceWindow("t")).isEmpty();
        }

        @Test
        @DisplayName("windowsByKind and standardWindows list every declared kind; TRACK is left out of the latter")
        void windowMaps() {
            assertThat(resolver.windowsByKind("k"))
                    .containsEntry("felt", Duration.ofDays(2))
                    .containsEntry("quake", Duration.ofDays(30));
            Map<String, Map<String, Duration>> standard = resolver.standardWindows();
            assertThat(standard).containsOnlyKeys("k");
            assertThat(standard.get("k")).containsEntry(null, Duration.ofDays(30)).containsEntry("felt", Duration.ofDays(2));
        }

        @Test
        @DisplayName("largestWindow covers the default, the TRACK window and every source window")
        void largest() {
            assertThat(resolver.largestWindow()).isEqualTo(Duration.ofDays(30));
        }
    }
}
