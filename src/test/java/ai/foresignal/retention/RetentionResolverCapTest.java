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
import ai.foresignal.store.SourceSettingsRepository;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** {@link RetentionResolver} under cap history (prompt R2-015): the cap tightens every window and never loosens one. */
class RetentionResolverCapTest {

    static final Duration D = Duration.ofDays(1);

    static SourceDescriptor.Builder base(String id) {
        return SourceDescriptor.builder(id).name(id).kinds("quake", "felt").mode(Mode.PUSH)
                .defaultBudget(Budget.of(60, 1000));
    }

    final SourceDescriptor longLived = base("k").defaultRetention(D.multipliedBy(365))
            .kindRetention("felt", D.multipliedBy(2)).build();
    final SourceDescriptor track = base("t").historyTier(HistoryTier.TRACK).build();

    static RetentionSettings settings(Integer maxDays, boolean on) {
        RetentionSettings plain = new RetentionSettings(D.multipliedBy(400), null, null, null, D.multipliedBy(30), null,
                null, null, null, Map.of("quake", D.multipliedBy(500)));
        return new RetentionSettings(plain.defaultRetention(), plain.alerts(), plain.usage(), plain.sources(),
                plain.trackWindow(), plain.provenance(), plain.notifications(), plain.briefings(), plain.ask(),
                plain.kinds(), plain.audit(), plain.inbox(), plain.stats(), plain.trackRawWindow(),
                plain.trackDownsampleInterval(), plain.entityLinks(), on, maxDays, null, null);
    }

    RetentionResolver resolver(RetentionSettings global) {
        SourceRegistry registry = mock(SourceRegistry.class);
        SourceSettingsRepository rows = mock(SourceSettingsRepository.class);
        AppSettingService store = mock(AppSettingService.class);
        when(store.getOrDefault(eq(SettingKeys.RETENTION), eq(RetentionSettings.class), any())).thenReturn(global);
        when(registry.find("k")).thenReturn(Optional.of(new SourceRegistry.Registration(longLived, null, false)));
        when(registry.find("t")).thenReturn(Optional.of(new SourceRegistry.Registration(track, null, false)));
        when(registry.find("gone")).thenReturn(Optional.empty());
        when(registry.descriptor("k")).thenReturn(longLived);
        when(registry.descriptor("t")).thenReturn(track);
        when(registry.descriptors()).thenReturn(List.of(longLived, track));
        when(rows.findById(any())).thenReturn(Optional.empty());
        when(rows.findAll()).thenReturn(List.of());
        return new RetentionResolver(registry, rows, store);
    }

    @Test
    @DisplayName("off: every window resolves exactly as before")
    void off() {
        RetentionResolver r = resolver(settings(10, false));
        assertThat(r.window("k", "quake")).isEqualTo(D.multipliedBy(500));
        assertThat(r.window("k", null)).isEqualTo(D.multipliedBy(365));
        assertThat(r.window("k", "felt")).isEqualTo(D.multipliedBy(2));
        assertThat(r.window("t", "quake")).isEqualTo(D.multipliedBy(30));
        assertThat(r.window("gone", null)).isEqualTo(D.multipliedBy(400));
        assertThat(r.largestWindow()).isEqualTo(D.multipliedBy(500));
    }

    @Test
    @DisplayName("on: every longer window (kind, descriptor, track, default) is clamped to maxDays")
    void tightens() {
        RetentionResolver r = resolver(settings(10, true));
        assertThat(r.window("k", "quake")).as("a per-kind setting").isEqualTo(D.multipliedBy(10));
        assertThat(r.window("k", null)).as("the descriptor default").isEqualTo(D.multipliedBy(10));
        assertThat(r.window("t", "quake")).as("the TRACK window").isEqualTo(D.multipliedBy(10));
        assertThat(r.window("gone", null)).as("retention.default").isEqualTo(D.multipliedBy(10));
        assertThat(r.largestWindow()).isEqualTo(D.multipliedBy(10));
        assertThat(r.standardWindows().get("k").values()).allSatisfy(w -> assertThat(w).isLessThanOrEqualTo(
                D.multipliedBy(10)));
    }

    @Test
    @DisplayName("on: a window already shorter than the cap is never lengthened")
    void neverLoosens() {
        RetentionResolver r = resolver(settings(10, true));
        assertThat(r.window("k", "felt")).isEqualTo(D.multipliedBy(2));
        assertThat(r.shortestWindow("k")).isEqualTo(D.multipliedBy(2));
        RetentionResolver wide = resolver(settings(1000, true));
        assertThat(wide.window("k", "quake")).isEqualTo(D.multipliedBy(500));
        assertThat(wide.window("t", null)).isEqualTo(D.multipliedBy(30));
    }

    @Test
    @DisplayName("the floor: a 1-day cap is the shortest, and it is still above the 1h minimum window")
    void floor() {
        RetentionResolver r = resolver(settings(1, true));
        assertThat(r.window("k", "quake")).isEqualTo(D);
        assertThat(r.window("k", "felt")).isEqualTo(D);
        assertThat(D).isGreaterThan(RetentionSettingsService.MIN_WINDOW);
    }

    @Test
    @DisplayName("a run's own settings drive the plan windows, so a tightened run is tighter than the stored ones")
    void runSettings() {
        RetentionResolver r = resolver(settings(null, false));
        Map<String, Map<String, Duration>> stored = r.standardWindows();
        Map<String, Map<String, Duration>> tightened = r.standardWindows(settings(null, false).withMaxDays(3));
        assertThat(stored.get("k").get("quake")).isEqualTo(D.multipliedBy(500));
        assertThat(tightened.get("k").get("quake")).isEqualTo(D.multipliedBy(3));
        assertThat(tightened.get("k").get("felt")).isEqualTo(D.multipliedBy(2));
    }

    @Test
    @DisplayName("the chronicle bound: forever and longer retentions take the cap; shorter ones stay")
    void chronicles() {
        assertThat(ai.foresignal.chronicle.ChronicleRetentionJob.keep("forever", D.multipliedBy(30)))
                .isEqualTo(D.multipliedBy(30));
        assertThat(ai.foresignal.chronicle.ChronicleRetentionJob.keep("P90D", D.multipliedBy(30)))
                .isEqualTo(D.multipliedBy(30));
        assertThat(ai.foresignal.chronicle.ChronicleRetentionJob.keep("P7D", D.multipliedBy(30)))
                .isEqualTo(D.multipliedBy(7));
        assertThat(ai.foresignal.chronicle.ChronicleRetentionJob.keep("forever", null)).isNull();
    }
}
