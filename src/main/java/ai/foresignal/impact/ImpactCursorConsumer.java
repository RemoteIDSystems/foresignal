package ai.foresignal.impact;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the impact projector's durable cursor ({@code impact.cursor}, prompt 127) with retention's consumer clamp
 * (RET-2), so the hazard versions it has not projected are kept, except after the documented release.
 */
@Component
public class ImpactCursorConsumer implements RetentionConsumer {

    private final ImpactWatcher watcher;

    /** Creates the consumer. */
    public ImpactCursorConsumer(ImpactWatcher watcher) {
        this.watcher = watcher;
    }

    @Override
    public String name() {
        return SettingKeys.IMPACT_CURSOR;
    }

    /** The cursor; empty before the first pass. */
    @Override
    public OptionalLong cursorSeq() {
        long value = watcher.cursor();
        return value <= 0 ? OptionalLong.empty() : OptionalLong.of(value);
    }
}
