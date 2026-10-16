package ai.foresignal.sink;

import ai.foresignal.retention.RetentionConsumer;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The retention consumer that clamps the purge horizon to the slowest enabled CHANGE_FEED sink's cursor
 * (prompt 110): {@code min(trigger, embedding, sinks) + 1}.
 */
@Component
public class SinkCursorConsumer implements RetentionConsumer {

    /** The consumer name. */
    public static final String NAME = "sink.cursor";

    private final SinkRepository sinks;
    private final SinkCursorRepository cursors;
    private final SinkTypeRegistry types;

    /** Creates the consumer. */
    public SinkCursorConsumer(SinkRepository sinks, SinkCursorRepository cursors, SinkTypeRegistry types) {
        this.sinks = sinks;
        this.cursors = cursors;
        this.types = types;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public OptionalLong cursorSeq() {
        long min = Long.MAX_VALUE;
        boolean any = false;
        for (SinkEntity sink : sinks.findByEnabledTrueOrderByIdAsc()) {
            SinkType type = types.find(sink.getType()).orElse(null);
            if (type == null || type.spec().kind() != SinkKind.CHANGE_FEED) {
                continue;
            }
            SinkCursorEntity cursor = cursors.findById(sink.getId()).orElse(null);
            if (cursor == null) {
                continue;
            }
            any = true;
            min = Math.min(min, cursor.getLastSeq());
        }
        return any ? OptionalLong.of(min) : OptionalLong.empty();
    }
}
