package ai.foresignal.manifest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.analytics.finding.AnalyticFindingEntity;
import ai.foresignal.analytics.finding.FindingDraft;
import ai.foresignal.analytics.finding.FindingService;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.backtest.JobMonitor;
import ai.foresignal.forecast.ForecastEntity;
import ai.foresignal.forecast.ForecastInputs;
import ai.foresignal.forecast.ForecastQuestionEntity;
import ai.foresignal.forecast.ForecastService;
import ai.foresignal.forecast.ForecasterRegistry;
import ai.foresignal.forecast.Ledger;
import ai.foresignal.forecast.resolve.ResolverSpec;
import ai.foresignal.intel.IntelEventMemberEntity;
import ai.foresignal.intel.IntelEventMemberRepository;
import ai.foresignal.intel.IntelEventService;
import ai.foresignal.intel.IntelligenceEventEntity;
import ai.foresignal.intel.IntelligenceEventRepository;
import ai.foresignal.precursor.PrecursorBatchEntity;
import ai.foresignal.precursor.PrecursorMiner;
import ai.foresignal.security.Ulids;
import ai.foresignal.situation.LinkerInputReader;
import ai.foresignal.situation.SituationLinker;
import ai.foresignal.situation.SituationSnapshots;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.stream.ChangeBus;
import ai.foresignal.test.IntegrationTest;
import ai.foresignal.test.MutableClock;
import ai.foresignal.testsupport.analytics.AnalyticsTestSources;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.testsupport.reference.ReferenceFixtures;
import ai.foresignal.trigger.TriggerEngine;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.window.ActivitySampleEntity;
import ai.foresignal.trigger.window.ActivitySampleRepository;
import ai.foresignal.trigger.window.AnomalyObservationEntity;
import ai.foresignal.trigger.window.AnomalyObservationRepository;
import ai.foresignal.trigger.window.WindowEngine;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * MAN-1 across the analytic outputs of prompts 100–123 (checkpoint 124, focus area 5) and the outlook tables of 126.
 *
 * <ul>
 *   <li>Every column named {@code manifest_id}, {@code last_manifest_id} or {@code brief_manifest_id} in the schema is
 *       listed in {@link #COVERAGE} with the test that shows its writer fills it, so a new analytic table cannot slip
 *       in without one; the writers this test runs fill it on every row, and every stored id names a stored manifest.
 *   <li>Every registered {@link ManifestReplayer} replays a deterministic manifest of its component
 *       {@code identical}: the 100 anomaly evaluation, the 101/116 event scorer and a 103 backtest job.
 * </ul>
 *
 * <p>The comparison itself is covered by {@code ManifestReplayCheckTest}, the differs and not-replayable outcomes by
 * {@code ManifestReplayIT}.
 */
@IntegrationTest
@Import(AnalyticsTestSources.class)
class ManifestReplayCheckIT {

    private static final Instant NOW = Instant.parse("2026-03-01T12:03:00Z");
    private static final Instant CURRENT = Instant.parse("2026-03-01T11:00:00Z");
    private static final String SOURCE = TestIngestSources.STANDARD;
    private static final String INTEL_SOURCE = "it-manifest-intel";

    /**
     * Every manifest column in the schema and where its writer is shown to fill it: {@code here} for the writers this
     * test runs, else the test (or the reason no deterministic manifest applies).
     */
    static final Map<String, String> COVERAGE = new TreeMap<>(Map.ofEntries(
            Map.entry("analytic_finding.manifest_id", "here"),
            Map.entry("anomaly_observation.manifest_id", "here"),
            Map.entry("backtest_job.manifest_id", "here"),
            Map.entry("forecast.manifest_id", "here"),
            Map.entry("forecast_question.manifest_id", "here"),
            Map.entry("intelligence_event.manifest_id", "here"),
            Map.entry("precursor_batch.manifest_id", "here"),
            Map.entry("situation.manifest_id", "here"),
            Map.entry("situation_snapshot.manifest_id", "here"),
            Map.entry("base_rate.manifest_id", "BaseRateControllersIT (a stored rate carries its manifestId)"),
            Map.entry("benchmark_run.manifest_id", "BenchmarkIsolationIT and BenchmarkControllersIT"),
            Map.entry("precursor.manifest_id", "PrecursorValidationIT (a validated precursor carries its manifest)"),
            Map.entry("observability_estimate.manifest_id", "ObservabilityEstimator writes the run's manifest with"
                    + " every row; AbsenceRetrofitIT plants rows by hand"),
            Map.entry("analyst_run.manifest_id", "LLM run: inputs recorded, output not reproducible (ManifestReplayIT"
                    + " llmManifest)"),
            Map.entry("situation.brief_manifest_id", "LLM brief: inputs recorded, output not reproducible"),
            Map.entry("correlation_recipe.last_manifest_id", "run bookkeeping of a recipe, not an output row"),
            // 126: series forecasts, outlooks and indices.
            Map.entry("series_forecast.manifest_id", "SeriesForecastIT (every forecast row carries its manifest)"),
            Map.entry("series_forecast_model.manifest_id", "SeriesForecastIT (the method selection's manifest)"),
            Map.entry("regional_outlook.manifest_id", "RegionalOutlookIT (every published outlook)"),
            Map.entry("regional_outlook_model.manifest_id", "RegionalOutlookService.refit writes each fit's manifest"
                    + " (RegionalOutlookIT publishes after a refit)"),
            Map.entry("regional_index_value.manifest_id", "RegionalIndexService.computeDays writes one manifest per"
                    + " computation; IndexFitIT plants rows by hand"),
            Map.entry("regional_index_fit.manifest_id", "IndexFitIT (a finished fit carries its manifest)"),
            // 128: analysis cycles.
            Map.entry("assessment.manifest_id", "CycleRunIT (a digest-only assessment carries its manifest)"),
            Map.entry("cycle_run.manifest_id", "CycleRunner gives the run its assessment's manifest"),
            // 132: reports.
            Map.entry("report.manifest_id", "ReportPublisher.publish writes the publication's manifest"),
            Map.entry("report_revision.manifest_id", "ReportPublisher.publish writes it with each revision"),
            // 136/137: social narratives, claims, comparisons and cues.
            Map.entry("social_narrative.manifest_id", "NarrativeClusterer records the clustering manifest"),
            Map.entry("social_claim.manifest_id", "LlmClaimExtractionTest (a stored claim carries the extraction"
                    + " manifest)"),
            Map.entry("social_comparison.manifest_id", "ComparisonService records the evaluation's manifest"),
            Map.entry("social_comparison_revision.manifest_id", "ComparisonService writes it with each revision"),
            Map.entry("social_cue.manifest_id", "SocialCueService records the cue's manifest")));

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired ActivitySampleRepository samples;
    @Autowired AnomalyObservationRepository observations;
    @Autowired SourceService sources;
    @Autowired SourceRegistry registry;
    @Autowired TestRecords records;
    @Autowired CoverageSupport coverage;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired ManifestService manifests;
    @Autowired ManifestReplayService replays;
    @Autowired List<ManifestReplayer> replayers;
    @Autowired BacktestJobService jobs;
    @Autowired IntelEventService intelEvents;
    @Autowired IntelligenceEventRepository intelRepository;
    @Autowired IntelEventMemberRepository intelMembers;
    @Autowired FindingService findings;
    @Autowired ForecastService ledger;
    @Autowired ForecasterRegistry forecasters;
    @Autowired SituationLinker linker;
    @Autowired SituationSnapshots snapshots;
    @Autowired PrecursorMiner miner;
    @Autowired ChangeBus changeBus;
    @Autowired TriggerEngine triggers;
    @Autowired ApplicationContext context;

    @BeforeEach
    void freeze() {
        clock.setInstant(NOW);
        sources.update(SOURCE, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null,
                false));
    }

    @AfterEach
    void unregister() {
        if (registry.contains(INTEL_SOURCE)) {
            registry.unregisterDynamic(INTEL_SOURCE);
        }
    }

    // ------------------------------------------------------------------------- writers

    /** A 100 anomaly evaluation of an activity-up bucket (as {@code ManifestReplayIT} sets it up). */
    AnomalyObservationEntity anomaly() {
        UUID rule = rules.create(json.readTree("{\"name\":\"unusual\",\"type\":\"anomaly\",\"sources\":[\"" + SOURCE
                + "\"],\"window\":{\"window\":\"1h\",\"direction\":\"both\",\"minWeeks\":8}}")).rule().getId();
        List<ActivitySampleEntity> batch = new ArrayList<>();
        for (Instant bucket = CURRENT.minus(Duration.ofDays(56)); bucket.isBefore(CURRENT);
                bucket = bucket.plus(Duration.ofHours(1))) {
            ActivitySampleEntity sample = new ActivitySampleEntity(rule, "*", bucket, 10);
            sample.setCoverageOk(true);
            sample.setCoverageRatio(1.0);
            batch.add(sample);
        }
        samples.saveAll(batch);
        coverage.fetch(SOURCE, "OK", CURRENT.minus(Duration.ofDays(1)));
        coverage.markCovered(SOURCE, CURRENT, CURRENT.plus(Duration.ofHours(1)));
        Instant at = CURRENT.plus(Duration.ofMinutes(30));
        clock.setInstant(at);
        for (int i = 0; i < 60; i++) {
            records.ingestAt(SOURCE, at, NormalizedRecord.builder("x" + i, TestIngestSources.STANDARD_KIND)
                    .observedAt(at).put("name", "x" + i).put("size", i).build());
        }
        clock.setInstant(NOW);
        engine.evaluate(NOW, new WindowEngine.Emitter() {
            @Override
            public void emit(NormalizedRecord record) {
                // the replay reads the stored observation
            }

            @Override
            public void tombstone(String kind, String externalId) {
                // nothing to retire
            }
        });
        AnomalyObservationEntity row = observations.findTop200ByRuleIdOrderByIdDesc(rule).get(0);
        assertThat(row.getClassification()).isEqualTo("activity-up");
        return row;
    }

    /** An intelligence event scored over three of the ingested records and stored. */
    IntelligenceEventEntity scoredEvent() {
        IntelligenceEventEntity event = new IntelligenceEventEntity(Ulids.next(NOW), "maritime-disruption",
                "Shipping disruption", NOW.minus(Duration.ofHours(1)));
        event.setCreatedAt(NOW);
        event.setUpdatedAt(NOW);
        for (int i = 0; i < 3; i++) {
            long seq = jdbc.queryForObject("SELECT max(seq) FROM record_version WHERE source_id = ? AND external_id"
                    + " = ?", Long.class, SOURCE, "x" + i);
            IntelEventMemberEntity m = new IntelEventMemberEntity(event.getId(), "record", SOURCE, "x" + i, seq);
            m.setSignalFamily(i == 0 ? "ais-behaviour" : "conflict");
            m.setWeight(0.6 + i * 0.1);
            m.setAddedAt(NOW);
            m.setAddedBy("test");
            intelMembers.save(m);
        }
        intelMembers.flush();
        intelEvents.rescore(event);
        return intelRepository.saveAndFlush(event);
    }

    /** A finished 103 rule backtest over the ingested records. */
    BacktestJobEntity backtest() {
        UUID ruleId = rules.create(json.readTree("{\"name\":\"big things\",\"sources\":[\"" + SOURCE + "\"],"
                + "\"kinds\":[\"" + TestIngestSources.STANDARD_KIND + "\"],\"mode\":\"match\",\"conditions\":[{\"path\":"
                + "\"size\",\"op\":\">=\",\"value\":50}]}")).rule().getId();
        BacktestJobEntity job = jobs.submit("rule", ruleId.toString(), null, json.readTree("{\"fromSeq\":0}"), null,
                null);
        jobs.run(job.getId());
        long deadline = System.currentTimeMillis() + 60_000;
        BacktestJobEntity done = jobs.get(job.getId());
        while (List.of("queued", "running").contains(done.getStatus()) && System.currentTimeMillis() < deadline) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            done = jobs.get(job.getId());
        }
        assertThat(done.getStatus()).isEqualTo("done");
        return done;
    }

    AnalyticFindingEntity finding() {
        return findings.record(new FindingDraft("change-point", "Change point conflict", "notable", null,
                List.of("YE"), NOW.minus(Duration.ofHours(2)), NOW.minus(Duration.ofHours(1)), NOW, List.of(),
                List.of(), 1, Map.of("i", 1), Map.of("value", 1.0, "unit", "alarms-per-year", "method",
                        "detector-false-alarm-rate", "text", "t"), List.of(), "changepoint-cusum", "1.0.0", Map.of(),
                "x", false, "conflict", List.of(), List.of(), "cp-manifest")).finding();
    }

    ForecastEntity forecast() {
        forecasters.registerBuiltIns();
        ForecastInputs.QuestionDraft draft = new ForecastInputs.QuestionDraft("Will the ceasefire hold?",
                Ledger.BINARY, Map.of(), "Yes if no party announces its end.",
                ResolverSpec.validate(Map.of("type", "manual"), Ledger.BINARY), NOW.minus(Duration.ofHours(1)),
                NOW.plus(Duration.ofDays(3)), NOW.plus(Duration.ofDays(4)), "conflict", List.of(), Map.of(), null,
                false);
        ForecastQuestionEntity q = ledger.openQuestion(draft, "agent", "test").question();
        return ledger.record(q, forecasters.ensure("operator:manifest", "operator", "Operator", "1.0.0", Map.of()),
                ForecastInputs.ForecastValue.binary(0.4), "Test.", List.of(), List.of(), Map.of(), null, false);
    }

    /** A critical intelligence event the 118 linker turns into a situation, and the day's snapshot of it. */
    void situationAndSnapshot() {
        ReferenceFixtures.loadAll(context);
        registry.registerDynamic(SourceDescriptor.builder(INTEL_SOURCE).name("Manifest test intel")
                .kind(LinkerInputReader.INTEL_KIND).mode(Mode.PUSH).defaultBudget(Budget.of(60, 10_000)).build());
        records.ingestAt(INTEL_SOURCE, NOW, NormalizedRecord.builder("ie1", LinkerInputReader.INTEL_KIND)
                .observedAt(NOW).point(-96.80, 32.78).put("type", "maritime-disruption").put("status", "active")
                .put("severity", "critical").put("signalFamilies", List.of("ais-behaviour")).put("title", "Event ie1")
                .put("automated", true).build());
        long target = changeBus.lastPublishedSeq();
        long deadline = System.currentTimeMillis() + 10_000;
        while (triggers.running() && triggers.cursor() < target && System.currentTimeMillis() < deadline) {
            Thread.onSpinWait();
        }
        linker.catchUp();
        assertThat(jdbc.queryForObject("SELECT count(*) FROM situation", Long.class)).as("a seeded situation")
                .isPositive();
        snapshots.snapshot(LocalDate.ofInstant(NOW, ZoneOffset.UTC));
        assertThat(jdbc.queryForObject("SELECT count(*) FROM situation_snapshot", Long.class)).isPositive();
    }

    // ------------------------------------------------------------------------- tests

    @Test
    @DisplayName("every manifest column is accounted for; the writers fill it on every row; every id is stored")
    void outputTablesCarryManifests() {
        AnomalyObservationEntity anomaly = anomaly();
        assertThat(anomaly.getManifestId()).isNotNull();
        assertThat(scoredEvent().getManifestId()).isNotNull();
        assertThat(backtest().getManifestId()).isNotNull();
        assertThat(finding().getManifestId()).isNotNull();
        assertThat(forecast().getManifestId()).isNotNull();
        situationAndSnapshot();
        PrecursorBatchEntity batch = miner.run("mine", "test", null, JobMonitor.NONE);
        assertThat(batch.getManifestId()).isNotNull();

        List<Map<String, Object>> columns = jdbc.queryForList("SELECT table_name, column_name FROM"
                + " information_schema.columns WHERE table_schema = current_schema() AND column_name IN"
                + " ('manifest_id', 'last_manifest_id', 'brief_manifest_id') ORDER BY 1, 2");
        TreeSet<String> found = new TreeSet<>();
        for (Map<String, Object> c : columns) {
            found.add(c.get("table_name") + "." + c.get("column_name"));
        }
        assertThat(found).as("every manifest column has a writer listed in COVERAGE")
                .containsExactlyElementsOf(COVERAGE.keySet());

        Map<String, String> problems = new LinkedHashMap<>();
        for (String column : found) {
            String table = column.substring(0, column.indexOf('.'));
            String name = column.substring(column.indexOf('.') + 1);
            Long dangling = jdbc.queryForObject("SELECT count(*) FROM " + table + " t WHERE t." + name + " IS NOT NULL"
                    + " AND NOT EXISTS (SELECT 1 FROM analysis_manifest m WHERE m.id = t." + name + ")", Long.class);
            if (dangling != null && dangling > 0) {
                problems.put(column, dangling + " ids name no stored manifest");
            }
            if (!"here".equals(COVERAGE.get(column))) {
                continue;
            }
            Long rows = jdbc.queryForObject("SELECT count(*) FROM " + table, Long.class);
            Long missing = jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE " + name + " IS NULL",
                    Long.class);
            if (rows == null || rows == 0) {
                problems.put(column, "no rows were written");
            } else if (missing != null && missing > 0) {
                problems.put(column, missing + " of " + rows + " rows have no manifest");
            }
        }
        assertThat(problems).as("MAN-1").isEmpty();
    }

    @Test
    @DisplayName("every registered replayer replays a deterministic manifest of its component identical")
    void everyReplayerReplaysIdentical() {
        Map<String, String> sample = new LinkedHashMap<>();
        sample.put("anomaly", anomaly().getManifestId());
        sample.put("event-scorer", scoredEvent().getManifestId());
        sample.put("backtest", backtest().getManifestId());
        List<String> components = new ArrayList<>();
        sample.forEach((what, id) -> {
            assertThat(id).as(what).isNotNull();
            ManifestService.Manifest manifest = manifests.get(id);
            components.add(manifest.component());
            ReplayOutcome outcome = replays.replay(id);
            assertThat(outcome.outcome()).as(what + " (" + manifest.component() + "): " + outcome.diffSummary() + " "
                    + outcome.message()).isEqualTo(ReplayOutcome.IDENTICAL);
        });
        for (ManifestReplayer replayer : replayers) {
            assertThat(components).as(replayer.getClass().getSimpleName() + " has a sample here")
                    .anyMatch(replayer::supports);
        }
    }
}
