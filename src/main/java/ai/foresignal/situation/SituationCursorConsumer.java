package ai.foresignal.situation;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The situation linker as a retention consumer (prompt 118, 019's consumer clamp): version retention is clamped to
 * the linker's durable {@code situation.cursor}, so its catch-up can still read the versions it has not linked,
 * including the clamp's release-after-2×-window rule with its alarm.
 */
@Component
public class SituationCursorConsumer implements RetentionConsumer {

    private final SituationLinker linker;

    /** Creates the consumer. */
    public SituationCursorConsumer(SituationLinker linker) {
        this.linker = linker;
    }

    @Override
    public String name() {
        return SettingKeys.SITUATION_CURSOR;
    }

    /** The linker's last linked seq; empty while it is not running. */
    @Override
    public OptionalLong cursorSeq() {
        return linker.running() ? OptionalLong.of(linker.cursor()) : OptionalLong.empty();
    }
}
