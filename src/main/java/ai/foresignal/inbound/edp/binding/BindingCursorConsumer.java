package ai.foresignal.inbound.edp.binding;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The binding router as a retention consumer (prompt 138): version retention is clamped to the router's durable cursor
 * {@code external.binding.cursor}, so it never deletes EDP versions the bindings have not seen (RET-2, including the
 * release after twice the window with an alarm).
 */
@Component
public class BindingCursorConsumer implements RetentionConsumer {

    private final BindingRouter router;

    /** Creates the consumer. */
    public BindingCursorConsumer(BindingRouter router) {
        this.router = router;
    }

    @Override
    public String name() {
        return SettingKeys.EXTERNAL_BINDING_CURSOR;
    }

    @Override
    public OptionalLong cursorSeq() {
        return router.running() ? OptionalLong.of(router.cursor()) : OptionalLong.empty();
    }
}
