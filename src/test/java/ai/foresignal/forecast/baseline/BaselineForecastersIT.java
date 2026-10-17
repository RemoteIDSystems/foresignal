package ai.foresignal.forecast.baseline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.AnalyticFindingRepository;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.series.SeriesData;
import ai.foresignal.analytics.series.SeriesFrequency;
import ai.foresignal.analytics.series.SeriesReader;
import ai.foresignal.analytics.series.SeriesRef;
import ai.foresignal.analytics.series.SeriesValue;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolutionData;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import java.sql.Date;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * The baseline forecasters on seeded questions (prompt 122 §4): base rate (scaled, Poisson, conditional on a
 * precursor), climatology and persistence over a stubbed series, the precursor forecaster on a question opened from a
 * {@code precursor-active} finding, and analogs; each emits nothing, and says why, when its inputs are missing. Also
 * the runner's at-most-daily rule and the automatic ensemble.
 */
@IntegrationTest
class BaselineForecastersIT {

    static final Instant NOW = Instant.parse("2026-10-17T12:00:00Z");

    @Autowired MutableClock clock;
    @Autowired ForecastService ledger;
    @Autowired BaselineRunner runner;
    @Autowired ForecasterRegistry registry;
    @Autowired ReferenceImportRepository imports;
    @Autowired BaseRateService baseRates;
    @Autowired FindingService findings;
    @Autowired AnalyticFindingRepository findingRows;
    @Autowired PrecursorRepository precursors;
    @Autowired PrecursorQuestionOpener opener;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        clock.setInstant(NOW);
        registry.registerBuiltIns();
        baseRates.clearCache();
        ReferenceImportEntity ucdp = new ReferenceImportEntity(Ulids.next(NOW), "ucdp-ged", "k-ucdp", "test", NOW);
        ucdp.setStatus("done");
        ucdp.setLicense("CC-BY-4.0");
        ucdp.setEgress("OPEN");
        ucdp.setProgress(Map.of("done", List.of("2023", "2024")));
        imports.save(ucdp);
        LocalDate start = ReferenceFamilies.yearChunk(2023)[0];
        for (LocalDate w = start; w.isBefore(ReferenceFamilies.yearChunk(2024)[1]); w = w.plusWeeks(2)) {
            jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count, fatalities,"
                    + " license, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)", ReferenceFamilies.BATTLES,
                    "YE", Date.valueOf(w), ReferenceFamilies.UCDP, 2, "CC-BY-4.0", "OPEN", ucdp.getId());
        }
    }

    ForecastQuestionEntity open(String type, Map<String, Object> resolver, Map<String, String> links,
            List<String> labels) {
        Map<String, Object> outcomes = labels.isEmpty() ? Map.of() : Map.of("labels", labels);
        ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("Question?", type, outcomes,
                "Criteria.", ai.foresignal.forecast.resolve.ResolverSpec.validate(resolver, type), NOW,
                NOW.plus(Duration.ofDays(1)), NOW.plus(Duration.ofDays(29)), "conflict", List.of(), links, null, false);
        return ledger.openQuestion(draft, "agent", "test").question();
    }

    static Map<String, Object> battles(String type) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", type);
        r.put("selector", Map.of("referenceFamily", ReferenceFamilies.BATTLES));
        r.put("area", Map.of("regionCodes", List.of("YE")));
        r.put("window", Map.of("start", NOW.plus(Duration.ofDays(1)).toString(), "end",
                NOW.plus(Duration.ofDays(29)).toString()));
        return r;
    }

    Map<String, Object> notes(String id) {
        return ledger.find(id).orElseThrow().getBaselineNotes();
    }

    @Test
    @DisplayName("base-rate: the 28-day P(at least one) for battles in Yemen, and the ensemble follows")
    void baseRate() {
        ForecastQuestionEntity q = open(Ledger.BINARY, battles("event-exists"), Map.of(), List.of());
        assertThat(runner.runFor(q.getId())).isGreaterThanOrEqualTo(1);
        Map<String, ForecastEntity> latest = ledger.latestByForecaster(q.getId());
        ForecastEntity f = latest.get(ForecasterRegistry.BASE_RATE);
        assertThat(f).isNotNull();
        double expected = baseRates.query(Map.of("family", ReferenceFamilies.BATTLES, "regionCodes", List.of("YE"),
                "window", "P28D")).computed().result().pAtLeastOne();
        assertThat(f.getProbability()).isCloseTo(expected, within(1e-9));
        assertThat(f.getInputs()).containsKeys("definitionKey", "nWindows", "pAtLeastOne");
        assertThat(f.getRationale()).contains("Outside view");
        assertThat(f.getManifestId()).isNotNull();
        ForecastEntity ensemble = latest.get(ForecasterRegistry.ENSEMBLE);
        assertThat(ensemble).isNotNull();
        // Members without a track record weigh equally (ordered by forecaster id).
        List<Double> members = latest.values().stream()
                .filter(x -> !ForecasterRegistry.ENSEMBLE.equals(x.getForecasterId()))
                .sorted(java.util.Comparator.comparing(ForecastEntity::getForecasterId))
                .map(ForecastEntity::getProbability).toList();
        double[] p = members.stream().mapToDouble(Double::doubleValue).toArray();
        double[] w = new double[p.length];
        java.util.Arrays.fill(w, 1.0 / p.length);
        assertThat(ensemble.getProbability()).isCloseTo(
                ai.foresignal.forecast.ensemble.EnsembleMath.combineBinary(p, w, 1.0), within(1e-6));
        // At most daily: a second run the same day writes nothing.
        assertThat(runner.runFor(q.getId())).isZero();
        // A day later with the same data, an identical forecast is not written again.
        clock.setInstant(NOW.plus(Duration.ofHours(25)));
        assertThat(runner.runFor(q.getId())).isZero();
    }

    @Test
    @DisplayName("base-rate: a count threshold uses a Poisson count over the window")
    void baseRateCount() {
        Map<String, Object> r = battles("count-threshold");
        r.put("op", "gte");
        r.put("threshold", 3);
        ForecastQuestionEntity q = open(Ledger.BINARY, r, Map.of(), List.of());
        runner.runFor(q.getId());
        ForecastEntity f = ledger.latestByForecaster(q.getId()).get(ForecasterRegistry.BASE_RATE);
        double rate = baseRates.query(Map.of("family", ReferenceFamilies.BATTLES, "regionCodes", List.of("YE"),
                "window", "P28D")).computed().result().ratePerWindow();
        assertThat(f.getProbability()).isCloseTo(BaseRateForecaster.poisson(rate, "gte", 3), within(1e-9));
    }

    @Test
    @DisplayName("base-rate emits nothing, and says why, without a family or for a multiple question")
    void baseRateMissing() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "event-exists");
        r.put("selector", Map.of("intelEventType", "maritime-disruption"));
        r.put("window", Map.of("start", NOW.plus(Duration.ofDays(1)).toString()));
        ForecastQuestionEntity q = open(Ledger.BINARY, r, Map.of(), List.of());
        runner.runFor(q.getId());
        assertThat(ledger.latestByForecaster(q.getId())).doesNotContainKey(ForecasterRegistry.BASE_RATE);
        assertThat(String.valueOf(notes(q.getId()).get(ForecasterRegistry.BASE_RATE))).contains("no reference family");
        ForecastQuestionEntity m = open(Ledger.MULTIPLE, Map.of("type", "manual"), Map.of(), List.of("a", "b"));
        runner.runFor(m.getId());
        assertThat(String.valueOf(notes(m.getId()).get(ForecasterRegistry.BASE_RATE))).contains("multiple");
        // The analog baseline says why too: no family-based outcome and no situation.
        assertThat(String.valueOf(notes(q.getId()).get(ForecasterRegistry.ANALOG))).contains("no analog outcome");
    }

    @Test
    @DisplayName("precursor: a precursor-active finding opens a deduplicated question, forecast at its probability")
    void precursor() {
        PrecursorEntity p = new PrecursorEntity(Ulids.next(NOW), "key-precursor", "event", NOW);
        p.setAntecedent(Map.of("selector", Map.of("referenceFamily", ReferenceFamilies.EXPLOSIONS), "label",
                "explosions in YE"));
        p.setConsequent(Map.of("selector", Map.of("referenceFamily", ReferenceFamilies.BATTLES), "label",
                "battles in YE"));
        p.setSpatial(Map.of("rule", "sameRegion", "regionLevel", "country"));
        p.setRegionCodes(List.of("YE"));
        p.setFamilies(List.of("conflict"));
        p.setLagMaxSeconds(Duration.ofDays(7).toSeconds());
        p.setStatus("validated");
        p.setBatchId(Ulids.next(NOW));
        p.setBatchSize(50);
        p.setUpdatedAt(NOW);
        precursors.save(p);
        String ucdp = imports.findAll().get(0).getId();
        for (LocalDate w = ReferenceFamilies.yearChunk(2023)[0]; w.isBefore(ReferenceFamilies.yearChunk(2024)[1]);
                w = w.plusWeeks(4)) {
            jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count, fatalities,"
                    + " license, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, ?, ?, ?)",
                    ReferenceFamilies.EXPLOSIONS, "YE", Date.valueOf(w.minusWeeks(1)), ReferenceFamilies.UCDP, 1,
                    "CC-BY-4.0", "OPEN", ucdp);
        }
        Instant from = NOW.plus(Duration.ofDays(1));
        Instant to = NOW.plus(Duration.ofDays(7));
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("precursorId", p.getId());
        stats.put("expectedFrom", from.toString());
        stats.put("expectedTo", to.toString());
        stats.put("probability", 0.42);
        stats.put("hits", 9);
        stats.put("n", 20);
        stats.put("baseRate", 0.2);
        findings.record(new FindingDraft("precursor-active", "Precursor active", "notable", null, List.of("YE"), from,
                to, NOW, List.of(), List.of("precursor:" + p.getId()), 0L, stats, Map.of("value", 0.2), List.of(),
                "precursor-watch", "1.0.0", Map.of(), "explanation", false, "conflict", List.of(), List.of(),
                p.getId() + "|x"));
        assertThat(opener.scan()).isEqualTo(1);
        assertThat(opener.scan()).isZero();
        AnalyticFindingEntity finding = findingRows.findAll().get(0);
        assertThat(finding.getQuestionIds()).hasSize(1);
        ForecastQuestionEntity q = ledger.find(finding.getQuestionIds().get(0)).orElseThrow();
        assertThat(q.getOrigin()).isEqualTo("system");
        assertThat(q.getPrecursorId()).isEqualTo(p.getId());
        assertThat(q.getFindingId()).isEqualTo(finding.getId());
        assertThat(q.getResolverType()).isEqualTo("event-exists");
        assertThat(q.getTitle()).startsWith("Will ").contains("YE");
        runner.runFor(q.getId());
        ForecastEntity f = ledger.latestByForecaster(q.getId()).get(ForecasterRegistry.PRECURSOR);
        assertThat(f.getProbability()).isEqualTo(0.42);
        // The base rate is conditional on the precursor's antecedent family.
        ForecastEntity base = ledger.latestByForecaster(q.getId()).get(ForecasterRegistry.BASE_RATE);
        assertThat(base).isNotNull();
        @SuppressWarnings("unchecked")
        Map<String, Object> definition = (Map<String, Object>) base.getInputs().get("definition");
        assertThat(definition).containsKey("condition");
    }

    @Test
    @DisplayName("precursor emits nothing for a question without a precursor-active finding")
    void precursorMissing() {
        ForecastQuestionEntity q = open(Ledger.BINARY, battles("event-exists"), Map.of(), List.of());
        PrecursorForecaster forecaster = new PrecursorForecaster(findingRows);
        assertThat(forecaster.supports(q)).isFalse();
        assertThat(forecaster.forecast(q, NOW, false).skipReason()).contains("precursor-active");
    }

    static SeriesData yearlySeries(SeriesRef ref, Instant asOf, Instant laterKnownAt) {
        List<SeriesValue> points = new ArrayList<>();
        for (LocalDate d = LocalDate.of(2020, 1, 1); d.isBefore(LocalDate.of(2026, 10, 17)); d = d.plusDays(1)) {
            Instant s = d.atStartOfDay(ZoneOffset.UTC).toInstant();
            points.add(new SeriesValue(s, s.plus(Duration.ofDays(1)), (double) (d.getYear() - 2000), s.plus(
                    Duration.ofHours(12)), true, null, null, null));
        }
        if (laterKnownAt != null) {
            Instant s = LocalDate.of(2026, 10, 16).atStartOfDay(ZoneOffset.UTC).toInstant();
            points.add(new SeriesValue(s.plus(Duration.ofDays(1)), s.plus(Duration.ofDays(2)), 99.0, laterKnownAt, true,
                    null, null, null));
        }
        return new SeriesData(ref, Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2026-10-18T00:00:00Z"),
                asOf, 1L, SeriesFrequency.DAY, null, null, null, null, 0, null, null, Set.of(), false, points,
                List.of());
    }

    static Map<String, Object> seriesValue(String ref, Double threshold) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "series-value");
        r.put("ref", ref);
        r.put("period", "2026-11-15");
        r.put("aggregate", "last");
        if (threshold != null) {
            r.put("op", "gt");
            r.put("threshold", threshold);
        }
        return r;
    }

    @Test
    @DisplayName("climatology: quantiles of the same day ±14 days in past years; nothing without history")
    void climatology() {
        String ref = "series:demo/value";
        SeriesReader reader = mock(SeriesReader.class);
        when(reader.read(any(), any(), any(), any())).thenReturn(yearlySeries(SeriesRef.parse(ref), NOW, null));
        ClimatologyForecaster forecaster = new ClimatologyForecaster(reader, registry);
        ForecastQuestionEntity q = open(Ledger.NUMERIC, seriesValue(ref, null), Map.of(), List.of());
        BaselineForecaster.Outcome o = forecaster.forecast(q, NOW, false);
        assertThat(o.skipped()).isFalse();
        // Six past years (2020-2025) × five shifts: values 20…25, each five times.
        assertThat(o.inputs().get("samples")).isEqualTo(30);
        assertThat(o.value().quantiles().get("p50")).isCloseTo(22.5, within(1e-9));
        assertThat(o.value().quantiles().get("p05")).isCloseTo(20.0, within(1e-9));
        when(reader.read(any(), any(), any(), any())).thenReturn(new SeriesData(SeriesRef.parse(ref), NOW, NOW
                .plus(Duration.ofDays(1)), NOW, 1L, SeriesFrequency.DAY, null, null, null, null, 0, null, null,
                Set.of(), false, List.of(), List.of()));
        assertThat(forecaster.forecast(q, NOW, false).skipReason()).contains("comparable past windows");
        ForecastQuestionEntity binary = open(Ledger.BINARY, battles("event-exists"), Map.of(), List.of());
        assertThat(forecaster.supports(binary)).isFalse();
    }

    @Test
    @DisplayName("persistence: the last value known by the forecast time; a later-known value is ignored")
    void persistence() {
        String ref = "series:demo/value";
        SeriesReader reader = mock(SeriesReader.class);
        when(reader.read(any(), any(), any(), any())).thenReturn(yearlySeries(SeriesRef.parse(ref), NOW,
                NOW.plus(Duration.ofHours(1))));
        PersistenceForecaster forecaster = new PersistenceForecaster(reader, mock(ResolutionData.class), registry);
        ForecastQuestionEntity numeric = open(Ledger.NUMERIC, seriesValue(ref, null), Map.of(), List.of());
        BaselineForecaster.Outcome o = forecaster.forecast(numeric, NOW, false);
        assertThat(o.value().quantiles()).hasSize(7).allSatisfy((k, v) -> assertThat(v).isEqualTo(26.0));
        ForecastQuestionEntity binary = open(Ledger.BINARY, seriesValue(ref, 20.0), Map.of(), List.of());
        assertThat(forecaster.forecast(binary, NOW, false).value().probability()).isCloseTo(0.9, within(1e-12));
        when(reader.read(any(), any(), any(), any())).thenReturn(new SeriesData(SeriesRef.parse(ref), NOW, NOW
                .plus(Duration.ofDays(1)), NOW, 1L, SeriesFrequency.DAY, null, null, null, null, 0, null, null,
                Set.of(), false, List.of(), List.of()));
        assertThat(forecaster.forecast(numeric, NOW, false).skipReason()).contains("no known value");
    }

    @Test
    @DisplayName("persistence of a situation state: the current state, if it already satisfies the question")
    void persistenceSituation() {
        ResolutionData data = mock(ResolutionData.class);
        when(data.situationAt(any(), any())).thenReturn(new ResolutionData.SituationState("active", "critical"));
        PersistenceForecaster forecaster = new PersistenceForecaster(mock(SeriesReader.class), data, registry);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "situation-status");
        r.put("situationId", "01JA8Q3V7S2K9W4T6Y1B5N0M3C");
        r.put("minSeverity", "critical");
        ForecastQuestionEntity q = open(Ledger.BINARY, r, Map.of(), List.of());
        assertThat(forecaster.forecast(q, NOW, false).value().probability()).isCloseTo(0.9, within(1e-12));
        when(data.situationAt(any(), any())).thenReturn(null);
        assertThat(forecaster.forecast(q, NOW, false).skipped()).isTrue();
    }

    @Test
    @DisplayName("analog: outcomes from a referenceFamily window or critical severity; nothing in retrospective runs")
    void analog() {
        ForecastQuestionEntity q = open(Ledger.BINARY, battles("event-exists"), Map.of(), List.of());
        assertThat(AnalogForecaster.outcome(q)).isEqualTo("conflict:battles/P28D");
        AnalogForecaster forecaster = runner.baselines().stream().filter(AnalogForecaster.class::isInstance)
                .map(AnalogForecaster.class::cast).findFirst().orElseThrow();
        assertThat(forecaster.forecast(q, NOW, true).skipReason()).contains("past time");
        BaselineForecaster.Outcome live;
        try {
            live = forecaster.forecast(q, NOW, false);
        } catch (ai.foresignal.api.error.ForesignalException e) {
            live = BaselineForecaster.Outcome.skip(e.getMessage());
        }
        if (!live.skipped()) {
            assertThat(live.value().probability()).isBetween(0.0, 1.0);
            assertThat(live.inputs()).containsKeys("analogIds", "analogCount");
        } else {
            assertThat(live.skipReason()).isNotBlank();
        }
    }
}
