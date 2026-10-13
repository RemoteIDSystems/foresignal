package ai.foresignal.source.adapters;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.adsblol.AdsbLolAreaFamily;
import ai.foresignal.source.adapters.adsblol.AdsbLolMilAdapter;
import ai.foresignal.source.adapters.opensky.OpenSkyStatesAdapter;
import ai.foresignal.source.formats.readsb.AircraftDescriptors;
import ai.foresignal.source.spi.FieldThreshold;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * The five global aircraft sources declare the Core-Data-Model §5.3 SAMPLED TRACK defaults (as overridden by
 * {@code docs/Conventions.md}) and pass the registry's validation, the keyframe constraint
 * {@code keyframe ≤ ttl − pollInterval − 30 s} included, at their default intervals (prompt 062).
 */
class DescriptorDefaultsTest {

    static Stream<SourceAdapter> sources() {
        List<SourceAdapter> all = new ArrayList<>();
        all.add(new AdsbLolMilAdapter(""));
        all.addAll(new AdsbLolAreaFamily("").adapters());
        all.add(new OpenSkyStatesAdapter("", ""));
        return all.stream();
    }

    @Test
    @DisplayName("there are five: adsblol-mil, adsblol-area-1..3 and opensky-states")
    void five() {
        assertThat(sources().map(SourceAdapter::id)).containsExactly(
                "adsblol-mil", "adsblol-area-1", "adsblol-area-2", "adsblol-area-3", "opensky-states");
    }

    @ParameterizedTest
    @MethodSource("sources")
    @DisplayName("registry validation finds no problem")
    void valid(SourceAdapter adapter) {
        assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("sources")
    @DisplayName("TRACK, OBSERVED, UPSTREAM and the SAMPLED §5.3 policy, within the keyframe ceiling")
    void trackDefaults(SourceAdapter adapter) {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.defaultRetention()).isNull();
        assertThat(d.defaultIngestPolicy()).isEqualTo(new IngestPolicy.Sampled(
                Duration.ofSeconds(60), Duration.ofSeconds(240), 1000.0, true, Duration.ofSeconds(30)));
        Duration ceiling = IngestPolicyValidator.keyframeCeiling(d);
        assertThat(ceiling).isGreaterThanOrEqualTo(Duration.ofSeconds(240));
        assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "defaultIngestPolicy")).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("sources")
    @DisplayName("significant fields, thresholds, motion, tiles, search, title and entity keys")
    void fields(SourceAdapter adapter) {
        SourceDescriptor d = adapter.descriptor();
        assertThat(d.significantFields())
                .containsExactly("flight", "r", "t", "squawk", "category", "emergency", "onGround", "navDegraded");
        assertThat(d.thresholds()).containsExactly(FieldThreshold.of("alt_baro", 300.0), FieldThreshold.angular("track", 20.0));
        assertThat(d.motion()).isEqualTo(new MotionSpec("gs", MotionSpec.SpeedUnit.KNOTS, "track"));
        assertThat(d.tileProperties()).containsExactly("flight", "r", "t", "alt_baro", "gs", "track", "squawk", "mil");
        assertThat(d.searchFields()).containsExactly("flight", "r", "t", "hex");
        assertThat(d.titleField()).isEqualTo("flight");
        assertThat(d.entityKeys()).containsEntry("icao24", "hex").containsEntry("registration", "r")
                .containsEntry("callsign", "flight");
        assertThat(d.kinds()).containsExactly("aircraft");
        assertThat(d.aggBelowZoom()).isEqualTo(AircraftDescriptors.AGG_BELOW_ZOOM);
        assertThat(d.fieldSpecs().get("alt_baro").unit()).isEqualTo("m");
        assertThat(d.fieldSpecs().get("alt_baro").datum()).isEqualTo("pressure altitude (1013.25 hPa)");
        assertThat(d.fieldSpecs().get("alt_geom").datum()).isEqualTo("WGS84 ellipsoid");
        assertThat(d.fieldSpecs().get("gs").unit()).isEqualTo("[kn_i]");
        assertThat(d.fieldSpecs().get("track").unit()).isEqualTo("deg");
        assertThat(d.fieldSpecs().get("baro_rate").unit()).isEqualTo("m/s");
    }

    @Test
    @DisplayName("TTLs: 6 minutes for adsb.lol, 15 for OpenSky; the keyframe still holds at the slowest documented poll")
    void ttls() {
        sources().filter(a -> a.id().startsWith("adsblol-"))
                .forEach(a -> assertThat(a.descriptor().recordTtl()).isEqualTo(Duration.ofMinutes(6)));
        SourceDescriptor opensky = new OpenSkyStatesAdapter("", "").descriptor();
        assertThat(opensky.recordTtl()).isEqualTo(Duration.ofMinutes(15));
        // 15 min - 630 s - 30 s = 240 s: the 240 s keyframe holds up to a 630 s interval.
        assertThat(Duration.ofMinutes(15).minusSeconds(630).minusSeconds(30)).isEqualTo(Duration.ofSeconds(240));
        // 6 min - 90 s - 30 s = 240 s: the adsb.lol sources hold up to a 90 s interval.
        assertThat(Duration.ofMinutes(6).minusSeconds(90).minusSeconds(30)).isEqualTo(Duration.ofSeconds(240));
    }
}
