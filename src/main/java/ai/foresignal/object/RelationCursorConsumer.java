package ai.foresignal.object;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The relation projector as a retention consumer (prompt 129, 019's consumer clamp): version retention is clamped to
 * the projector's durable {@code relation.cursor}, including the clamp's release-after-2×-window rule with its alarm.
 */
@Component
public class RelationCursorConsumer implements RetentionConsumer {

    private final RelationProjector projector;

    /** Creates the consumer. */
    public RelationCursorConsumer(RelationProjector projector) {
        this.projector = projector;
    }

    @Override
    public String name() {
        return SettingKeys.RELATION_CURSOR;
    }

    /** The projector's last processed seq; empty while it is not running. */
    @Override
    public OptionalLong cursorSeq() {
        return projector.running() ? OptionalLong.of(projector.cursor()) : OptionalLong.empty();
    }
}
