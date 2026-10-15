package ai.foresignal.backtest;

import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.test.MutableClock;
import ai.foresignal.test.WebIntegrationTest;
import ai.foresignal.testsupport.ingest.TestIngestSources;
import ai.foresignal.testsupport.ingest.TestRecords;
import ai.foresignal.trigger.TriggerRuleService;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import tools.jackson.databind.json.JsonMapper;

/** A rule backtest (prompt 103): the job completes with caveats and a would-fire count. */
@WebIntegrationTest
@Import(TestIngestSources.class)
class RuleBacktestIT {

    @Autowired BacktestJobService jobs;
    @Autowired TriggerRuleService rules;
    @Autowired TestRecords records;
    @Autowired MutableClock clock;
    @Autowired JsonMapper json;

    @Test
    @DisplayName("a rule job completes with NOT_GROUND_TRUTH and a would-fire count")
    void ruleJob() {
        clock.setInstant(Instant.parse("2026-03-01T12:00:00Z"));
        Instant now = clock.instant();
        UUID ruleId = rules.create(json.readTree("{\"name\":\"any thing\",\"sources\":[\"test-standard\"],"
                + "\"kinds\":[\"thing\"],\"mode\":\"match\"}")).rule().getId();
        records.ingestAt(TestIngestSources.STANDARD, now,
                NormalizedRecord.builder("a", "thing").observedAt(now).point(-95, 32).put("name", "a").build());

        BacktestJobEntity job = jobs.submit("rule", ruleId.toString(), null,
                json.readTree("{\"fromSeq\":0,\"toSeq\":" + Long.MAX_VALUE + "}"), null, null);
        jobs.run(job.getId());
        BacktestJobEntity done = jobs.get(job.getId());
        assertThat(done.getStatus()).isEqualTo("done");
        assertThat(done.getResult()).containsKey("wouldFire");
        assertThat(done.getCaveats()).anyMatch(c -> BacktestCaveat.NOT_GROUND_TRUTH.equals(c.get("code")));
    }
}
