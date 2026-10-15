package ai.foresignal.trigger.window;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The windowed-rule evaluator as a retention consumer (prompt 100): the clamp holds version
 * retention back to the smallest watermark the window state has processed, so anomaly backfill can
 * still read the history it needs.
 */
@Component
public class WindowCursorConsumer implements RetentionConsumer {

    private final WindowStateRepository states;

    /** Creates the consumer. */
    public WindowCursorConsumer(WindowStateRepository states) {
        this.states = states;
    }

    @Override
    public String name() {
        return SettingKeys.ANOMALY_CURSOR;
    }

    @Override
    public OptionalLong cursorSeq() {
        Long min = null;
        for (WindowStateEntity state : states.findAll()) {
            Long seq = state.getLastWatermarkSeq();
            if (seq != null && (min == null || seq < min)) {
                min = seq;
            }
        }
        return min == null ? OptionalLong.empty() : OptionalLong.of(min);
    }
}
