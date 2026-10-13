package ai.foresignal.source.adapters.transit;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.DescriptorValidator;
import ai.foresignal.source.spi.Egress;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.IngestPolicyValidator;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.MotionSpec;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.testsupport.net.TestOutbound;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * {@link TransitFamily} over the reviewed list (prompt 070): one vehicle source per reviewed feed, an alerts source
 * only where an alerts URL exists, the Entur header carrying the install id, valid descriptors (the keyframe
 * constraint included) and no URL that does not come from the list.
 */
class TransitFamilyTest {

    private static TransitFamily family() {
        return new TransitFamily("", "", new MobilityCatalog(MobilityCatalogTest.TEST_CATALOG),
                TestOutbound.offlinePolicy(), () -> "abcd1234", null);
    }

    private static Map<String, SourceAdapter> byId(TransitFamily family) {
        return family.adapters().stream().collect(Collectors.toMap(SourceAdapter::id, Function.identity()));
    }

    @Test
    @DisplayName("one transit-{id} per reviewed feed, transit-alerts-{id} only where an alerts URL exists")
    void sources() {
        Map<String, SourceAdapter> adapters = byId(family());

        for (TransitFeed feed : TransitFeeds.REVIEWED) {
            assertThat(adapters).containsKey("transit-" + feed.id());
            assertThat(adapters.containsKey("transit-alerts-" + feed.id())).as(feed.id()).isEqualTo(feed.alertsUrl() != null);
        }
        assertThat(adapters).hasSize(13).doesNotContainKey("transit-alerts-capmetro-austin");
        assertThat(family().enabledCatalogFeeds()).isEmpty();
    }

    @Test
    @DisplayName("the reviewed list is valid: kebab ids, https URLs, only ET-Client-Name, known modes")
    void reviewedList() {
        assertThat(TransitFeeds.problems(TransitFeeds.REVIEWED)).isEmpty();
        TransitFeed bad = new TransitFeed("Bad_Id", "x", "x", "x", URI.create("http://example.org/vp"), null,
                Map.of("User-Agent", "spoof"), "x", null, "x", TimeBase.HEADER, "hovercraft", null, Egress.OPEN, false);
        assertThat(TransitFeeds.problems(List.of(bad, bad))).hasSize(5);
        TransitFeed none = new TransitFeed("none", "x", "x", "x", null, null, Map.of(), "x", null, "x", TimeBase.HEADER,
                null, null, null, false);
        assertThat(TransitFeeds.problems(List.of(none))).containsExactly(
                "none: the feed has neither a VehiclePositions nor an alerts URL");
        assertThat(TransitFeeds.reviewed("nope")).isNull();
    }

    @Test
    @DisplayName("Entur's ET-Client-Name carries the install id; other feeds send no extra header")
    void enturHeader() {
        Map<String, SourceAdapter> adapters = byId(family());

        assertThat(((TransitVehicleAdapter) adapters.get("transit-entur-norway")).requestHeaders())
                .containsExactly(Map.entry("ET-Client-Name", "foresignal-abcd1234"));
        assertThat(((TransitAlertsAdapter) adapters.get("transit-alerts-entur-norway")).requestHeaders())
                .containsExactly(Map.entry("ET-Client-Name", "foresignal-abcd1234"));
        assertThat(((TransitVehicleAdapter) adapters.get("transit-mbta")).requestHeaders()).isEmpty();
        assertThat(TransitFeeds.reviewed("entur-norway").resolvedHeaders(null))
                .containsEntry("ET-Client-Name", "foresignal-unknown");
    }

    @Test
    @DisplayName("every descriptor is valid, including the keyframe constraint 240 <= 300 - 30 - 30")
    void descriptorsValid() {
        for (SourceAdapter adapter : family().adapters()) {
            assertThat(DescriptorValidator.problems(adapter.descriptor(), adapter)).as(adapter.id()).isEmpty();
        }
    }

    @Test
    @DisplayName("vehicles: POLLING 30 s (min 15 s), 4/min and 3000/day, TRACK + SAMPLED, TTL 5 min, OBSERVED")
    void vehicleDescriptor() {
        SourceDescriptor d = byId(family()).get("transit-mbta").descriptor();

        assertThat(d.mode()).isEqualTo(Mode.POLLING);
        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(15));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(4);
        assertThat(d.defaultBudget().perDay()).isEqualTo(3000);
        assertThat(d.historyTier()).isEqualTo(HistoryTier.TRACK);
        assertThat(d.ttlAnchor()).isEqualTo(TtlAnchor.OBSERVED);
        assertThat(d.observedAtMode()).isEqualTo(ObservedAtMode.UPSTREAM);
        assertThat(d.recordTtl()).isEqualTo(Duration.ofMinutes(5));
        assertThat(d.defaultIngestPolicy()).isEqualTo(new IngestPolicy.Sampled(Duration.ofSeconds(15),
                Duration.ofSeconds(240), 150.0, true, Duration.ofSeconds(8)));
        assertThat(d.significantFields()).containsExactly("routeId", "tripId", "directionId", "label", "mode")
                .doesNotContain("currentStatus", "stopId");
        assertThat(d.motion()).isEqualTo(new MotionSpec("speedMs", MotionSpec.SpeedUnit.METERS_PER_SECOND, "bearing"));
        assertThat(d.thresholds()).singleElement().satisfies(t -> {
            assertThat(t.field()).isEqualTo("bearing");
            assertThat(t.delta()).isEqualTo(30.0);
            assertThat(t.angular()).isTrue();
        });
        assertThat(d.titleField()).isEqualTo("label");
        assertThat(d.tileProperties()).containsExactly("routeId", "mode", "bearing", "occupancyStatus");
        assertThat(d.licence()).isEqualTo("MassDOT Developers License Agreement");
        assertThat(d.attribution()).isEqualTo("MBTA / MassDOT");
        assertThat(d.egress()).isEqualTo(Egress.OPEN);
        assertThat(d.maxResponseBytes()).isEqualTo(16 * 1024 * 1024);
        assertThat(d.defaultRetention()).isNull();
    }

    @Test
    @DisplayName("keyframe <= ttl - interval - 30 s: 240 s fits a 30 s interval, a 45 s interval needs 225 s")
    void keyframeConstraint() {
        SourceDescriptor d = byId(family()).get("transit-mbta").descriptor();
        assertThat(IngestPolicyValidator.problems(d.defaultIngestPolicy(), d, "policy")).isEmpty();

        SourceDescriptor slower = SourceDescriptor.builder("transit-slower").kind(GtfsRtVehicleParser.KIND)
                .mode(Mode.POLLING).interval(Duration.ofSeconds(15), Duration.ofSeconds(45)).defaultBudget(4, 3000)
                .historyTier(HistoryTier.TRACK).ttlAnchor(TtlAnchor.OBSERVED).recordTtl(TransitDescriptors.VEHICLE_TTL)
                .significantFields(TransitDescriptors.VEHICLE_SIGNIFICANT.toArray(String[]::new))
                .defaultIngestPolicy(TransitDescriptors.vehiclePolicy()).build();
        assertThat(IngestPolicyValidator.problems(TransitDescriptors.vehiclePolicy(), slower, "policy")).isNotEmpty();
        IngestPolicy shorter = new IngestPolicy.Sampled(Duration.ofSeconds(15), Duration.ofSeconds(225), 150.0, true,
                Duration.ofSeconds(8));
        assertThat(IngestPolicyValidator.problems(shorter, slower, "policy")).isEmpty();
    }

    @Test
    @DisplayName("alerts: POLLING 60 s (min 30 s), 2/min and 1500/day, STANDARD, ALL, no TTL, event retention")
    void alertsDescriptor() {
        SourceDescriptor d = byId(family()).get("transit-alerts-hsl-helsinki").descriptor();

        assertThat(d.defaultInterval()).isEqualTo(Duration.ofSeconds(60));
        assertThat(d.minInterval()).isEqualTo(Duration.ofSeconds(30));
        assertThat(d.defaultBudget().perMinute()).isEqualTo(2);
        assertThat(d.defaultBudget().perDay()).isEqualTo(1500);
        assertThat(d.historyTier()).isEqualTo(HistoryTier.STANDARD);
        assertThat(d.defaultIngestPolicy()).isEqualTo(IngestPolicy.all());
        assertThat(d.recordTtl()).isNull();
        assertThat(d.zeroResultsNormal()).isTrue();
        assertThat(d.embedFields()).containsExactly("headerText", "descriptionText");
        assertThat(d.defaultRetention()).isEqualTo(Duration.ofDays(1825));
        assertThat(d.licence()).isEqualTo("CC BY 4.0");
        assertThat(d.params()).singleElement().satisfies(p -> {
            assertThat(p.name()).isEqualTo("language");
            assertThat(p.defaultValue()).isEqualTo("en");
        });
    }

    @Test
    @DisplayName("every URL comes from the reviewed list; the test base URL moves scheme and host only")
    void urls() {
        for (SourceAdapter adapter : family().adapters()) {
            TransitFeed feed = adapter instanceof TransitVehicleAdapter v ? v.feed() : ((TransitAlertsAdapter) adapter).feed();
            assertThat(TransitFeeds.REVIEWED).contains(feed);
        }
        TransitFamily rebased = new TransitFamily("", "http://127.0.0.1:9999/", new MobilityCatalog(
                MobilityCatalogTest.TEST_CATALOG), TestOutbound.offlinePolicy(), () -> "x", null);
        TransitVehicleAdapter capmetro = (TransitVehicleAdapter) byId(rebased).get("transit-capmetro-austin");
        assertThat(capmetro.feed().vehiclePositionsUrl()).isEqualTo(
                URI.create("http://127.0.0.1:9999/download/eiei-9rpf/application%2Foctet-stream"));
    }

    @Test
    @DisplayName("the language parameter must be a BCP 47 tag")
    void languageParam() {
        TransitAlertsAdapter alerts = (TransitAlertsAdapter) byId(family()).get("transit-alerts-mbta");
        assertThat(alerts.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("language", "nb-NO")))).isEmpty();
        assertThat(alerts.checkParams(ai.foresignal.source.spi.ValidatedParams.of(Map.of("language", "en; drop"))))
                .containsKey("language");
        assertThat(alerts.checkParams(ai.foresignal.source.spi.ValidatedParams.empty())).isEmpty();
    }
}
