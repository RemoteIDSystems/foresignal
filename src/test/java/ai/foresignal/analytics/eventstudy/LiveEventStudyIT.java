package ai.foresignal.analytics.eventstudy;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.intel.ForesignalIntelGenerator;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Live event studies (prompt 119): a seeded intelligence event triggers the matching watch pairs and writes a
 * {@code pre-event-signal}; lagged weekly data schedules a re-check that later carries {@code LAGGED_PUBLICATION}; an
 * {@code unexplained-move} is linked to an event that follows within seven days, which also writes a pre-event signal.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class LiveEventStudyIT extends AnalyticsItSupport {

    /** The event day. */
    static final Instant EVENT_DAY = Instant.parse("2026-03-02T00:00:00Z");

    @Autowired LiveEventStudyService live;
    @Autowired AnalysisSeriesService registry;
    @Autowired FindingService findings;
    @Autowired JsonMapper json;

    /** A random-walk price for {@code days} days ending the day before {@code end}, with a run-up in the last five. */
    private void seedPrices(String seriesId, Instant end, int days, double runUp, Instant publishedAt) {
        Random random = new Random(119);
        List<NormalizedRecord> batch = new ArrayList<>();
        double v = 70;
        Instant start = end.minus(Duration.ofDays(days));
        for (int i = 0; i < days; i++) {
            v *= Math.exp(0.01 * random.nextGaussian() + (i >= days - 5 ? runUp : 0));
            batch.add(dailyPoint(seriesId, start.plus(Duration.ofDays(i)), v, publishedAt));
        }
        batch.add(latest(seriesId, "P1D", v, publishedAt, SeriesMeta.of(SeriesMeta.PRICE, SeriesMeta.LOG_RETURN, 1)));
        seed(AnalyticsTestSources.SERIES, publishedAt, batch.toArray(NormalizedRecord[]::new));
    }

    private long intelEvent(String id, Instant at, String family) {
        seed(ForesignalIntelGenerator.ID, at, NormalizedRecord.builder(id, ForesignalIntelGenerator.KIND).observedAt(at)
                .put("type", "maritime-disruption").put("severity", "warning").put("status", "active")
                .put("title", "Shipping disruption " + id).put("signalFamilies", List.of(family)).build());
        return jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class);
    }

    private void register(String ref, String frequency, String pairs) {
        registry.upsert(json.readTree("{\"ref\":\"" + ref + "\",\"frequency\":\"" + frequency + "\",\"watchPairs\":"
                + pairs + "}"));
    }

    private List<AnalyticFindingEntity> findingsOf(String type) {
        return findings.list(new FindingService.ListQuery(List.of(type), List.of(), List.of(), List.of(), null, null,
                null, null, 50), true).items();
    }

    @Test
    @DisplayName("a qualifying intel event triggers the watch pair and writes a pre-event signal from known data only")
    void preEventSignal() {
        seedPrices("oil", EVENT_DAY, 700, 0.03, EVENT_DAY.minus(Duration.ofHours(1)));
        register("series:" + AnalyticsTestSources.SERIES + "/oil", "day",
                "[{\"eventFamily\":\"ais-behaviour\",\"regionCodes\":[],\"minEffect\":2.0}]");
        live.rewind(jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class));
        intelEvent("e1", EVENT_DAY.plus(Duration.ofHours(12)), "ais-behaviour");

        LiveEventStudyService.PollResult result = live.poll();

        assertThat(result.events()).isEqualTo(1);
        assertThat(result.findings()).isEqualTo(1);
        AnalyticFindingEntity signal = findingsOf("pre-event-signal").get(0);
        assertThat(signal.getStats()).containsEntry("linkedEventId", "intel-event:e1");
        assertThat(((Number) signal.getStats().get("placeboP")).doubleValue()).isLessThanOrEqualTo(0.01);
        assertThat(((Number) signal.getStats().get("car")).doubleValue()).isGreaterThan(2);
        assertThat(signal.getBaseRate()).containsEntry("method", "placebo-distribution");
        assertThat(signal.getCaveats()).extracting(c -> c.get("code")).contains("DAILY_RESOLUTION_ONLY");
        assertThat(signal.getExplanation()).contains("not evidence of wrongdoing");
        assertThat(signal.getMembers()).anyMatch(m -> m.startsWith("record:foresignal-intel/e1@"));
        assertThat(live.cursor()).isEqualTo(result.cursor());

        live.rewind(result.cursor() - 1);
        assertThat(live.poll().findings()).as("a re-scan writes nothing new").isLessThanOrEqualTo(1);
        assertThat(findingsOf("pre-event-signal")).as("the identical finding is not duplicated").hasSize(1);
    }

    @Test
    @DisplayName("an event of another family or a quiet series writes nothing")
    void nonMatching() {
        seedPrices("calm", EVENT_DAY, 700, 0, EVENT_DAY.minus(Duration.ofHours(1)));
        register("series:" + AnalyticsTestSources.SERIES + "/calm", "day",
                "[{\"eventFamily\":\"ais-behaviour\",\"regionCodes\":[]}]");
        live.rewind(jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class));
        intelEvent("e2", EVENT_DAY.plus(Duration.ofHours(12)), "ais-behaviour");
        intelEvent("e3", EVENT_DAY.plus(Duration.ofHours(13)), "seismic");
        assertThat(live.poll().findings()).isZero();
    }

    @Test
    @DisplayName("lagged weekly data schedules a re-check; the re-check carries LAGGED_PUBLICATION")
    void laggedWeekly() {
        Instant monday = Instant.parse("2026-03-02T00:00:00Z");
        Random random = new Random(5);
        List<NormalizedRecord> batch = new ArrayList<>();
        double level = 0;
        int weeks = 300;
        Instant start = monday.minus(Duration.ofDays(7L * weeks));
        for (int i = 0; i < weeks - 1; i++) {
            level += 1000 * random.nextGaussian() + (i == weeks - 2 ? 30_000 : 0);
            batch.add(weeklyPoint("cot", start.plus(Duration.ofDays(7L * i)), level, monday.minus(Duration.ofDays(3))));
        }
        batch.add(latest("cot", "P1W", level, monday.minus(Duration.ofDays(3)),
                SeriesMeta.of(SeriesMeta.POSITION, SeriesMeta.DIFFERENCE, 3)));
        seed(AnalyticsTestSources.SERIES, monday.minus(Duration.ofDays(3)), batch.toArray(NormalizedRecord[]::new));
        register("series:" + AnalyticsTestSources.SERIES + "/cot", "week",
                "[{\"eventFamily\":\"conflict\",\"regionCodes\":[],\"minEffect\":2.0}]");
        live.rewind(jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class));
        intelEvent("e4", monday.plus(Duration.ofHours(10)), "conflict");

        LiveEventStudyService.PollResult first = live.poll();
        assertThat(first.rechecks()).isEqualTo(1);
        assertThat(settings.get(SettingKeys.ANALYSIS_LIVE_RECHECKS, List.class)).get().asList().hasSize(1);

        Instant published = monday.plus(Duration.ofDays(2));
        level += 30_000;
        seed(AnalyticsTestSources.SERIES, published, weeklyPoint("cot", monday.minus(Duration.ofDays(7)), level,
                published));
        clock.setInstant(monday.plus(Duration.ofDays(6)));
        assertThat(live.processDue(clock.instant())).isEqualTo(1);
        assertThat(settings.get(SettingKeys.ANALYSIS_LIVE_RECHECKS, List.class)).get().asList().isEmpty();
        List<AnalyticFindingEntity> signals = findingsOf("pre-event-signal");
        assertThat(signals).allSatisfy(s -> assertThat(s.getCaveats()).extracting(c -> c.get("code"))
                .contains("LAGGED_PUBLICATION"));
    }

    @Test
    @DisplayName("an unexplained move is linked to an event within seven days, which writes a pre-event signal")
    void unexplainedMoveLinked() {
        Random random = new Random(11);
        List<NormalizedRecord> batch = new ArrayList<>();
        Instant end = Instant.parse("2026-03-02T00:00:00Z");
        int days = 400;
        for (int i = 0; i < days; i++) {
            double value = 1000 + 30 * random.nextGaussian() + (i == days - 1 ? 2_000 : 0);
            batch.add(dailyPoint("views", end.minus(Duration.ofDays(days - i)), value,
                    end.minus(Duration.ofHours(1))));
        }
        batch.add(latest("views", "P1D", 3000, end.minus(Duration.ofHours(1)),
                SeriesMeta.of(SeriesMeta.ATTENTION, SeriesMeta.LOG_LEVEL, 1)));
        seed(AnalyticsTestSources.SERIES, end.minus(Duration.ofHours(1)), batch.toArray(NormalizedRecord[]::new));
        register("series:" + AnalyticsTestSources.SERIES + "/views", "day", "[]");

        clock.setInstant(end.plus(Duration.ofHours(4)));
        assertThat(live.scanMoves(clock.instant())).isEqualTo(1);
        AnalyticFindingEntity move = findingsOf("unexplained-move").get(0);
        assertThat(((Number) move.getStats().get("z")).doubleValue()).isGreaterThanOrEqualTo(3);
        assertThat(move.getStats().get("linkedEventId")).isNull();

        live.rewind(jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class));
        intelEvent("e5", end.plus(Duration.ofDays(3)), "conflict");
        live.poll();

        AnalyticFindingEntity linked = findings.visible(move.getId(), true);
        assertThat(linked.getStats()).containsEntry("linkedEventId", "intel-event:e5");
        assertThat(((Number) linked.getStats().get("leadDays")).doubleValue()).isBetween(3.0, 4.0);
        assertThat(findingsOf("pre-event-signal")).anySatisfy(s -> assertThat(s.getMembers())
                .contains("finding:" + move.getId()));
        assertThat(LiveEventStudyService.qualifies("situation", Map.of("status", "active"), Map.of("status",
                "emerging"))).isTrue();
        assertThat(LiveEventStudyService.qualifies("intelligence-event", Map.of("status", "active", "severity",
                "warning"), Map.of("status", "active", "severity", "critical"))).isFalse();
    }
}
