package ai.foresignal.entity;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Holds retention back to the entity linker's durable cursor (prompt 098), so no key is deleted
 * before the linker has read the version that asserted it.
 */
@Component
public class EntityCursorConsumer implements RetentionConsumer {

    private final EntityLinker linker;

    /** Creates the consumer. */
    public EntityCursorConsumer(EntityLinker linker) {
        this.linker = linker;
    }

    @Override
    public String name() {
        return SettingKeys.ENTITY_CURSOR;
    }

    @Override
    public OptionalLong cursorSeq() {
        long cursor = linker.cursor();
        return cursor < 0 ? OptionalLong.empty() : OptionalLong.of(cursor);
    }
}
