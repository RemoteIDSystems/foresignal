package ai.foresignal.trigger.window;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * The count rule end to end (prompt 100): a clear → active transition fires once, distinct entities
 * count once however many versions they have, and a group that leaves the window clears.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class WindowEvaluatorIT {

    @Autowired TriggerRuleService rules;
    @Autowired WindowEngine engine;
    @Autowired TestRecords records;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    /** A capturing emitter. */
    static final class Capture implements WindowEngine.Emitter {

        final List<NormalizedRecord> emitted = new ArrayList<>();

        @Override
        public void emit(NormalizedRecord record) {
            emitted.add(record);
        }

        @Override
        public void tombstone(String kind, String externalId) {
            emitted.add(NormalizedRecord.tombstone(externalId, kind, Instant.now()));
        }

        boolean hasState(String externalId, String state) {
            return emitted.stream().anyMatch(r -> r.kind().equals("window-state")
                    && r.externalId().equals(externalId)
                    && state.equals(r.payload().get("state")));
        }
    }

    private UUID createRule(String body) {
        return rules.create(json.readTree(body)).rule().getId();
    }

    private int alertCount(UUID ruleId) {
        Integer n = jdbc.queryForObject("select count(*) from alert where rule_id = ?", Integer.class, ruleId);
        return n == null ? 0 : n;
    }

    @Test
    @DisplayName("a count rule fires once on clear → active and resolves on active → clear")
    void countFiresAndResolves() {
        Instant now = Instant.now();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("a", "thing").observedAt(now).put("name", "a").build(),
                NormalizedRecord.builder("b", "thing").observedAt(now).put("name", "b").build());
        UUID id = createRule("{\"name\":\"many things\",\"type\":\"count\",\"sources\":[\"test-standard\"],"
                + "\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":2}}");
        Capture capture = new Capture();
        engine.evaluate(now.plusSeconds(60), capture);
        assertThat(capture.hasState(id + ":*", "active")).isTrue();
        assertThat(alertCount(id)).isEqualTo(1);

        // The window moves past the records: active → clear resolves the alert.
        Capture later = new Capture();
        engine.evaluate(now.plusSeconds(3 * 3600), later);
        assertThat(later.hasState(id + ":*", "clear")).isTrue();
    }

    @Test
    @DisplayName("distinct entities, not versions")
    void distinctEntities() {
        Instant now = Instant.now();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("a", "thing").observedAt(now).put("name", "a1").build());
        records.ingestAt(TestIngestSources.STANDARD, now.plusSeconds(5),
                NormalizedRecord.builder("a", "thing").observedAt(now.plusSeconds(5)).put("name", "a2").build());
        UUID id = createRule("{\"name\":\"two versions\",\"type\":\"count\",\"sources\":[\"test-standard\"],"
                + "\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":2}}");
        Capture capture = new Capture();
        engine.evaluate(now.plusSeconds(60), capture);
        assertThat(capture.hasState(id + ":*", "clear")).isTrue();
    }

    @Test
    @DisplayName("per-group states with groupBy=source")
    void groups() {
        Instant now = Instant.now();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("a", "thing").observedAt(now).put("name", "a").build());
        records.ingestAt(TestIngestSources.TTL, now,
                NormalizedRecord.builder("b", "thing").observedAt(now).put("name", "b").build());
        UUID id = createRule("{\"name\":\"per source\",\"type\":\"count\",\"sources\":[\"test-standard\",\"test-ttl\"],"
                + "\"window\":{\"window\":\"30m\",\"op\":\">=\",\"threshold\":1,\"groupBy\":\"source\"}}");
        Capture capture = new Capture();
        engine.evaluate(now.plusSeconds(60), capture);
        assertThat(capture.hasState(id + ":" + TestIngestSources.STANDARD, "active")).isTrue();
        assertThat(capture.hasState(id + ":" + TestIngestSources.TTL, "active")).isTrue();
    }
}
