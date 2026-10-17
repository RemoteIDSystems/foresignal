package ai.foresignal.analytics.eventstudy;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the live event-study scan's durable cursor ({@code analysis.live.cursor}, prompt 119) with retention's
 * consumer clamp (RET-2), so the versions it has not read are kept, except after the documented release.
 */
@Component
public class LiveCursorConsumer implements RetentionConsumer {

    private final LiveEventStudyService live;

    /** Creates the consumer. */
    public LiveCursorConsumer(LiveEventStudyService live) {
        this.live = live;
    }

    @Override
    public String name() {
        return SettingKeys.ANALYSIS_LIVE_CURSOR;
    }

    /** The cursor; empty before the first poll. */
    @Override
    public OptionalLong cursorSeq() {
        long value = live.cursor();
        return value <= 0 ? OptionalLong.empty() : OptionalLong.of(value);
    }
}
