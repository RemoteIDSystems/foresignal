package ai.foresignal.precursor;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the precursor live watch's durable cursor ({@code precursor.cursor}, prompt 120) with retention's consumer
 * clamp (RET-2), so the versions it has not passed are kept, except after the documented release.
 */
@Component
public class PrecursorCursorConsumer implements RetentionConsumer {

    private final PrecursorLiveWatch watch;

    /** Creates the consumer. */
    public PrecursorCursorConsumer(PrecursorLiveWatch watch) {
        this.watch = watch;
    }

    @Override
    public String name() {
        return SettingKeys.PRECURSOR_CURSOR;
    }

    /** The cursor; empty before the first poll. */
    @Override
    public OptionalLong cursorSeq() {
        long value = watch.cursor();
        return value <= 0 ? OptionalLong.empty() : OptionalLong.of(value);
    }
}
