package ai.foresignal.trigger.window;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import ai.foresignal.store.query.SeqClockQuery;
import java.util.OptionalLong;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * The windowed-rule evaluator as a retention consumer (prompt 100): the clamp holds version retention
 * back to the oldest version an <em>enabled anomaly</em> rule's next bucket can still need (the
 * {@code seq_clock} cutoff an hour before that bucket starts), so its catch-up can read it.
 *
 * <p>Only enabled anomaly rules count: count and absence rules evaluate the current window and never
 * replay, and a disabled rule, a deleted rule or a group that no longer appears must not hold
 * retention back. A rule whose cursor is reset for a backfill (or not yet started) holds nothing: its
 * backfill reads only what retention still has.
 */
@Component
public class WindowCursorConsumer implements RetentionConsumer {

    /** Margin before the next bucket for versions whose clocks ran ahead of ingestion. */
    static final java.time.Duration MARGIN = java.time.Duration.ofHours(1);

    private final WindowEngine engine;
    private final SeqClockQuery seqClock;
    private final NamedParameterJdbcTemplate jdbc;

    /** Creates the consumer. */
    public WindowCursorConsumer(WindowEngine engine, SeqClockQuery seqClock, NamedParameterJdbcTemplate jdbc) {
        this.engine = engine;
        this.seqClock = seqClock;
        this.jdbc = jdbc;
    }

    @Override
    public String name() {
        return SettingKeys.ANOMALY_CURSOR;
    }

    @Override
    public OptionalLong cursorSeq() {
        Long min = null;
        for (WindowEngine.WindowRule rule : engine.enabled()) {
            if (rule.type() != RuleType.ANOMALY) {
                continue;
            }
            WindowEngine.AnomalyCursor cursor = engine.cursor(rule.spec().id());
            if (cursor == null || cursor.bucketStart() < 0) {
                continue;
            }
            // The next bucket starts where the cursor's bucket ends; every version it can contain was
            // ingested after that (less a margin), so retention may drop only seqs ingested before it.
            java.time.Instant nextBucket = java.time.Instant.ofEpochSecond(cursor.bucketStart()).plus(rule.window().bucket());
            long seq = Math.min(cursor.watermarkSeq(), seqClock.cutoffSeq(jdbc, nextBucket.minus(MARGIN)));
            if (min == null || seq < min) {
                min = seq;
            }
        }
        return min == null ? OptionalLong.empty() : OptionalLong.of(min);
    }
}
