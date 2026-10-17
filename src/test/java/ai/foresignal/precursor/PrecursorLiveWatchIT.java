package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

/**
 * The precursor live watch (prompt 120 §5): an antecedent occurrence of a validated precursor produces exactly one
 * {@code precursor-active} finding with the expected consequent window and the shrunk probability; a restart from an
 * older cursor writes nothing twice; other families, other regions and non-validated precursors write nothing; the
 * durable cursor joins the consumer clamp (RET-2).
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class PrecursorLiveWatchIT extends AnalyticsItSupport {

    @Autowired PrecursorLiveWatch watch;
    @Autowired PrecursorRepository precursors;
    @Autowired PrecursorCursorConsumer consumer;
    @Autowired FindingService findings;

    int seq;

    PrecursorEntity precursor(String status, String family) {
        Instant now = clock.instant();
        PrecursorEntity p = new PrecursorEntity(Ulids.next(now), "key-" + status + "-" + family, "event", now);
        Map<String, Object> antecedent = new LinkedHashMap<>();
        antecedent.put("selector", Map.of("changePoint", Map.of("family", family), "regionCodes", List.of("YE")));
        antecedent.put("family", family);
        antecedent.put("label", "change points in " + family + " series in YE");
        Map<String, Object> consequent = new LinkedHashMap<>();
        consequent.put("selector", Map.of("unexplainedMove", Map.of("family", "markets"), "regionCodes",
                List.of("YE")));
        consequent.put("family", "markets");
        consequent.put("label", "unexplained moves in markets series in YE");
        p.setAntecedent(antecedent);
        p.setConsequent(consequent);
        p.setSpatial(Map.of("rule", "sameRegion", "regionLevel", "country"));
        p.setRegionCodes(List.of("YE"));
        p.setFamilies(List.of(family, "markets"));
        p.setLagMinSeconds(0);
        p.setLagMaxSeconds(Duration.ofDays(2).toSeconds());
        p.setHits(8);
        p.setNAntecedent(10);
        p.setHitRate(0.8);
        p.setBaseRate(0.2);
        p.setLift(4.0);
        p.setBatchSize(50);
        p.setStatus(status);
        p.setDescription("In YE, change points were followed by unexplained moves.");
        p.setUpdatedAt(now);
        return precursors.save(p);
    }

    AnalyticFindingEntity changePoint(String family, String region, Instant at) {
        return findings.record(new FindingDraft("change-point", "Change point " + family + " " + seq, "notable", null,
                List.of(region), at, at.plus(Duration.ofHours(1)), at, List.of(), List.of(), 1, Map.of("i", seq),
                Map.of("value", 1.0, "unit", "alarms-per-year", "method", "detector-false-alarm-rate", "text", "t"),
                List.of(), "changepoint-cusum", "1.0.0", Map.of(), "x", false, family, List.of(), List.of(),
                "cp-" + seq++)).finding();
    }

    /** Starts the watch afresh: the next poll begins at the heads. */
    void restartWatch() {
        watch.rewind(new PrecursorLiveWatch.Cursors(0, 0, 0, 0, 0, 0));
    }

    List<AnalyticFindingEntity> active() {
        return findings.list(new FindingService.ListQuery(List.of("precursor-active"), List.of(), List.of(), List.of(),
                null, null, null, null, 50), true).items();
    }

    @Test
    @DisplayName("an antecedent occurrence writes one precursor-active finding with the shrunk probability")
    void antecedentOccurrence() {
        clock.setInstant(T0.plus(Duration.ofDays(10)));
        PrecursorEntity p = precursor("validated", "conflict");
        precursor("candidate", "seismic");
        restartWatch();
        assertThat(watch.poll().findings()).as("the first poll starts at the heads").isZero();
        assertThat(consumer.cursorSeq()).isPresent();

        Instant at = clock.instant().minus(Duration.ofHours(2));
        AnalyticFindingEntity cp = changePoint("conflict", "YE", at);
        changePoint("conflict", "SA", at);
        changePoint("seismic", "YE", at);

        PrecursorLiveWatch.PollResult result = watch.poll();

        assertThat(result.occurrences()).isEqualTo(1);
        assertThat(result.findings()).isEqualTo(1);
        List<AnalyticFindingEntity> active = active();
        assertThat(active).hasSize(1);
        AnalyticFindingEntity f = active.get(0);
        assertThat(f.getPrecursorId()).isEqualTo(p.getId());
        assertThat(f.getStats()).containsEntry("precursorId", p.getId());
        assertThat(((Number) f.getStats().get("probability")).doubleValue()).isCloseTo(
                PrecursorRules.probability(8, 10, 0.2), within(1e-5));
        assertThat(f.getStats()).containsEntry("expectedFrom", at.toString())
                .containsEntry("expectedTo", at.plus(Duration.ofDays(2)).toString());
        assertThat(f.getWindowStart()).isEqualTo(at);
        assertThat(f.getWindowEnd()).isEqualTo(at.plus(Duration.ofDays(2)));
        assertThat(f.getMembers()).contains("finding:" + cp.getId(), "precursor:" + p.getId());
        assertThat(f.getBaseRate()).containsEntry("method", "precursor-base-rate");
        assertThat(f.getCaveats()).extracting(c -> c.get("code")).contains("NOT_GROUND_TRUTH", "MULTIPLE_TESTS");
        assertThat(f.getExplanation()).contains("association in stored data; not causation");
        assertThat(f.getManifestId()).isNotNull();
        assertThat(watch.cursor()).isEqualTo(result.cursors().seq());
    }

    @Test
    @DisplayName("restarting from an older cursor writes each finding exactly once")
    void restartExactlyOnce() {
        clock.setInstant(T0.plus(Duration.ofDays(10)));
        precursor("validated", "conflict");
        restartWatch();
        watch.poll();
        changePoint("conflict", "YE", clock.instant().minus(Duration.ofHours(1)));
        PrecursorLiveWatch.Cursors before = watch.cursors();
        PrecursorLiveWatch.PollResult first = watch.poll();
        assertThat(first.findings()).isEqualTo(1);

        watch.rewind(before);
        PrecursorLiveWatch.PollResult again = watch.poll();

        assertThat(again.occurrences()).isEqualTo(1);
        assertThat(again.findings()).as("the identical finding is left unchanged").isZero();
        assertThat(active()).hasSize(1);
        assertThat(watch.cursors().findings()).isEqualTo(first.cursors().findings());

        assertThat(watch.poll().occurrences()).as("its own precursor-active finding is no antecedent").isZero();
    }

    @Test
    @DisplayName("matching: selector keys, regions and minimum severity")
    void matching() {
        PrecursorLiveWatch.LiveOccurrence o = new PrecursorLiveWatch.LiveOccurrence(
                java.util.Set.of("alertRuleTag:gnss", "alertRuleId:r1"), T0, "alert:1", List.of("YE", "YE-HD"),
                "warning", "alert:1");
        assertThat(PrecursorLiveWatch.matches(Map.of("alertRuleTag", "gnss"), o)).isTrue();
        assertThat(PrecursorLiveWatch.matches(Map.of("alertRuleTag", "gnss", "regionCodes", List.of("YE-HD")), o))
                .isTrue();
        assertThat(PrecursorLiveWatch.matches(Map.of("alertRuleTag", "gnss", "regionCodes", List.of("SA")), o))
                .isFalse();
        assertThat(PrecursorLiveWatch.matches(Map.of("alertRuleTag", "gnss", "minSeverity", "critical"), o))
                .isFalse();
        assertThat(PrecursorLiveWatch.matches(Map.of("alertRuleTag", "other"), o)).isFalse();
    }
}
