package ai.foresignal.trigger;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the trigger engine's durable cursor ({@code app_setting['trigger.cursor']}) with
 * retention's consumer clamp (prompt 019), so retention never deletes a version the engine has not
 * evaluated, except after the documented release.
 */
@Component
public class TriggerCursorConsumer implements RetentionConsumer {

    private final TriggerEngine engine;

    /** Creates the consumer. */
    public TriggerCursorConsumer(TriggerEngine engine) {
        this.engine = engine;
    }

    @Override
    public String name() {
        return SettingKeys.TRIGGER_CURSOR;
    }

    /** The engine's last evaluated seq; empty while the engine is not running. */
    @Override
    public OptionalLong cursorSeq() {
        return engine.running() ? OptionalLong.of(engine.cursor()) : OptionalLong.empty();
    }
}
