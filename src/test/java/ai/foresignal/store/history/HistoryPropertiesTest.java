package ai.foresignal.store.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.source.spi.HistoryTier;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/** Defaults, binding and validation of {@link HistoryProperties}. */
class HistoryPropertiesTest {

    @Test
    void defaultsMatchCoreDataModel() {
        HistoryProperties defaults = HistoryProperties.defaults();

        assertThat(defaults.trackBlock()).isEqualTo(20_000_000L);
        assertThat(defaults.standardBlock()).isEqualTo(250_000_000L);
        assertThat(defaults.block(HistoryTier.TRACK)).isEqualTo(20_000_000L);
        assertThat(defaults.block(HistoryTier.STANDARD)).isEqualTo(250_000_000L);
        assertThat(defaults.maintenance().enabled()).isTrue();
        assertThat(defaults.maintenance().interval()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void bindsFromKebabCaseWithDefaultsForMissingKeys() {
        var source = new MapConfigurationPropertySource();
        source.put("foresignal.history.track-block", "500");
        source.put("foresignal.history.maintenance.enabled", "false");

        HistoryProperties bound = new Binder(source).bindOrCreate("foresignal.history", HistoryProperties.class);

        assertThat(bound.trackBlock()).isEqualTo(500);
        assertThat(bound.standardBlock()).isEqualTo(250_000_000L);
        assertThat(bound.maintenance().enabled()).isFalse();
        assertThat(bound.maintenance().interval()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void rejectsNonPositiveBlocks() {
        assertThatThrownBy(() -> new HistoryProperties(0, 1, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HistoryProperties(1, -1, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fillsMissingMaintenance() {
        assertThat(new HistoryProperties(1, 1, null).maintenance()).isEqualTo(HistoryProperties.Maintenance.defaults());
        assertThat(new HistoryProperties.Maintenance(true, null).interval()).isEqualTo(Duration.ofMinutes(10));
        assertThat(new HistoryProperties.Maintenance(true, Duration.ZERO).interval()).isEqualTo(Duration.ofMinutes(10));
    }

    @Test
    void leafContainsItsHalfOpenRange() {
        LeafInfo leaf = new LeafInfo("rv_t_100", HistoryTier.TRACK, 100, 200, false);

        assertThat(leaf.contains(100)).isTrue();
        assertThat(leaf.contains(199)).isTrue();
        assertThat(leaf.contains(200)).isFalse();
        assertThat(leaf.contains(99)).isFalse();
    }
}
