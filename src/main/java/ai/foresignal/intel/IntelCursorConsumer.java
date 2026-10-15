package ai.foresignal.intel;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The correlation engine as a retention consumer (prompt 101): version retention is clamped to the
 * smallest watermark the engine has processed, so its catch-up can still read the history it needs.
 */
@Component
public class IntelCursorConsumer implements RetentionConsumer {

    private final CorrelationEngine engine;

    /** Creates the consumer. */
    public IntelCursorConsumer(CorrelationEngine engine) {
        this.engine = engine;
    }

    @Override
    public String name() {
        return SettingKeys.INTEL_CURSOR;
    }

    @Override
    public OptionalLong cursorSeq() {
        return engine.running() ? OptionalLong.of(engine.cursor()) : OptionalLong.empty();
    }
}
