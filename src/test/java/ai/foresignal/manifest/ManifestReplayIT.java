package ai.foresignal.manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import ai.foresignal.api.error.ConflictException;
import ai.foresignal.backtest.BacktestJobEntity;
import ai.foresignal.backtest.BacktestJobService;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.CoverageSupport;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import ai.foresignal.trigger.window.ActivitySampleEntity;
import ai.foresignal.trigger.window.ActivitySampleRepository;
import ai.foresignal.trigger.window.AnomalyObservationEntity;
import ai.foresignal.trigger.window.AnomalyObservationRepository;
import ai.foresignal.trigger.window.WindowEngine;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Manifest replay (prompt 116, MAN-1): a 100 anomaly evaluation records a manifest and replays to
 * {@code identical}; after a deliberate change to the recorded configuration it {@code differs}; with an input
 * sample purged it is {@code not-replayable} ({@code inputs-purged}); an LLM manifest reports "inputs recorded;
 * model output not reproducible"; an unknown component is refused up front; and the replay runs as a 103 job.
 */
@WebIntegrationTest
@Import({TestIngestSources.class, CoverageSupport.Config.class})
class ManifestReplayIT {

    private static final Instant NOW = Instant.parse("2026-03-01T12:03:00Z");
    private static final Instant CURRENT = Instant.parse("2026-03-01T11:00:00Z");
    private static final String SOURCE = TestIngestSources.STANDARD;

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired ActivitySampleRepository samples;
    @Autowired AnomalyObservationRepository observations;
    @Autowired SourceService sources;
    @Autowired TestRecords records;
    @Autowired CoverageSupport coverage;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;
    @Autowired ManifestService manifests;
    @Autowired ManifestReplayService replays;
    @Autowired BacktestJobService jobs;

    @BeforeEach
    void freeze() {
        clock.setInstant(NOW);
        sources.update(SOURCE, new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }

    /** An evaluated activity-up bucket with its manifest. */
    private AnomalyObservationEntity anomaly() {
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
                    .observedAt(at).put("name", "x" + i).build());
        }
        clock.setInstant(NOW);
        engine.evaluate(NOW, new WindowEngine.Emitter() {
            @Override
            public void emit(NormalizedRecord record) {
                // the replay reads the stored observation, not the rule-windows record
            }

            @Override
            public void tombstone(String kind, String externalId) {
                // nothing to retire
            }
        });
        AnomalyObservationEntity row = observations.findTop200ByRuleIdOrderByIdDesc(rule).get(0);
        assertThat(row.getClassification()).isEqualTo("activity-up");
        assertThat(row.getManifestId()).isNotNull();
        return row;
    }

    @Test
    @DisplayName("a replayed anomaly evaluation is identical; the manifest records the detector, window and inputs")
    void identical() {
        AnomalyObservationEntity row = anomaly();
        ManifestService.Manifest manifest = manifests.get(row.getManifestId());
        assertThat(manifest.component()).isEqualTo("poisson-tail");
        assertThat(manifest.componentVersion()).isEqualTo("1.0.0");
        assertThat(manifest.asOfSeq()).isEqualTo(row.getWatermarkSeq());
        assertThat(manifest.refs()).hasSizeGreaterThan(1);
        assertThat(manifest.refs().get(0)).startsWith("activity-sample:");
        assertThat(manifest.registries()).containsKeys("observability.json", "originators.json");
        assertThat(manifest.build()).containsKey("version");
        assertThat(replays.replay(row.getManifestId()).outcome()).isEqualTo(ReplayOutcome.IDENTICAL);
    }

    @Test
    @DisplayName("after a deliberate configuration change the replay differs, with a diff summary")
    void differsAfterConfigChange() {
        AnomalyObservationEntity row = anomaly();
        jdbc.update("UPDATE analysis_manifest SET config = jsonb_set(config, '{window,minAbsoluteDelta}', '1000')"
                + " WHERE id = ?", row.getManifestId());
        ReplayOutcome outcome = replays.replay(row.getManifestId());
        assertThat(outcome.outcome()).isEqualTo(ReplayOutcome.DIFFERS);
        assertThat(outcome.diffSummary()).contains("classification");
    }

    @Test
    @DisplayName("with an input sample purged the manifest is not-replayable (inputs-purged)")
    void purgedInputs() {
        AnomalyObservationEntity row = anomaly();
        String ref = manifests.get(row.getManifestId()).refs().get(1);
        String bucket = ref.substring(ref.lastIndexOf('/') + 1);
        jdbc.update("DELETE FROM activity_sample WHERE rule_id = ? AND bucket_start = ?", row.getRuleId(),
                java.sql.Timestamp.from(Instant.parse(bucket)));
        ReplayOutcome outcome = replays.replay(row.getManifestId());
        assertThat(outcome.outcome()).isEqualTo(ReplayOutcome.NOT_REPLAYABLE);
        assertThat(outcome.reason()).isEqualTo(ReplayOutcome.INPUTS_PURGED);
    }

    @Test
    @DisplayName("an LLM manifest reports inputs recorded, model output not reproducible")
    void llmManifest() {
        String id = manifests.record(ManifestRequest.of("analyst", "1.0.0").inputs(List.of("alert:1"))
                .model(Map.of("provider", "test", "model", "m", "promptHash", "h")));
        ReplayOutcome outcome = replays.replay(id);
        assertThat(outcome.outcome()).isEqualTo(ReplayOutcome.NOT_REPLAYABLE);
        assertThat(outcome.reason()).isEqualTo(ReplayOutcome.LLM_OUTPUT);
        assertThat(outcome.message()).isEqualTo(ReplayOutcome.LLM_MESSAGE);
    }

    @Test
    @DisplayName("a component no replayer knows is refused up front with 409 MANIFEST_NOT_REPLAYABLE")
    void unknownComponent() {
        String id = manifests.record(ManifestRequest.of("retired-detector", "0.1.0").config(Map.of("a", 1)));
        assertThatThrownBy(() -> replays.submit(id, null)).isInstanceOf(ConflictException.class)
                .hasMessageContaining("retired-detector");
    }

    @Test
    @DisplayName("identical manifests are stored once")
    void storedOnce() {
        String a = manifests.record(ManifestRequest.of("x", "1.0.0").config(Map.of("k", 1, "j", 2)));
        String b = manifests.record(ManifestRequest.of("x", "1.0.0").config(Map.of("j", 2, "k", 1)));
        assertThat(a).isEqualTo(b);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM analysis_manifest WHERE component = 'x'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("the replay runs as a manifest-replay job whose result is the ReplayResult")
    void replayJob() {
        AnomalyObservationEntity row = anomaly();
        Map<String, Object> queued = replays.submit(row.getManifestId(), null);
        assertThat(queued).containsEntry("status", "queued").containsEntry("manifestId", row.getManifestId());
        String jobId = (String) queued.get("jobId");
        await().atMost(Duration.ofSeconds(20)).until(() -> "done".equals(jobs.get(jobId).getStatus()));
        BacktestJobEntity job = jobs.get(jobId);
        assertThat(job.getType()).isEqualTo(BacktestJobService.MANIFEST_REPLAY);
        assertThat(job.getResult()).containsEntry("outcome", ReplayOutcome.IDENTICAL).containsEntry("status", "done");
    }
}
