package ai.foresignal.outlook.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.foresignal.analytics.finding.FindingEvent;
import ai.foresignal.analytics.finding.FindingLogEntity;
import ai.foresignal.analytics.finding.FindingLogRepository;
import ai.foresignal.analytics.math.NbSampling;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Index weight fits (prompt 126): a planted relationship between one component and the next-four-week target count is
 * recovered from stored Monday values and reference history; the weights are proposed, not applied; the out-of-sample
 * skill is reported on the fit and the definition; {@code autoApplyFittedWeights} applies them; and applying a fitted
 * version through the weights operation is logged.
 */
@IntegrationTest
@Import(MutableClock.TestConfig.class)
class IndexFitIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    static final int REGIONS = 24;

    static final int WEEKS = 90;

    @Autowired MutableClock clock;
    @Autowired RegionalIndexService indices;
    @Autowired IndexFitService fits;
    @Autowired RegionalIndexDefRepository defs;
    @Autowired RegionalIndexValueRepository values;
    @Autowired RegionalIndexFitRepository fitRows;
    @Autowired ReferenceImportRepository imports;
    @Autowired FindingLogRepository logs;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    /**
     * Synthetic fitting rows: each region has a constant signal in [0, 1]; the noise component is random per week; the
     * four-week target is NB with mean {@code 4·exp(0.5 + 2·signal)}.
     */
    static List<IndexFitter.Row> plantedRows(int regions, int weeks, long seed) {
        Random random = new Random(seed);
        NbSampling sampler = new NbSampling(seed);
        List<IndexFitter.Row> rows = new ArrayList<>();
        LocalDate first = LocalDate.of(2024, 1, 1);
        for (int r = 0; r < regions; r++) {
            double signal = (r + 0.5) / regions;
            for (int w = 0; w < weeks; w++) {
                double mean = 4 * Math.exp(0.5 + 2 * signal);
                rows.add(new IndexFitter.Row("R" + r, first.plusWeeks(w), new double[] {signal, random.nextDouble()},
                        sampler.negBin(mean, 10), (double) sampler.negBin(mean, 10)));
            }
        }
        rows.sort(java.util.Comparator.comparing(IndexFitter.Row::week));
        return rows;
    }

    static String region(int r) {
        return "Q" + (char) ('A' + r);
    }

    @BeforeEach
    void setUp() throws Exception {
        clock.setInstant(NOW);
        indices.put("unrest", json.readTree("""
                {"name": "Unrest", "components": [
                  {"key": "signal", "ref": "rollup:day/*/conflict-event/{region}", "transform": "pctrank",
                   "window": "P28D", "direction": 1},
                  {"key": "noise", "ref": "rollup:day/*/internet-outage/{region}", "transform": "pctrank",
                   "window": "P28D", "direction": 1}],
                 "weights": {"signal": 0.5, "noise": 0.5}, "target": "conflict:battles", "enabled": true}
                """), "test");
        ReferenceImportEntity ucdp = new ReferenceImportEntity(Ulids.next(NOW), "ucdp-ged", "k-fit", "test", NOW);
        ucdp.setStatus("done");
        ucdp.setLicense("CC-BY-4.0");
        ucdp.setEgress("OPEN");
        imports.save(ucdp);
        Random random = new Random(5);
        NbSampling sampler = new NbSampling(6);
        LocalDate monday = NOW.atZone(ZoneOffset.UTC).toLocalDate().with(TemporalAdjusters.previousOrSame(
                DayOfWeek.MONDAY));
        LocalDate first = monday.minusWeeks(WEEKS + 60);
        for (int r = 0; r < REGIONS; r++) {
            double signal = (r + 0.5) / REGIONS;
            for (LocalDate w = first; w.isBefore(monday); w = w.plusWeeks(1)) {
                int count = sampler.negBin(Math.exp(0.5 + 2 * signal), 10);
                jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count,"
                        + " fatalities, licence, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)",
                        ReferenceFamilies.BATTLES, region(r), Date.valueOf(w), ReferenceFamilies.UCDP, count,
                        "CC-BY-4.0", "OPEN", ucdp.getId());
            }
            for (int k = 1; k <= WEEKS; k++) {
                LocalDate day = monday.minusWeeks(k + 5);
                RegionalIndexValueEntity v = new RegionalIndexValueEntity(Ulids.next(NOW), "unrest", region(r), day,
                        50, NOW);
                List<Map<String, Object>> parts = new ArrayList<>();
                parts.add(part("signal", 100 * signal));
                parts.add(part("noise", 100 * random.nextDouble()));
                v.setComponents(parts);
                v.setWeightsVersion(1);
                values.save(v);
            }
        }
    }

    static Map<String, Object> part(String key, double normalized) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("key", key);
        p.put("raw", null);
        p.put("normalised", normalized);
        p.put("weight", 0.5);
        p.put("contribution", normalized / 2);
        p.put("missing", false);
        return p;
    }

    RegionalIndexFitEntity runFit() {
        RegionalIndexFitEntity fit = fitRows.save(new RegionalIndexFitEntity(Ulids.next(clock.instant()), "unrest",
                clock.instant()));
        return fits.runFit(fit);
    }

    @Test
    @DisplayName("the planted relationship is recovered; weights are proposed, not applied; skill is reported")
    void plantedRecovered() {
        RegionalIndexFitEntity fit = runFit();
        assertThat(fit.getStatus()).as(String.valueOf(fit.getError())).isEqualTo("done");
        assertThat(fit.getProposedWeights()).isNotNull();
        assertThat(fit.getProposedWeights().get("signal")).isGreaterThan(fit.getProposedWeights().get("noise"));
        assertThat(fit.getWeightsVersion()).isEqualTo(2);
        assertThat(fit.getRegions()).isEqualTo(REGIONS);
        assertThat(fit.getLambda()).isNotNull();
        assertThat(fit.getSkill()).containsKeys("evaluatedAt", "n", "pinballLoss", "logScore", "baselinePinballLoss",
                "skillPinball", "skillLog");
        assertThat(fit.getComponents()).anySatisfy(c -> {
            assertThat(c).containsEntry("key", "signal");
            assertThat(c).containsEntry("significant", true);
        });
        assertThat(fit.isApplied()).isFalse();
        RegionalIndexDefEntity def = defs.findById("unrest").orElseThrow();
        assertThat(def.getWeights()).containsEntry("signal", 0.5).containsEntry("noise", 0.5);
        assertThat(def.getWeightsOrigin()).isEqualTo("operator");
        assertThat(def.getSkill()).isEqualTo(fit.getSkill());
        assertThat(logs.findAll()).extracting(FindingLogEntity::getEvent).contains(FindingEvent.INDEX_WEIGHTS_PROPOSED);
        assertThat(IndexViews.fit(fit)).containsEntry("method", IndexFitService.METHOD);
    }

    @Test
    @DisplayName("fitted weights apply automatically only with autoApplyFittedWeights")
    void autoApply() {
        RegionalIndexDefEntity def = defs.findById("unrest").orElseThrow();
        def.setAutoApplyFittedWeights(true);
        defs.save(def);
        RegionalIndexFitEntity fit = runFit();
        assertThat(fit.isApplied()).isTrue();
        RegionalIndexDefEntity after = defs.findById("unrest").orElseThrow();
        assertThat(after.getWeightsOrigin()).isEqualTo("fitted");
        assertThat(after.getWeights()).isEqualTo(fit.getProposedWeights());
        assertThat(after.getWeightsVersion()).isEqualTo(2);
        assertThat(after.getWeightsLog()).last().satisfies(e -> assertThat(e).containsEntry("origin", "fitted"));
    }

    @Test
    @DisplayName("the weights operation applies the latest fitted version (logged), and refuses another version")
    void applyFitted() throws Exception {
        RegionalIndexFitEntity fit = runFit();
        assertThatThrownBy(() -> indices.applyWeights("unrest", json.readTree("{\"source\":\"fitted\",\"version\":9}"),
                "agent")).isInstanceOf(ai.foresignal.api.error.ValidationException.class);
        RegionalIndexDefEntity def = indices.applyWeights("unrest", json.readTree("{\"source\":\"fitted\",\"version\":"
                + fit.getWeightsVersion() + ",\"rationale\":\"beat seasonal naive\"}"), "agent");
        assertThat(def.getWeightsOrigin()).isEqualTo("fitted");
        assertThat(def.getWeightsLog()).last().satisfies(e -> {
            assertThat(e).containsEntry("by", "agent");
            assertThat(e).containsEntry("rationale", "beat seasonal naive");
        });
        assertThat(fitRows.findById(fit.getId()).orElseThrow().isApplied()).isTrue();
        RegionalIndexDefEntity reset = indices.applyWeights("unrest", json.readTree("{\"source\":\"default\"}"),
                "agent");
        assertThat(reset.getWeights()).containsEntry("signal", 0.5);
        assertThat(reset.getWeightsOrigin()).isEqualTo("default");
    }

    @Test
    @DisplayName("too little history fails the fit with a reason")
    void insufficient() {
        values.deleteAll();
        RegionalIndexFitEntity fit = runFit();
        assertThat(fit.getStatus()).isEqualTo("failed");
        assertThat(fit.getError()).contains("needs at least");
    }
}
