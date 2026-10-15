package ai.foresignal.intel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * A 017 rule fires on an intelligence event (prompt 101): the {@code foresignal-intel} record of kind
 * {@code intelligence-event} is an ordinary record, so a rule on {@code kind=intelligence-event} and
 * {@code severity == critical} alerts.
 */
@WebIntegrationTest
@Import(TestIngestSources.class)
class EventAlertIT {

    @Autowired TriggerRuleService rules;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JdbcTemplate jdbc;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("a critical intelligence event raises an alert")
    void criticalFires() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        rules.create(json.readTree("{\"name\":\"critical intel\",\"sources\":[\"foresignal-intel\"],"
                + "\"kinds\":[\"intelligence-event\"],\"conditions\":[{\"path\":\"severity\",\"op\":\"==\","
                + "\"value\":\"critical\"}],\"mode\":\"enter\",\"severity\":\"critical\"}"));
        records.ingestAt(ForesignalIntelGenerator.ID, now,
                NormalizedRecord.builder("01INTELEVENT0000000000001", ForesignalIntelGenerator.KIND)
                        .observedAt(now).put("severity", "critical").put("type", "maritime-disruption")
                        .put("status", "active").put("automated", true).build());
        await().atMost(Duration.ofSeconds(20)).until(() -> alerts() > 0);
        assertThat(alerts()).isGreaterThan(0);
    }

    private int alerts() {
        Integer n = jdbc.queryForObject("select count(*) from alert where source_id = 'foresignal-intel'",
                Integer.class);
        return n == null ? 0 : n;
    }
}
