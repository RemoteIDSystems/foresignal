package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.derive.DerivedRunner;
import ai.foresignal.source.SourceService;
import ai.foresignal.source.SourceSettingsUpdate;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The {@code rule-windows} records persist through the real DERIVED pipeline (prompt 100). This is
 * the guard for the epoch {@code observedAt} defect: a record with {@code observed_at} before the
 * normalizer's floor is dropped at ingest, so nothing lands in {@code record_head}. Here the source
 * is enabled through {@link DerivedRunner} and a {@code window-state} record must actually persist.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class RuleWindowsIngestIT {

    private static final Duration WAIT = Duration.ofSeconds(20);

    @Autowired TriggerRuleService rules;
    @Autowired DerivedRunner runner;
    @Autowired SourceService sources;
    @Autowired TestRecords records;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("an enabled rule-windows source persists a window-state record through ingest")
    void windowStatePersists() {
        Instant now = Instant.now();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("a", "thing").observedAt(now).put("name", "a").build());
        UUID id = rules.create(json.readTree("{\"name\":\"one thing\",\"type\":\"count\","
                + "\"sources\":[\"test-standard\"],\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1}}"))
                .rule().getId();

        sources.update(RuleWindowsGenerator.ID,
                new SourceSettingsUpdate(true, null, Map.of(), Budget.of(1, 1), null, false, null, false));
        await().atMost(WAIT).until(() -> runner.running().contains(RuleWindowsGenerator.ID));
        runner.tickNow(RuleWindowsGenerator.ID);
        assertThat(runner.awaitIdle(RuleWindowsGenerator.ID, WAIT)).isTrue();

        Integer heads = jdbc.queryForObject(
                "select count(*) from record_head where source_id = ? and external_id = ?",
                Integer.class, RuleWindowsGenerator.ID, id + ":*");
        assertThat(heads).isNotNull();
        assertThat(heads).isGreaterThan(0);

        sources.update(RuleWindowsGenerator.ID,
                new SourceSettingsUpdate(false, null, Map.of(), Budget.of(1, 1), null, false, null, false));
    }
}
