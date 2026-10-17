package ai.foresignal.stats;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the region rollup's durable cursor ({@code app_setting['stats.region.cursor']}, prompt 119) with
 * retention's consumer clamp (Core-Data-Model §13, RET-2), including its release after {@code 2 × window} with an
 * alarm, so raw rows are never deleted before they are rolled up by region.
 */
@Component
public class RegionRollupCursorConsumer implements RetentionConsumer {

    private final RegionRollupJob job;

    /** Creates the consumer. */
    public RegionRollupCursorConsumer(RegionRollupJob job) {
        this.job = job;
    }

    @Override
    public String name() {
        return SettingKeys.STATS_REGION_CURSOR;
    }

    /** The cursor's {@code seq}; empty before the first run. */
    @Override
    public OptionalLong cursorSeq() {
        RollupCursor cursor = job.cursor();
        return cursor == null ? OptionalLong.empty() : OptionalLong.of(cursor.seq());
    }
}
