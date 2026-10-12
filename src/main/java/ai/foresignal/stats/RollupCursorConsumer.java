package ai.foresignal.stats;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the rollup job's durable cursor ({@code app_setting['stats.rollup.cursor']}) with
 * retention's consumer clamp (prompt 038, Core-Data-Model §13): retention clamps to
 * {@code min(trigger, embedding, rollup) + 1}, so raw rows are never deleted before they are rolled
 * up, except after the documented release at {@code 2 × window}.
 */
@Component
public class RollupCursorConsumer implements RetentionConsumer {

    private final RollupJob job;

    /** Creates the consumer. */
    public RollupCursorConsumer(RollupJob job) {
        this.job = job;
    }

    @Override
    public String name() {
        return SettingKeys.STATS_ROLLUP_CURSOR;
    }

    /** The cursor's {@code seq}; empty before the job's first run (it then holds nothing back). */
    @Override
    public OptionalLong cursorSeq() {
        RollupCursor cursor = job.cursor();
        return cursor == null ? OptionalLong.empty() : OptionalLong.of(cursor.seq());
    }
}
