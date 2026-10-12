package ai.foresignal.retention;

import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD;
import static ai.foresignal.testsupport.ingest.TestIngestSources.STANDARD_KIND;
import static org.assertj.core.api.Assertions.assertThat;

import ai.foresignal.test.IntegrationTest;
import ai.foresignal.testsupport.retention.RetentionItSupport;
import ai.foresignal.testsupport.retention.RetentionTestSources;
import ai.foresignal.testsupport.retention.RetentionTestSources.TestConsumer;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * Invariant RET-2: retention never deletes a version a durable consumer has not processed, except
 * after the documented release at 2 × window, which raises the alarm.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Import(RetentionTestSources.class)
class ConsumerClampIT extends RetentionItSupport {

    @Autowired MeterRegistry meters;

    @Test
    @DisplayName("a lagging cursor holds the cutoff, then releases it after 2 × window with metric and notification")
    void clampHoldsThenReleases() {
        ingestAt(STANDARD, T0, record("x", STANDARD_KIND, T0, 1));
        Instant m1 = T0.plus(Duration.ofMinutes(1));
        ingestAt(STANDARD, m1, record("m", STANDARD_KIND, m1, 1));
        Instant m2 = T0.plus(Duration.ofMinutes(2));
        ingestAt(STANDARD, m2, record("n", STANDARD_KIND, m2, 1));
        long xSeq = jdbc.queryForObject("SELECT seq FROM record_version WHERE source_id = ? AND external_id = 'x'",
                Long.class, STANDARD);
        consumer.set(xSeq);

        RetentionRunResult held = runAt(T0.plus(Duration.ofDays(7)).plus(Duration.ofMinutes(5)));

        assertThat(versions(STANDARD, "x")).isZero();
        assertThat(versions(STANDARD, "m")).isEqualTo(1);
        assertThat(held.consumerClamp().active()).isTrue();
        assertThat(held.consumerClamp().consumer()).isEqualTo(TestConsumer.NAME);
        assertThat(held.consumerClamp().cursorSeq()).isEqualTo(xSeq);
        assertThat(job.lastLags()).isEmpty();
        double before = meters.counter(RetentionJob.CONSUMER_LAG_METRIC, "consumer", TestConsumer.NAME).count();

        RetentionRunResult released = runAt(T0.plus(Duration.ofDays(14)).plus(Duration.ofMinutes(5)));

        assertThat(versions(STANDARD, "m")).isZero();
        assertThat(versions(STANDARD, "n")).isEqualTo(1);
        assertThat(released.consumerClamp().active()).isFalse();
        assertThat(job.lastLags()).extracting(RetentionRun.Lag::consumer).containsExactly(TestConsumer.NAME);
        assertThat(meters.counter(RetentionJob.CONSUMER_LAG_METRIC, "consumer", TestConsumer.NAME).count())
                .isEqualTo(before + 1);
        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM notification WHERE subtype = 'system.retention_consumer_lag'", Long.class))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("the trigger engine's cursor is a registered consumer")
    void triggerCursorRegistered() {
        assertThat(job.consumers()).extracting(RetentionConsumer::name).contains("trigger.cursor", TestConsumer.NAME);
    }
}
