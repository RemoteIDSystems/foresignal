package ai.foresignal.precursor;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingLogRepository;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * Precursor mining end to end (prompt 120, FDR-1): a planted precedence that holds in both windows becomes
 * {@code validated}; one present only in the discovery window stays {@code candidate} with the reason; batch sizes and
 * the cap are recorded; a re-test after the pattern decays makes the precursor {@code stale}, and a second
 * {@code retired}, each announced on the {@code analytics} topic. Occurrences are change-point and unexplained-move
 * findings recorded through {@code FindingService}; records are seeded only through {@code TestRecords}.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class PrecursorValidationIT extends AnalyticsItSupport {

    /** The decision time of the first run (a UTC midnight one year after the first seeded day). */
    static final Instant NOW = T0.plus(Duration.ofDays(400));

    @Autowired PrecursorMiner miner;
    @Autowired PrecursorService service;
    @Autowired PrecursorRepository precursors;
    @Autowired FindingService findings;
    @Autowired FindingLogRepository logs;
    @Autowired JsonMapper json;

    int seq;

    /** Records one finding of {@code type} in {@code family}, region YE, at {@code at}. */
    void finding(String type, String family, Instant at) {
        findings.record(new FindingDraft(type, type + " " + family + " " + at, "notable", null, List.of("YE"), at,
                at.plus(Duration.ofHours(1)), at, List.of(), List.of(), 1, Map.of("i", seq), Map.of("value", 1.0,
                        "unit", "alarms-per-year", "method", "detector-false-alarm-rate", "text", "t"),
                List.of(), "test", "1.0.0", Map.of(), "planted", false, family, List.of(), List.of(),
                type + "-" + family + "-" + seq++));
    }

    /**
     * Plants antecedents of {@code antecedentFamily} every 4–10 days over {@code [from, to)}, each followed one day
     * later by an unexplained move of {@code consequentFamily} while before {@code consequentsUntil}.
     */
    void plant(String antecedentFamily, String consequentFamily, Instant from, Instant to, Instant consequentsUntil,
            long seed) {
        Random random = new Random(seed);
        for (Instant t = from; t.isBefore(to); t = t.plus(Duration.ofDays(4 + random.nextInt(7)))) {
            finding("change-point", antecedentFamily, t.plus(Duration.ofHours(random.nextInt(12))));
            Instant consequent = t.plus(Duration.ofDays(1));
            if (consequent.isBefore(consequentsUntil)) {
                finding("unexplained-move", consequentFamily, consequent.plus(Duration.ofHours(random.nextInt(6))));
            }
        }
    }

    void candidate(String antecedentFamily, String consequentFamily) {
        service.createCandidate(json.readTree("{\"kind\":\"event\",\"antecedent\":{\"selector\":{\"changePoint\":"
                + "{\"family\":\"" + antecedentFamily + "\"}}},\"consequent\":{\"selector\":{\"unexplainedMove\":"
                + "{\"family\":\"" + consequentFamily + "\"}}},\"spatial\":{\"rule\":\"sameRegion\"},\"lagMin\":\"P0D\","
                + "\"lagMax\":\"P2D\",\"regionCodes\":[\"YE\"]}"), "test");
    }

    PrecursorEntity byAntecedent(String family) {
        return precursors.findAll().stream().filter(p -> p.getAntecedent().toString().contains(family)).findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("valid in both windows → validated; discovery only → candidate; decayed → stale, then retired")
    void lifecycle() {
        Instant end = NOW.truncatedTo(java.time.temporal.ChronoUnit.DAYS);
        Instant start = end.minus(Duration.ofDays(365));
        Instant split = PrecursorMiner.split(start.plus(Duration.ofDays(1)), end, 2.0 / 3.0);
        plant("conflict", "markets", start.plus(Duration.ofDays(1)), end.minus(Duration.ofDays(3)), end, 120);
        plant("seismic", "attention", start.plus(Duration.ofDays(1)), end.minus(Duration.ofDays(3)),
                split.minus(Duration.ofDays(3)), 121);
        candidate("conflict", "markets");
        candidate("seismic", "attention");
        settings.put(SettingKeys.ANALYSIS_PRECURSORS_MAX_TESTS, 2);
        clock.setInstant(NOW);

        PrecursorBatchEntity batch = miner.run("mine", "manual", null, JobMonitor.NONE);

        assertThat(batch.getStatus()).isEqualTo("done");
        assertThat(batch.getMode()).isEqualTo("mine");
        assertThat(batch.getTested()).isEqualTo(2);
        assertThat(batch.getCandidatesGenerated() - batch.getCapped()).isEqualTo(2);
        assertThat(batch.getCapped()).isPositive();
        assertThat(batch.getSurvivors()).isEqualTo(2);
        assertThat(batch.getValidated()).isEqualTo(1);
        assertThat(batch.getMethod()).isEqualTo("BH");
        assertThat(batch.getQ()).isEqualTo(0.10);

        PrecursorEntity planted = byAntecedent("conflict");
        assertThat(planted.getStatus()).isEqualTo("validated");
        assertThat(planted.getOrigin()).isEqualTo("operator");
        assertThat(planted.getBatchSize()).isEqualTo(2);
        assertThat(planted.getLift()).isGreaterThanOrEqualTo(PrecursorRules.VALIDATION_LIFT);
        assertThat(planted.getHitRateLower()).isGreaterThan(planted.getBaseRate());
        assertThat(planted.getValidation()).containsEntry("verdict", "validated");
        assertThat(((Number) planted.getDiscovery().get("adjustedP")).doubleValue()).isLessThanOrEqualTo(0.10);
        assertThat(planted.getDiscovery()).containsEntry("verdict", "notable");
        assertThat(planted.getFirstValidatedAt()).isEqualTo(NOW);
        assertThat(planted.getDescription()).startsWith("In YE, ").contains("validated on");
        assertThat(planted.getCaveats()).extracting(c -> c.get("code")).contains("NOT_GROUND_TRUTH",
                "MULTIPLE_TESTS");
        assertThat(planted.getManifestId()).isNotNull();
        assertThat(PrecursorViews.precursor(planted)).containsEntry("associationNote",
                "association in stored data; not causation");

        PrecursorEntity discoveryOnly = byAntecedent("seismic");
        assertThat(discoveryOnly.getStatus()).isEqualTo("candidate");
        assertThat(discoveryOnly.getStatusReason()).contains("validation");
        assertThat(discoveryOnly.getValidation()).containsEntry("verdict", "failed");
        assertThat(discoveryOnly.getFirstValidatedAt()).isNull();

        assertThat(logs.findByFindingIdOrderByIdAsc(planted.getId())).extracting(l -> l.getEvent())
                .containsExactly("precursor.validated");

        // The pattern decays: antecedents keep coming, consequents stop.
        Instant later = NOW.plus(Duration.ofDays(150));
        plant("conflict", "markets", end, later.minus(Duration.ofDays(3)), end, 122);
        clock.setInstant(later);

        PrecursorBatchEntity retest = miner.run("retest", "manual", null, JobMonitor.NONE);
        assertThat(retest.getMode()).isEqualTo("retest");
        PrecursorEntity stale = precursors.findById(planted.getId()).orElseThrow();
        assertThat(stale.getStatus()).isEqualTo("stale");
        assertThat(stale.getStaleCount()).isEqualTo(1);
        assertThat(stale.getLastRetest()).isNotNull();
        assertThat(stale.getLastTestedAt()).isEqualTo(later);

        miner.run("retest", "manual", null, JobMonitor.NONE);
        PrecursorEntity retired = precursors.findById(planted.getId()).orElseThrow();
        assertThat(retired.getStatus()).isEqualTo("retired");
        assertThat(retired.getStatusReason()).startsWith("stale twice in a row");
        assertThat(retired.getHistory()).extracting(h -> h.get("to")).containsExactly("validated", "stale", "retired");
        assertThat(logs.findByFindingIdOrderByIdAsc(planted.getId())).extracting(l -> l.getEvent())
                .containsExactly("precursor.validated", "precursor.stale", "precursor.retired");
        assertThat(precursors.findById(discoveryOnly.getId()).orElseThrow().getStatus()).isEqualTo("candidate");

        // A new mining run does not test retired patterns again.
        PrecursorBatchEntity again = miner.run("mine", "manual", null, JobMonitor.NONE);
        assertThat(again.getStatus()).isEqualTo("done");
        assertThat(precursors.findById(planted.getId()).orElseThrow().getStatus()).isEqualTo("retired");
        assertThat(service.batches(null, 10).items()).hasSize(4);
    }
}
