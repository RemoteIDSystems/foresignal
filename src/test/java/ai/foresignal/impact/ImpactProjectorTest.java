package ai.foresignal.impact;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.impact.hazard.HazardAdapters;
import ai.foresignal.impact.hazard.HazardForecast;
import ai.foresignal.impact.hazard.HazardRecord;
import ai.foresignal.impact.rate.CoOccurrenceCounter;
import ai.foresignal.impact.rate.ImpactRateService;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

/**
 * {@link ImpactProjector} (pure) and the question draft of {@link ImpactProjectionService}: P(impact) = P(hazard) ×
 * P(impact | class) with the product interval, the skipped pairs, the minimum probability, one projection per family
 * and region, the top 20, assets and paths per region, and no projection from a cone.
 */
class ImpactProjectorTest {

    static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);
    static final Instant T = Instant.parse("2026-09-10T15:00:00Z");
    static final ImpactPairs PAIRS = new ImpactPairs();

    static Geometry square(double lon, double lat, double size) {
        return GF.createPolygon(new Coordinate[] {new Coordinate(lon, lat), new Coordinate(lon + size, lat),
            new Coordinate(lon + size, lat + size), new Coordinate(lon, lat + size), new Coordinate(lon, lat)});
    }

    static HazardForecast wind34() {
        List<HazardRecord> bands = List.of(
                new HazardRecord("nhc-wind-probs", "34kt:10-20", 1, "wind-probability", Map.of("thresholdKt", 34,
                        "band", "10-20%", "pMin", 0.1, "pMax", 0.2), square(-100, 25, 10), T, null, null, null),
                new HazardRecord("nhc-wind-probs", "34kt:40-50", 2, "wind-probability", Map.of("thresholdKt", 34,
                        "band", "40-50%", "pMin", 0.4, "pMax", 0.5), square(-96, 28, 2), T, null, null, null));
        return HazardAdapters.windProbabilities(34, bands).orElseThrow();
    }

    static ImpactRateService.ImpactRate rate(String hazardClass, String family, long n, long x) {
        ImpactPairs.Pair pair = PAIRS.pairs().stream().filter(p -> p.hazardClass().equals(hazardClass)
                && p.impactFamily().equals(family)).findFirst().orElseThrow();
        return ImpactRateService.of(pair, "continent:NA", new CoOccurrenceCounter.Counts(n, x), T);
    }

    static final ImpactExposure.Admin1 TX = new ImpactExposure.Admin1("US-TX", "Texas", "US", square(-100, 26, 6));
    static final ImpactExposure.Admin1 LA = new ImpactExposure.Admin1("US-LA", "Louisiana", "US", square(-94, 29, 4));

    @Test
    @DisplayName("40% × 55% = 22%, interval the product of the bounds, assets and paths of the region")
    void product() {
        ImpactExposure.Asset port = new ImpactExposure.Asset("record:osm-ports/123", "Port Y", "port",
                GF.createPoint(new Coordinate(-95, 29)));
        ImpactExposure.Path path = new ImpactExposure.Path("record:osm-ports/123", "record:osm-power/9",
                List.of("record:osm-power/7"), "likely", 2);
        ImpactProjector.Rates rates = (h, f, rc) -> "outage".equals(f) ? rate(h, f, 40, 22) : rate(h, f, 10, 5);
        ImpactProjector.Result r = ImpactProjector.project(wind34(), List.of(TX), List.of(port), List.of(path),
                PAIRS.pairsFor("tropical-wind-34"), PAIRS, code -> "continent:NA", rates,
                ImpactProjector.Settings.DEFAULTS);
        assertThat(r.projections()).singleElement().satisfies(p -> {
            assertThat(p.family().id()).isEqualTo("outage");
            assertThat(p.pHazard()).isEqualTo(0.4);
            assertThat(p.rate().rate()).isEqualTo(0.55);
            assertThat(p.pImpact()).isCloseTo(0.22, within(1e-9));
            assertThat(p.lower()).isCloseTo(0.4 * p.rate().lower(), within(1e-5));
            assertThat(p.upper()).isCloseTo(0.5 * p.rate().upper(), within(1e-5));
            assertThat(p.assets()).singleElement().satisfies(a -> {
                assertThat(a.asset().ref()).isEqualTo("record:osm-ports/123");
                assertThat(a.pHazard()).isEqualTo(0.4);
                assertThat(a.distanceKm()).isZero();
            });
            assertThat(p.paths()).containsExactly(path);
        });
        // ground-stop has n = 10 < 20: skipped and listed, not guessed.
        assertThat(r.skipped()).singleElement().satisfies(s -> {
            assertThat(s.impactFamily()).isEqualTo("ground-stop");
            assertThat(s.reason()).isEqualTo("insufficient-history");
            assertThat(s.n()).isEqualTo(10L);
        });
    }

    @Test
    @DisplayName("below impact.minProbability (5%) dropped; no region class skipped")
    void thresholds() {
        ImpactProjector.Rates low = (h, f, rc) -> rate(h, f, 100, 10);
        ImpactProjector.Result r = ImpactProjector.project(wind34(), List.of(TX, LA), List.of(), List.of(),
                PAIRS.pairsFor("tropical-wind-34"), PAIRS, code -> "US-LA".equals(code) ? null : "continent:NA", low,
                ImpactProjector.Settings.DEFAULTS);
        // Texas: 0.4 × 0.1 = 4% < 5% for both families.
        assertThat(r.projections()).isEmpty();
        assertThat(r.belowThreshold()).isEqualTo(2);
        assertThat(r.skipped()).extracting(ImpactProjector.Skipped::reason).contains("no-region-class");
    }

    @Test
    @DisplayName("top 20 per hazard, highest first; one projection per family and region")
    void top() {
        List<ImpactExposure.Admin1> regions = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            regions.add(new ImpactExposure.Admin1("US-" + (char) ('A' + i / 26) + (char) ('A' + i % 26), "R" + i,
                    "US", square(-99 + i * 0.2, 26, 0.1)));
        }
        ImpactProjector.Rates rates = (h, f, rc) -> rate(h, f, 50, 40);
        ImpactProjector.Result r = ImpactProjector.project(wind34(), regions, List.of(), List.of(),
                PAIRS.pairsFor("tropical-wind-34"), PAIRS, code -> "continent:NA", rates,
                ImpactProjector.Settings.DEFAULTS);
        assertThat(r.projections()).hasSize(ImpactProjector.TOP);
        assertThat(r.projections()).isSortedAccordingTo((a, b) -> Double.compare(b.pImpact(), a.pImpact()));
        Map<String, Integer> perKey = new LinkedHashMap<>();
        r.projections().forEach(p -> perKey.merge(p.family().id() + p.admin1().code(), 1, Integer::sum));
        assertThat(perKey.values()).containsOnly(1);
    }

    @Test
    @DisplayName("a cone or categorical forecast projects nothing")
    void noProbability() {
        HazardForecast cone = HazardAdapters.cone(new HazardRecord("nhc-cyclone-cones", "al052026", 3, "cyclone-cone",
                Map.of("stormId", "al052026"), square(-100, 25, 10), T, null, null, null));
        ImpactProjector.Result r = ImpactProjector.project(cone, List.of(TX), List.of(), List.of(),
                PAIRS.pairsFor("tropical-wind-34"), PAIRS, code -> "continent:NA", (h, f, rc) -> rate(h, f, 50, 40),
                ImpactProjector.Settings.DEFAULTS);
        assertThat(r.projections()).isEmpty();
    }

    @Test
    @DisplayName("question: event-exists over the family's rule in the region, window to validTo + lag, dedup per issuance")
    void question() {
        HazardForecast f = wind34();
        ImpactProjector.Rates rates = (h, fam, rc) -> rate(h, fam, 40, 22);
        ImpactProjector.Projection p = ImpactProjector.project(f, List.of(TX), List.of(), List.of(),
                PAIRS.pairsFor("tropical-wind-34"), PAIRS, code -> "continent:NA", rates,
                ImpactProjector.Settings.DEFAULTS).projections().stream()
                .filter(x -> x.family().id().equals("outage")).findFirst().orElseThrow();
        String rule = ImpactRules.ruleId("outage").toString();
        Instant now = T.plus(Duration.ofHours(1));
        ForecastInputs.QuestionDraft d = ImpactProjectionService.questionDraft(f, p, rule, "01JFINDING00000000000000000",
                false, now);
        assertThat(d.title()).startsWith("Will an internet outage be reported in Texas (US-TX) between");
        assertThat(d.type()).isEqualTo("binary");
        assertThat(d.category()).isEqualTo("weather-impact");
        assertThat(d.resolver()).containsEntry("type", "event-exists");
        assertThat(d.resolver().get("selector")).isEqualTo(Map.of("alertRuleId", rule));
        assertThat(d.resolver().get("area")).isEqualTo(Map.of("regionCodes", List.of("US-TX")));
        Instant end = T.plus(Duration.ofHours(120 + 48));
        assertThat(d.resolveBy()).isEqualTo(end);
        assertThat(d.closesAt()).isEqualTo(now.plus(Duration.ofHours(1)));
        assertThat(d.links()).containsEntry("findingId", "01JFINDING00000000000000000");
        assertThat(d.dedupKey()).isEqualTo("impact:nhc-wind-probs:34:" + T.getEpochSecond() + ":outage:US-TX");
        assertThat(ImpactProjectionService.questionDraft(f, p, rule, "x", false, end)).isNull();
    }

    @Test
    @DisplayName("family rules: the family's sources, kinds and conditions, tagged origin-impact")
    @SuppressWarnings("unchecked")
    void ruleSpec() {
        Map<String, Object> spec = ImpactRules.spec(PAIRS.family("ground-stop"));
        assertThat(spec).containsEntry("sources", List.of("faa-nas-status")).containsEntry("kinds",
                List.of("airport-delay")).containsEntry("mode", "enter");
        assertThat((List<Object>) spec.get("tags")).contains((Object) ImpactRules.TAG, ImpactRules.ORIGIN_TAG, "impact-ground-stop");
        assertThat(ImpactRules.ruleId("outage")).isEqualTo(ImpactRules.ruleId("outage"))
                .isNotEqualTo(ImpactRules.ruleId("ground-stop"));
    }
}
