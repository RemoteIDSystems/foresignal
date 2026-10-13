package ai.foresignal.source.adapters.rail;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.adapters.adsbfi.AdsbFiMilAdapter;
import ai.foresignal.source.adapters.adsbfi.AdsbFiRegionFamily;
import ai.foresignal.source.adapters.airplaneslive.AirplanesLiveMilAdapter;
import ai.foresignal.source.adapters.amtraker.AmtrakerAdapter;
import ai.foresignal.source.adapters.digitraffic.rail.DigitrafficRailAdapter;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
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
 * Every prompt 080 descriptor passes the registry rules ({@link DescriptorValidator}, including the TRACK rules) and
 * the §5.1 keyframe constraint of {@link IngestPolicyValidator} ({@code keyframe ≤ ttl − pollInterval − 30 s}), and
 * carries the prompt's tier, policy, TTL, interval, budget, pacing and egress.
 */
class RailAndAircraftDescriptorTest {

    static List<SourceAdapter> adapters() {
        List<SourceAdapter> all = new ArrayList<>();
        all.add(new AirplanesLiveMilAdapter(""));
        all.add(new AdsbFiMilAdapter(""));
        all.addAll(new AdsbFiRegionFamily("").adapters());
        all.add(new DigitrafficRailAdapter("", () -> "dev"));
        all.add(new AmtrakerAdapter(""));
        return all;
    }

    static Stream<SourceAdapter> each() {
        return adapters().stream();
    }

    @Test
    @DisplayName("the eight sources of prompt 080")
    void ids() {
        assertThat(adapters()).extracting(SourceAdapter::id).containsExactly("airplaneslive-mil", "adsbfi-mil",
                "adsbfi-region-1", "adsbfi-region-2", "adsbfi-region-3", "adsbfi-region-4", "digitraffic-rail",
                "amtraker");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("each")
    @DisplayName("passes the registry validator and the keyframe constraint; TRACK, OBSERVED, SAMPLED")
    void valid(SourceAdapter adapter) {
        SourceDescriptor d = adapter.descriptor();
        assertThat(DescriptorValidator.problems(d, adapter)).as(d.id()).isEmpty();
        assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "defaultIngestPolicy")).as(d.id())
                .isEmpty();
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.defaultIngestPolicy()).isInstanceOf(IngestPolicy.Sampled.class);
        IngestPolicy.Sampled policy = (IngestPolicy.Sampled) d.defaultIngestPolicy();
        assertThat(policy.keyframe()).as("%s keyframe", d.id())
                .isLessThanOrEqualTo(d.recordTtl().minus(d.defaultInterval()).minusSeconds(30));
        assertThat(d.defaultRetention()).isNull();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("each")
    @DisplayName("the aircraft sources: 6-minute TTL, 60 s/240 s, 30 s polls, 2/min and 2,880/day, 1.1 s pacing, LOCAL_ONLY")
    void aircraft(SourceAdapter adapter) {
        SourceDescriptor d = adapter.descriptor();
        if (!d.kinds().contains("aircraft")) {
            return;
        }
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(6));
        IngestPolicy.Sampled policy = (IngestPolicy.Sampled) d.defaultIngestPolicy();
        assertThat(policy.minInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(policy.keyframe()).isEqualTo(Duration.ofSeconds(240));
        assertThat(policy.minDeviationMeters()).isEqualTo(1000.0);
        assertThat(policy.edgeMinInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 2_880));
        assertThat(d.hostPacing()).isEqualTo(Duration.ofMillis(1100));
        assertThat(d.accessDeniedIsTerminal()).isTrue();
        assertThat(d.egress()).isEqualTo(Egress.LOCAL_ONLY);
        assertThat(d.titleField()).isEqualTo("flight");
        assertThat(d.userAgentOverride()).isNull();
    }

    @Test
    @DisplayName("digitraffic-rail: TTL 5 min, {15s, 240s, 150 m, edge 8s}, 30 s (min 15 s), 4/min and 4,000/day, CC BY 4.0")
    void digitraffic() {
        SourceDescriptor d = new DigitrafficRailAdapter("", () -> "dev").descriptor();
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(5));
        IngestPolicy.Sampled policy = (IngestPolicy.Sampled) d.defaultIngestPolicy();
        assertThat(policy.minInterval()).isEqualTo(Duration.ofSeconds(15));
        assertThat(policy.keyframe()).isEqualTo(Duration.ofSeconds(240));
        assertThat(policy.minDeviationMeters()).isEqualTo(150.0);
        assertThat(policy.edgeMinInterval()).isEqualTo(Duration.ofSeconds(8));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(15));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(4, 4_000));
        assertThat(d.significantFields()).containsExactly("trainNumber", "departureDate", "trainType",
                "operatorShortCode");
        assertThat(d.volatileFields()).containsExactly("accuracyM");
        assertThat(d.thresholds()).isEmpty();
        assertThat(d.motion()).isNull();
        assertThat(d.licence()).isEqualTo("CC BY 4.0");
        assertThat(d.attribution()).isEqualTo("Fintraffic / digitraffic.fi");
        assertThat(d.egress()).isEqualTo(Egress.OPEN);
        assertThat(d.hostPacing()).isNull();
    }

    @Test
    @DisplayName("amtraker: 60 s (min 60 s), keyframe 180 s, heading threshold 30°, motion in km/h, 2/min, 1,500/day, 250 MB")
    void amtraker() {
        SourceDescriptor d = new AmtrakerAdapter("").descriptor();
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(5));
        IngestPolicy.Sampled policy = (IngestPolicy.Sampled) d.defaultIngestPolicy();
        assertThat(policy.minInterval()).isEqualTo(Duration.ofSeconds(15));
        assertThat(policy.keyframe()).isEqualTo(Duration.ofSeconds(180));
        assertThat(policy.minDeviationMeters()).isEqualTo(150.0);
        assertThat(policy.edgeMinInterval()).isEqualTo(Duration.ofSeconds(8));
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(d.defaultBudget()).isEqualTo(Budget.of(2, 1_500, 250));
        assertThat(d.thresholds()).singleElement().satisfies(t -> {
            assertThat(t.field()).isEqualTo("headingDeg");
        });
        assertThat(d.motion().speedField()).isEqualTo("speedKph");
        assertThat(d.motion().courseField()).isEqualTo("headingDeg");
        assertThat(d.significantFields()).containsExactly("trainNumber", "route", "provider", "state",
                "destinationCode");
        assertThat(d.licence()).isEqualTo("ODC-By 1.0");
        assertThat(d.attribution()).contains("amtraker.com");
    }

    @Test
    @DisplayName("the keyframe ceilings ttl - interval - 30 s: 300 s for aircraft, 210 s for amtraker, 240 s for digitraffic")
    void ceilings() {
        assertThat(IngestPolicyValidator.keyframeCeiling(new AdsbFiMilAdapter("").descriptor()))
                .isEqualTo(Duration.ofSeconds(300));
        assertThat(IngestPolicyValidator.keyframeCeiling(new AmtrakerAdapter("").descriptor()))
                .isEqualTo(Duration.ofSeconds(210));
        assertThat(IngestPolicyValidator.keyframeCeiling(new DigitrafficRailAdapter("", () -> "dev").descriptor()))
                .isEqualTo(Duration.ofSeconds(240));
    }
}
