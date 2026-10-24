package ai.foresignal.impact;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.FindingLogRepository;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastQuestionRepository;
import ai.foresignal.forecast.ForecastRepository;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.resolve.ResolutionService;
import ai.foresignal.impact.rate.ImpactRateService;
import ai.foresignal.infra.InfraGraphBuilder;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;

/**
 * The impact projector end to end (prompt 127): a seeded hurricane wind-probability advisory over Texas (reference
 * regions loaded), with a power plant and its dependents built into the 104 graph and 25 days of seeded history (14
 * followed by an IODA region outage within 48 h), produces a {@code projected-impact} finding with exposed assets,
 * dependency paths, the conditional rate and the label; opens a ledger question above 10% forecast by {@code impact};
 * is superseded by the next advisory; resolves {@code yes} from a seeded outage; and keeps {@code impact.cursor} in
 * the retention clamp.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class ImpactProjectorIT extends AnalyticsItSupport {

    static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

    /** The advisory time. */
    static final Instant ADVISORY = Instant.parse("2026-09-10T15:00:00Z");

    @Autowired ApplicationContext context;
    @Autowired ImpactProjectionService projection;
    @Autowired ImpactWatcher watcher;
    @Autowired ImpactRateService rates;
    @Autowired ImpactRules rules;
    @Autowired RegionClasses regionClasses;
    @Autowired InfraGraphBuilder infra;
    @Autowired AnalyticFindingRepository findings;
    @Autowired FindingLogRepository findingLog;
    @Autowired ForecastQuestionRepository questions;
    @Autowired ForecastRepository forecasts;
    @Autowired ForecasterRegistry forecasters;
    @Autowired ResolutionService resolution;

    static Geometry square(double lon, double lat, double size) {
        return GF.createPolygon(new Coordinate[] {new Coordinate(lon, lat), new Coordinate(lon + size, lat),
            new Coordinate(lon + size, lat + size), new Coordinate(lon, lat + size), new Coordinate(lon, lat)});
    }

    static NormalizedRecord band(String id, String band, double pMin, double pMax, Geometry g, Instant issued) {
        return NormalizedRecord.builder(id, "wind-probability").observedAt(issued).validFrom(issued)
                .validUntil(issued.plus(Duration.ofHours(120))).geometry(g).put("thresholdKt", 34).put("band", band)
                .put("pMin", pMin).put("pMax", pMax).put("issuedAt", issued.toString())
                .put("validUntil", issued.plus(Duration.ofHours(120)).toString()).put("horizonHours", 120).build();
    }

    static NormalizedRecord outage(String id, double lon, double lat, Instant at) {
        return NormalizedRecord.builder(id, "outage").observedAt(at).point(lon, lat).put("entityType", "region")
                .put("entityCode", id).put("entityName", "Texas").put("datasource", "ping-slash24")
                .put("level", "critical").put("country", "US").build();
    }

    @BeforeEach
    void setUp() {
        ReferenceFixtures.loadAll(context);
        regionClasses.reset();
        rates.clearCache();
        rules.reset();
        forecasters.registerBuiltIns();
        // The 104 graph: a plant just inside the advisory's south edge (29°N) supplying a substation 4.4 km south,
        // which supplies a hospital 3.3 km further on. Facilities within the 5 km asset buffer of the published
        // polygons are exposed and are the traversal's seeds, so the dependency path is the one to the hospital
        // beyond the buffer, from an exposed facility. (The plant's own substation always lies within the buffer:
        // the plant-substation rule reaches at most 5 km too.)
        Instant built = ADVISORY.minus(Duration.ofDays(60));
        ingestAt("wri-power-plants", built, NormalizedRecord.builder("pp1", "power-plant").observedAt(built)
                .point(-97.5, 29.02).put("name", "Plant").put("capacityMw", 500).build());
        ingestAt("osm-infrastructure", built,
                NormalizedRecord.builder("n10", "substation").observedAt(built).point(-97.5, 28.98)
                        .put("name", "Oak substation").build(),
                NormalizedRecord.builder("n11", "hospital").observedAt(built).point(-97.5, 28.95)
                        .put("name", "County hospital").build());
        infra.rebuild(Map.of());
        // History: 25 advisory days with a likely (≥ 50%) 34-kt band over east Texas; 14 followed by an outage.
        Instant start = ADVISORY.minus(Duration.ofDays(40));
        for (int d = 0; d < 25; d++) {
            Instant at = start.plus(Duration.ofDays(d));
            ingestAt("nhc-wind-probs", at, band("34kt:50-60", "50-60%", 0.5, 0.6, square(-96, 30.5, 1), at));
            if (d < 14) {
                Instant out = at.plus(Duration.ofHours(20));
                ingestAt("ioda-outages", out, outage("region:hist" + d + ":ping-slash24", -95.5, 31, out));
            }
        }
    }

    /** The current advisory: 40–50% over the plant inside a 10–20% band. */
    void advisory(Instant issued, double innerMin, double innerMax, String innerBand) {
        String id = "34kt:" + Math.round(innerMin * 100) + "-" + Math.round(innerMax * 100);
        ingestAt("nhc-wind-probs", issued,
                band("34kt:10-20", "10-20%", 0.1, 0.2, square(-98, 29, 5), issued),
                band(id, innerBand, innerMin, innerMax, square(-97.75, 29, 0.5), issued));
    }

    @Test
    @DisplayName("a hurricane advisory → a projected-impact finding with paths and labels, a question, supersession,"
            + " resolution")
    void endToEnd() {
        advisory(ADVISORY, 0.4, 0.5, "40-50%");
        clock.setInstant(ADVISORY.plus(Duration.ofMinutes(1)));

        ImpactRateService.ImpactRate rate = rates.rate("tropical-wind-34", "outage", "continent:NA");
        assertThat(rate.n()).isEqualTo(25);
        assertThat(rate.impacts()).isEqualTo(14);
        assertThat(rate.usable()).isTrue();

        ImpactProjectionService.RunResult run = projection.run("nhc-wind-probs:34");
        assertThat(run.forecast().probabilityModel()).isEqualTo("published-polygons");
        AnalyticFindingEntity f = findings.findById(run.projected().stream().filter(id -> "outage".equals(
                findings.findById(id).orElseThrow().getStats().get("impactFamily"))).findFirst().orElseThrow())
                .orElseThrow();
        Map<String, Object> stats = f.getStats();
        assertThat(f.getFindingType()).isEqualTo("projected-impact");
        assertThat(stats).containsEntry("admin1", "US-TX").containsEntry("hazardType", "tropical-wind-34");
        assertThat(((Number) stats.get("pHazard")).doubleValue()).isEqualTo(0.4);
        assertThat(((Number) stats.get("pConditional")).doubleValue()).isEqualTo(14.0 / 25);
        assertThat(((Number) stats.get("pImpact")).doubleValue()).isCloseTo(0.4 * 14 / 25,
                org.assertj.core.api.Assertions.within(1e-5));
        assertThat(stats).containsKeys("interval", "n", "exposedAssets", "dependencyPaths", "inputs", "skipped");
        assertThat(stats.get("labels")).isEqualTo(List.of(ImpactProjector.LABEL));
        assertThat((List<?>) stats.get("exposedAssets")).anySatisfy(a -> assertThat(((Map<?, ?>) a).get("ref"))
                .isEqualTo("record:wri-power-plants/pp1"));
        List<String> exposed = ((List<?>) stats.get("exposedAssets")).stream()
                .map(a -> String.valueOf(((Map<?, ?>) a).get("ref"))).toList();
        assertThat((List<?>) stats.get("dependencyPaths")).anySatisfy(p -> {
            Map<?, ?> path = (Map<?, ?>) p;
            assertThat(path.get("to")).isEqualTo("record:osm-infrastructure/n11");
            assertThat(exposed).contains(String.valueOf(path.get("from")));
            assertThat(path.get("weakestEdgeLabel")).isEqualTo("possible");
        });
        assertThat(f.getManifestId()).isNotNull();
        assertThat(f.getCaveats()).anySatisfy(c -> assertThat(c.get("code")).isEqualTo("MODELED"));

        // Ground stops never followed the 25 hazard days: a measured rate of 0 (n is above MIN_N, so it is not
        // insufficient), whose projection falls below the minimum probability and is not made.
        ImpactRateService.ImpactRate groundStops = rates.rate("tropical-wind-34", "ground-stop", "continent:NA");
        assertThat(groundStops.usable()).isTrue();
        assertThat(groundStops.n()).isEqualTo(25);
        assertThat(groundStops.impacts()).isZero();
        assertThat(run.projected()).noneSatisfy(id -> assertThat(findings.findById(id).orElseThrow().getStats()
                .get("impactFamily")).isEqualTo("ground-stop"));

        // 22% ≥ 10%: a binary weather-impact question forecast by impact (and the baselines).
        assertThat(f.getQuestionIds()).hasSize(1);
        ForecastQuestionEntity q = questions.findById(f.getQuestionIds().get(0)).orElseThrow();
        assertThat(q.getCategory()).isEqualTo("weather-impact");
        List<ForecastEntity> made = forecasts.findByQuestionIdOrderByMadeAtAscIdAsc(q.getId());
        assertThat(made).anySatisfy(fc -> {
            assertThat(fc.getForecasterId()).isEqualTo("impact");
            assertThat(fc.getProbability()).isCloseTo(0.4 * 14 / 25, org.assertj.core.api.Assertions.within(1e-5));
        });
        assertThat(findingLog.findAll()).anySatisfy(l -> assertThat(l.getEvent()).isEqualTo("impact.projected"));

        // A rerun of the same advisory changes nothing.
        ImpactProjectionService.RunResult again = projection.run("nhc-wind-probs:34");
        assertThat(again.projected()).isEmpty();
        assertThat(again.unchanged()).isGreaterThanOrEqualTo(1);

        // The next advisory supersedes the projection.
        Instant next = ADVISORY.plus(Duration.ofHours(6));
        advisory(next, 0.5, 0.6, "50-60%");
        clock.setInstant(next.plus(Duration.ofMinutes(1)));
        ImpactProjectionService.RunResult second = projection.run("nhc-wind-probs:34");
        assertThat(second.superseded()).contains(f.getId());
        AnalyticFindingEntity old = findings.findById(f.getId()).orElseThrow();
        assertThat(old.getStatus()).isEqualTo("superseded");
        assertThat(old.getSupersededBy()).isNotNull();
        assertThat(findingLog.findAll()).anySatisfy(l -> assertThat(l.getEvent()).isEqualTo("impact.superseded"));

        // A seeded outage in Texas inside the window resolves the first question yes.
        Instant hit = ADVISORY.plus(Duration.ofHours(30));
        ingestAt("ioda-outages", hit, outage("region:now:ping-slash24", -95.2, 31.8, hit));
        awaitTriggerCursor();
        clock.setInstant(hit.plus(Duration.ofHours(1)));
        resolution.runOnce();
        ForecastQuestionEntity resolved = questions.findById(q.getId()).orElseThrow();
        assertThat(resolved.getStatus()).isEqualTo("resolved");
        assertThat(resolved.getResolution()).containsEntry("outcome", "yes");
    }

    @Test
    @DisplayName("the watcher schedules each hazard once per burst, debounced 10 minutes; the cursor joins the clamp")
    void watcher() {
        watcher.resetState();
        advisory(ADVISORY, 0.4, 0.5, "40-50%");
        clock.setInstant(ADVISORY.plus(Duration.ofMinutes(1)));
        watcher.scan();
        assertThat(watcher.pending()).containsKey("nhc-wind-probs:34");
        assertThat(watcher.runDue(clock.instant())).isEmpty();
        assertThat(watcher.runDue(clock.instant().plus(ImpactWatcher.DEBOUNCE))).containsExactly("nhc-wind-probs:34");
        assertThat(watcher.pending()).isEmpty();
        long cursor = watcher.cursor();
        assertThat(cursor).isPositive();
        assertThat(settings.get(SettingKeys.IMPACT_CURSOR, Long.class)).contains(cursor);
        assertThat(job.consumers()).extracting(ai.foresignal.retention.RetentionConsumer::name)
                .contains(SettingKeys.IMPACT_CURSOR);
    }

    @Test
    @DisplayName("a probability below the threshold opens no question; a vanished advisory supersedes its projections")
    void thresholdsAndDisappearance() {
        ingestAt("nhc-wind-probs", ADVISORY, band("34kt:10-20", "10-20%", 0.1, 0.2, square(-98, 29, 5), ADVISORY));
        clock.setInstant(ADVISORY.plus(Duration.ofMinutes(1)));
        ImpactProjectionService.RunResult run = projection.run("nhc-wind-probs:34");
        // 10% × 56% = 5.6%: a finding (≥ 5%) but no question (< 10%).
        assertThat(run.projected()).isNotEmpty();
        assertThat(run.questions()).isEmpty();
        // Out of season the bands are tombstoned: the projections are superseded without a successor.
        Instant later = ADVISORY.plus(Duration.ofHours(12));
        ingestAt("nhc-wind-probs", later,
                NormalizedRecord.builder("34kt:10-20", "wind-probability").observedAt(later).tombstone(true).build(),
                NormalizedRecord.builder("34kt:50-60", "wind-probability").observedAt(later).tombstone(true).build());
        clock.setInstant(later.plus(Duration.ofMinutes(1)));
        ImpactProjectionService.RunResult gone = projection.run("nhc-wind-probs:34");
        assertThat(gone.superseded()).containsAll(run.projected());
        assertThat(findings.findById(run.projected().get(0)).orElseThrow().getSupersededBy()).isNull();
    }
}
