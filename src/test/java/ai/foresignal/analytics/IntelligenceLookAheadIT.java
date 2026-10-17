package ai.foresignal.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.eventstudy.LiveEventStudyService;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.analytics.registry.AnalysisSeriesService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.baserate.BaseRateDefinition;
import ai.foresignal.baserate.BaseRateService;
import ai.foresignal.baserate.RegionClasses;
import ai.foresignal.baserate.analog.AnalogService;
import ai.foresignal.baserate.analog.SignatureService;
import ai.foresignal.baserate.analog.SituationOutcomeEntity;
import ai.foresignal.baserate.analog.SituationOutcomeRepository;
import ai.foresignal.baserate.analog.SituationSignatureEntity;
import ai.foresignal.baserate.analog.SituationSignatureRepository;
import ai.foresignal.baserate.reference.ReferenceFamilies;
import ai.foresignal.baserate.reference.ReferenceImportEntity;
import ai.foresignal.baserate.reference.ReferenceImportRepository;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.eval.EvaluationService;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.intel.ForesignalIntelGenerator;
import ai.foresignal.precursor.Candidate;
import ai.foresignal.precursor.PrecursorEntity;
import ai.foresignal.precursor.PrecursorLiveWatch;
import ai.foresignal.precursor.PrecursorRepository;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.SituationEntity;
import ai.foresignal.situation.SituationMemberEntity;
import ai.foresignal.situation.SituationMemberRepository;
import ai.foresignal.situation.SituationRepository;
import ai.foresignal.source.series.SeriesMeta;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.analytics.AnalyticsItSupport;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/**
 * No look-ahead on the database paths of prompts 119–122 (checkpoint 124, focus area 3; invariant INT-2): data
 * recorded after the decision time, or known only after it, changes nothing.
 *
 * <ul>
 *   <li>Analog search (121): a situation that began after the decision time, with its signature and an outcome
 *       recorded later, is no analog and does not move the outcome distribution or its base rate.
 *   <li>Base rates as of a time (121/122 {@code computeAsOf}): a country first seen after the cutoff, and coverage
 *       imported for later years, change nothing.
 *   <li>Retrospective ledger evaluation (122): a situation's state is read as of the question's opening, so a later
 *       status change does not change the persistence forecast's score.
 *   <li>The live precursor watch (120) and the live pre-event check (119) ignore points whose {@code knownAt} is after
 *       the decision time.
 * </ul>
 *
 * <p>The pure forms of these components are covered by {@link LookAheadTest}.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class IntelligenceLookAheadIT extends AnalyticsItSupport {

    @Autowired SituationRepository situations;
    @Autowired SituationMemberRepository members;
    @Autowired SignatureService signatures;
    @Autowired SituationSignatureRepository signatureRows;
    @Autowired SituationOutcomeRepository outcomeRows;
    @Autowired AnalogService analogs;
    @Autowired RegionClasses regionClasses;
    @Autowired BaseRateService baseRates;
    @Autowired ReferenceImportRepository imports;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry registry;
    @Autowired EvaluationService evaluations;
    @Autowired PrecursorRepository precursors;
    @Autowired PrecursorLiveWatch watch;
    @Autowired LiveEventStudyService live;
    @Autowired AnalysisSeriesService seriesRegistry;
    @Autowired FindingService findings;
    @Autowired JsonMapper json;

    // ------------------------------------------------------------------------- analogs

    static final Instant DECISION = Instant.parse("2026-06-01T12:00:00Z");

    String situation(String title, Instant started, Instant closed, String status, String severity) {
        String id = Ulids.next(started);
        SituationEntity s = new SituationEntity(id, title, "conflict", started);
        s.setStatus(status);
        s.setSeverity(severity);
        s.setRegionCodes(List.of("YE"));
        s.setLastActivityAt(closed != null ? closed : started.plus(Duration.ofHours(6)));
        s.setClosedAt(closed);
        s.setSummary(title);
        s.setCreatedAt(started);
        s.setUpdatedAt(closed != null ? closed : started);
        situations.save(s);
        return id;
    }

    void members(String situationId, Instant at, String family, int n) {
        for (int i = 0; i < n; i++) {
            Instant when = at.plus(Duration.ofHours(1 + i));
            SituationMemberEntity m = new SituationMemberEntity(Ulids.next(when), situationId, "record",
                    "test/" + situationId + "/" + family + "/" + i, (long) i + 1);
            m.setSignalFamily(family);
            m.setObservedAt(when);
            m.setAddedAt(when);
            m.setAddedBy("test");
            m.setRegionCodes(List.of("YE"));
            members.save(m);
        }
    }

    @Test
    @DisplayName("analogs: a situation begun after the decision time, and its later outcome, change nothing")
    void analogsIgnoreLaterSituations() {
        clock.setInstant(DECISION);
        signatures.clearCache();
        regionClasses.reset();
        String critical = situation("Clashes near Marib", DECISION.minus(Duration.ofDays(200)),
                DECISION.minus(Duration.ofDays(190)), "closed", "critical");
        members(critical, DECISION.minus(Duration.ofDays(200)), "conflict", 4);
        String calm = situation("Fighting in Taiz", DECISION.minus(Duration.ofDays(150)),
                DECISION.minus(Duration.ofDays(140)), "closed", "info");
        members(calm, DECISION.minus(Duration.ofDays(150)), "conflict", 3);
        String target = situation("Escalation in Hodeidah", DECISION.minus(Duration.ofDays(2)), null, "active",
                "warning");
        members(target, DECISION.minus(Duration.ofDays(2)), "conflict", 3);
        jdbc.update("INSERT INTO situation_log (situation_id, at, actor, action, event) VALUES (?, ?, 'linker',"
                + " 'status', 'situation.activated')", target, Timestamp.from(DECISION.minus(Duration.ofDays(2))));
        signatures.computeDue();
        Map<String, Object> before = analogs.forSituation(target, 10, "critical", false);
        assertThat(ids(before)).containsExactlyInAnyOrder(critical, calm);

        // A situation that began after the decision time (as a replay or a skewed clock would see it), with the
        // signature of the critical analog and a critical outcome recorded later still.
        Instant later = DECISION.plus(Duration.ofDays(5));
        String future = situation("Later clashes", later, later.plus(Duration.ofDays(5)), "closed", "critical");
        members(future, later, "conflict", 4);
        SituationSignatureEntity copy = signatureRows.findBySituationIdOrderByDayNAsc(critical).get(0);
        signatureRows.save(new SituationSignatureEntity(future, copy.getDayN(), copy.signature(), null,
                later.plus(Duration.ofDays(1))));
        SituationOutcomeEntity outcome = new SituationOutcomeEntity(future);
        outcome.setDurationDays(5.0);
        outcome.setPeakSeverity("critical");
        outcome.setReferenceAt(later.plus(Duration.ofDays(5)));
        outcome.setFollowOn7d(List.of());
        outcome.setFollowOn30d(List.of());
        outcome.setFinalOutcome(true);
        outcome.setComputedAt(later.plus(Duration.ofDays(40)));
        outcomeRows.save(outcome);

        Map<String, Object> after = analogs.forSituation(target, 10, "critical", false);

        assertThat(ids(after)).doesNotContain(future).containsExactlyElementsOf(ids(before));
        assertThat(after.get("countFound")).isEqualTo(before.get("countFound"));
        assertThat(after.get("outcomeDistribution")).isEqualTo(before.get("outcomeDistribution"));
        assertThat(after.get("analogs")).isEqualTo(before.get("analogs"));
    }

    @SuppressWarnings("unchecked")
    static List<Object> ids(Map<String, Object> result) {
        return ((List<Map<String, Object>>) result.get("analogs")).stream().map(a -> a.get("situationId")).toList();
    }

    // ------------------------------------------------------------------------- base rates as of a time

    static final Instant AS_OF = Instant.parse("2024-12-18T00:00:00Z");

    ReferenceImportEntity imported(String dataset, String key, List<String> done) {
        ReferenceImportEntity e = new ReferenceImportEntity(Ulids.next(DECISION), dataset, key, "test", DECISION);
        e.setStatus("done");
        e.setLicense("test");
        e.setEgress("OPEN");
        e.setProgress(Map.of("done", done));
        return imports.save(e);
    }

    void history(String family, String region, LocalDate week, String source, long count, String importId) {
        jdbc.update("INSERT INTO reference_history (family, region_code, week_start, source, count, fatalities,"
                + " licence, egress, import_id) VALUES (?, ?, ?, ?, ?, NULL, 'test', 'OPEN', ?)", family, region,
                Date.valueOf(week), source, count, importId);
    }

    void weeks(String family, String region, int year, String source, long count, int every, String importId) {
        LocalDate[] chunk = ReferenceFamilies.yearChunk(year);
        int i = 0;
        for (LocalDate w = chunk[0]; w.isBefore(chunk[1]); w = w.plusWeeks(1), i++) {
            if (i % every == 0) {
                history(family, region, w, source, count, importId);
            }
        }
    }

    @Test
    @DisplayName("base rates as of a time: a country first seen later, and later coverage, change nothing")
    void baseRateAsOfIgnoresLaterHistory() {
        clock.setInstant(DECISION);
        baseRates.clearCache();
        // ACLED is imported by country-year: Yemen for 2023 and 2024.
        ReferenceImportEntity acled = imported("acled", "k-acled", List.of("YE:2023", "YE:2024"));
        weeks(ReferenceFamilies.BATTLES, "YE", 2023, ReferenceFamilies.ACLED, 3, 2, acled.getId());
        weeks(ReferenceFamilies.BATTLES, "YE", 2024, ReferenceFamilies.ACLED, 3, 2, acled.getId());
        BaseRateDefinition worldwide = BaseRateDefinition.parse(Map.of("family", ReferenceFamilies.BATTLES,
                "window", "P7D"));
        BaseRateService.Computed first = baseRates.computeAsOf(worldwide, AS_OF);
        assertThat(first.result().nWindows()).isPositive();

        // Saudi Arabia appears only in a later import (2025): before the cutoff it was never part of the class.
        ReferenceImportEntity laterCountry = imported("acled", "k-acled-sa", List.of("SA:2025"));
        weeks(ReferenceFamilies.BATTLES, "SA", 2025, ReferenceFamilies.ACLED, 9, 1, laterCountry.getId());
        BaseRateService.Computed second = baseRates.computeAsOf(worldwide, AS_OF);
        assertThat(second.result()).usingRecursiveComparison().isEqualTo(first.result());

        // Longest-coverage-first ordering (a family without a priority) must not change with coverage of later years.
        String family = "test:lookahead";
        ReferenceImportEntity ucdp = imported("ucdp-ged", "k-ucdp", List.of("2022", "2023", "2024"));
        ReferenceImportEntity usgs = imported("usgs-comcat", "k-usgs", List.of("2023", "2024"));
        for (int year = 2022; year <= 2024; year++) {
            weeks(family, "YE", year, ReferenceFamilies.UCDP, 2, 1, ucdp.getId());
        }
        BaseRateDefinition test = BaseRateDefinition.parse(Map.of("family", family, "regionCodes", List.of("YE"),
                "window", "P7D"));
        BaseRateService.Computed ordered = baseRates.computeAsOf(test, AS_OF);
        assertThat(ordered.result().pAtLeastOne()).isEqualTo(1.0);
        imported("usgs-comcat", "k-usgs-later", List.of("2025", "2026", "2027"));
        BaseRateService.Computed reordered = baseRates.computeAsOf(test, AS_OF);
        assertThat(reordered.result()).usingRecursiveComparison().isEqualTo(ordered.result());
        assertThat(usgs.getId()).isNotNull();
    }

    // ------------------------------------------------------------------------- retrospective evaluation

    static final Instant OPENED = Instant.parse("2025-01-06T00:00:00Z");

    @Test
    @DisplayName("retrospective evaluation: a situation's later status change does not change the persistence score")
    void retrospectiveReadsSituationAsOfOpening() {
        clock.setInstant(Instant.parse("2026-10-16T12:00:00Z"));
        registry.registerBuiltIns();
        String s = situation("Standoff at the port", Instant.parse("2024-12-01T00:00:00Z"), null, "active", "warning");
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("type", "situation-status");
        r.put("situationId", s);
        r.put("status", "active");
        ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("Still active?", Ledger.BINARY,
                Map.of(), "Yes if the situation is active at resolve_by.", ResolverSpec.validate(r, Ledger.BINARY),
                OPENED, OPENED.plus(Duration.ofDays(1)), OPENED.plus(Duration.ofDays(28)), "conflict", List.of(),
                Map.of(), null, false);
        ForecastQuestionEntity q = ledger.openQuestion(draft, "agent", "test").question();
        Map<String, Object> resolution = new LinkedHashMap<>();
        resolution.put("outcome", "yes");
        resolution.put("value", null);
        resolution.put("resolvedAt", OPENED.plus(Duration.ofDays(28)).toString());
        resolution.put("resolvedBy", "resolver:situation-status/1.0.0");
        resolution.put("early", false);
        resolution.put("evidence", List.of("situation:" + s));
        ledger.finishResolution(q, resolution);
        Map<String, Object> definition = new LinkedHashMap<>();
        definition.put("forecasterId", ForecasterRegistry.PERSISTENCE);
        definition.put("from", "2025-01-01T00:00:00Z");
        definition.put("to", "2026-01-01T00:00:00Z");
        definition.put("categories", List.of());
        definition.put("maxQuestions", null);
        definition.put("knowledgeCutoff", null);
        Map<String, Object> first = evaluations.run(definition, JobMonitor.NONE);
        assertThat(first.get("questionsEvaluated")).isEqualTo(1);
        Object brier = entry(first).get("brier");
        assertThat(brier).isNotNull();

        // Months after the question opened the situation closed; the row now says closed.
        Instant closedAt = OPENED.plus(Duration.ofDays(60));
        jdbc.update("UPDATE situation SET status = 'closed', severity = 'info', closed_at = ? WHERE id = ?",
                Timestamp.from(closedAt), s);
        jdbc.update("INSERT INTO situation_log (situation_id, at, actor, action, event, before, after) VALUES (?, ?,"
                + " 'linker', 'status', 'situation.closed', CAST(? AS jsonb), CAST(? AS jsonb))", s,
                Timestamp.from(closedAt), "{\"status\":\"active\",\"severity\":\"warning\"}",
                "{\"status\":\"closed\",\"severity\":\"info\"}");

        Map<String, Object> second = evaluations.run(definition, JobMonitor.NONE);

        assertThat(entry(second).get("brier")).isEqualTo(brier);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> entry(Map<String, Object> result) {
        Map<String, Object> card = (Map<String, Object>) result.get("scorecard");
        return ((List<Map<String, Object>>) card.get("entries")).get(0);
    }

    // ------------------------------------------------------------------------- live precursor watch

    @Test
    @DisplayName("the live precursor watch ignores a series point known only after the decision time")
    void precursorWatchIgnoresLaterKnowledge() {
        String ref = "series:" + AnalyticsTestSources.SERIES + "/pw";
        double[] values = new double[130];
        for (int i = 0; i < values.length; i++) {
            values[i] = 100 + (i % 3);
        }
        seedDaily(AnalyticsTestSources.SERIES, "pw", T0, values, SeriesMeta.of(SeriesMeta.PRICE,
                SeriesMeta.LOG_RETURN, 1));
        Instant decision = T0.plus(Duration.ofDays(130)).plus(Duration.ofHours(12));
        clock.setInstant(decision);
        PrecursorEntity p = new PrecursorEntity(Ulids.next(decision), "key-series-pw", Candidate.SERIES, decision);
        Map<String, Object> antecedent = new LinkedHashMap<>();
        antecedent.put("seriesRef", ref);
        antecedent.put("family", "markets");
        antecedent.put("label", "moves in pw");
        Map<String, Object> consequent = new LinkedHashMap<>();
        consequent.put("selector", Map.of("unexplainedMove", Map.of("family", "markets"), "regionCodes",
                List.of("YE")));
        consequent.put("family", "markets");
        consequent.put("label", "unexplained moves in markets series in YE");
        p.setAntecedent(antecedent);
        p.setConsequent(consequent);
        p.setSpatial(Map.of("rule", "none"));
        p.setRegionCodes(List.of("YE"));
        p.setFamilies(List.of("markets"));
        p.setLagMinSeconds(0);
        p.setLagMaxSeconds(Duration.ofDays(3).toSeconds());
        p.setBestLagSeconds(Duration.ofDays(1).toSeconds());
        p.setHits(8);
        p.setNAntecedent(10);
        p.setHitRate(0.8);
        p.setBaseRate(0.2);
        p.setLift(4.0);
        p.setBatchSize(50);
        p.setStatus("validated");
        p.setDescription("pw moves were followed by unexplained moves.");
        p.setUpdatedAt(decision);
        precursors.save(p);
        assertThat(watch.scanSeries(decision)).as("a calm series").isZero();

        // A revision of the last bucket to a large move, published an hour after the decision time.
        Instant published = decision.plus(Duration.ofHours(1));
        Instant lastDay = T0.plus(Duration.ofDays(129));
        seed(AnalyticsTestSources.SERIES, published, dailyPoint("pw", lastDay, 500, published));

        assertThat(watch.scanSeries(decision)).as("the revision is not known at the decision time").isZero();
        assertThat(active()).isZero();
        assertThat(watch.scanSeries(published.plus(Duration.ofHours(1)))).as("once known, it counts").isEqualTo(1);
        assertThat(active()).isEqualTo(1);
    }

    long active() {
        return findings.list(new FindingService.ListQuery(List.of("precursor-active"), List.of(), List.of(), List.of(),
                null, null, null, null, 50), true).items().size();
    }

    // ------------------------------------------------------------------------- live pre-event check

    static final Instant EVENT_DAY = Instant.parse("2026-03-02T00:00:00Z");

    /** A seeded random-walk price for {@code days} days ending the day before {@link #EVENT_DAY}. */
    static double[] prices(int days, double runUp) {
        Random random = new Random(119);
        double[] out = new double[days];
        double v = 70;
        for (int i = 0; i < days; i++) {
            v *= Math.exp(0.01 * random.nextGaussian() + (i >= days - 5 ? runUp : 0));
            out[i] = v;
        }
        return out;
    }

    void seedPrices(String seriesId, double[] values, int fromIndex, Instant publishedAt) {
        List<NormalizedRecord> batch = new ArrayList<>();
        Instant start = EVENT_DAY.minus(Duration.ofDays(values.length));
        for (int i = fromIndex; i < values.length; i++) {
            batch.add(dailyPoint(seriesId, start.plus(Duration.ofDays(i)), values[i], publishedAt));
        }
        batch.add(latest(seriesId, "P1D", values[values.length - 1], publishedAt, SeriesMeta.of(SeriesMeta.PRICE,
                SeriesMeta.LOG_RETURN, 1)));
        seed(AnalyticsTestSources.SERIES, publishedAt, batch.toArray(NormalizedRecord[]::new));
    }

    @Test
    @DisplayName("the live pre-event check ignores a run-up revised in after the event (knownAt after the decision)")
    void preEventCheckIgnoresLaterRevision() {
        int days = 700;
        seedPrices("rev", prices(days, 0), 0, EVENT_DAY.minus(Duration.ofHours(1)));
        seriesRegistry.upsert(json.readTree("{\"ref\":\"series:" + AnalyticsTestSources.SERIES + "/rev\","
                + "\"frequency\":\"day\",\"watchPairs\":[{\"eventFamily\":\"ais-behaviour\",\"regionCodes\":[],"
                + "\"minEffect\":2.0}]}"));
        live.rewind(jdbc.queryForObject("SELECT max(seq) FROM record_version", Long.class));
        Instant eventAt = EVENT_DAY.plus(Duration.ofHours(12));
        seed(ForesignalIntelGenerator.ID, eventAt, NormalizedRecord.builder("la1", ForesignalIntelGenerator.KIND)
                .observedAt(eventAt).put("type", "maritime-disruption").put("severity", "warning")
                .put("status", "active").put("title", "Shipping disruption la1")
                .put("signalFamilies", List.of("ais-behaviour")).build());
        // The run-up the preEventSignal test detects, but published a day after the event.
        seedPrices("rev", prices(days, 0.03), days - 5, EVENT_DAY.plus(Duration.ofDays(1)));
        clock.setInstant(EVENT_DAY.plus(Duration.ofDays(2)));

        LiveEventStudyService.PollResult result = live.poll();

        assertThat(result.events()).isEqualTo(1);
        assertThat(result.findings()).isZero();
        assertThat(findings.list(new FindingService.ListQuery(List.of("pre-event-signal"), List.of(), List.of(),
                List.of(), null, null, null, null, 50), true).items()).isEmpty();
    }
}
