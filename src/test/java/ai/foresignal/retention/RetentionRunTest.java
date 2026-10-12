package ai.foresignal.retention;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.foresignal.settings.RetentionSettings;
import ai.foresignal.store.RetentionStateEntity;
import ai.foresignal.store.query.SeqClockQuery;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.OptionalLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/** The consumer clamp and the run's accumulators (prompt 019, invariant RET-2). */
class RetentionRunTest {

    static final Instant NOW = Instant.parse("2026-06-01T00:00:00Z");
    static final Duration W = Duration.ofDays(7);

    final SeqClockQuery seqClock = mock(SeqClockQuery.class);
    final NamedParameterJdbcTemplate jdbc = mock(NamedParameterJdbcTemplate.class);

    static RetentionConsumer consumer(String name, Long cursor) {
        return new RetentionConsumer() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public OptionalLong cursorSeq() {
                return cursor == null ? OptionalLong.empty() : OptionalLong.of(cursor);
            }
        };
    }

    RetentionRun run(RetentionConsumer... consumers) {
        when(seqClock.cutoffSeq(any(), eq(NOW.minus(W)))).thenReturn(1_000L);
        when(seqClock.cutoffSeq(any(), eq(NOW.minus(W.multipliedBy(2))))).thenReturn(400L);
        return new RetentionRun(NOW, RetentionSettings.defaults(), jdbc, seqClock, List.of(consumers));
    }

    @Test
    @DisplayName("without consumers the cutoff is cutoffSeq(now − window)")
    void noConsumers() {
        RetentionRun run = run();
        assertThat(run.cutoff(W)).isEqualTo(1_000L);
        assertThat(run.clampActive()).isFalse();
        assertThat(run.lags()).isEmpty();
    }

    @Test
    @DisplayName("a consumer behind the cutoff clamps it to cursor + 1; the furthest behind is reported")
    void clamps() {
        RetentionRun run = run(consumer("a", 700L), consumer("b", 500L), consumer("c", 5_000L), consumer("d", null));
        assertThat(run.cutoff(W)).isEqualTo(501L);
        assertThat(run.clampActive()).isTrue();
        assertThat(run.clampConsumer()).isEqualTo("b");
        assertThat(run.clampCursor()).isEqualTo(500L);
    }

    @Test
    @DisplayName("a consumer older than now − 2 × window releases its clamp and is recorded as lagging")
    void releases() {
        RetentionRun run = run(consumer("old", 100L), consumer("ok", 800L));
        assertThat(run.cutoff(W)).isEqualTo(801L);
        assertThat(run.lags()).extracting(RetentionRun.Lag::consumer).containsExactly("old");
        assertThat(run.clampConsumer()).isEqualTo("ok");
    }

    @Test
    @DisplayName("a consumer that throws does not clamp")
    void throwingConsumer() {
        RetentionConsumer broken = new RetentionConsumer() {
            @Override
            public String name() {
                return "broken";
            }

            @Override
            public OptionalLong cursorSeq() {
                throw new IllegalStateException("no cursor");
            }
        };
        assertThat(run(broken).cutoff(W)).isEqualTo(1_000L);
    }

    @Test
    @DisplayName("floors keep the maximum; purges record the watermark only when heads were deleted")
    void accumulators() {
        RetentionRun run = run();
        run.floor("s", null, 10);
        run.floor("s", null, 5);
        run.floor("s", "k", 7);
        run.versionsDeleted("s", 3);
        run.versionsDeleted("s", 2);
        run.headsPurged("s", 0, 99);
        run.headsPurged("t", 2, 50);
        run.skip("x");
        run.standardWindow(Duration.ofDays(3));
        run.standardWindow(Duration.ofDays(30));
        run.partitionDropped();

        assertThat(run.floors().get("s")).containsEntry(RetentionStateEntity.ALL_KINDS, 10L).containsEntry("k", 7L);
        assertThat(run.versionsBySource()).containsEntry("s", 5L);
        assertThat(run.deletedFrom("record_version")).isEqualTo(5L);
        assertThat(run.purgedSources()).containsOnlyKeys("t").containsEntry("t", 50L);
        assertThat(run.headsPurged()).isEqualTo(2L);
        assertThat(run.skipped("x")).isTrue();
        assertThat(run.largestStandardWindow()).isEqualTo(Duration.ofDays(30));
        assertThat(run.partitionsDropped()).isEqualTo(1);
    }

    @Test
    @DisplayName("the largest standard window defaults to retention.default")
    void largestDefault() {
        assertThat(run().largestStandardWindow()).isEqualTo(RetentionSettings.DEFAULT_RETENTION);
    }
}
