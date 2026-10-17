package ai.foresignal.evidence;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * The evidence resolver as a retention consumer (prompt 116): version retention is clamped to the resolver's
 * durable cursor {@code evidence.cursor}, so it never deletes versions the resolver has not linked yet (RET-2,
 * including the release after twice the window with an alarm).
 */
@Component
public class EvidenceCursorConsumer implements RetentionConsumer {

    private final EvidenceResolver resolver;

    /** Creates the consumer. */
    public EvidenceCursorConsumer(EvidenceResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public String name() {
        return SettingKeys.EVIDENCE_CURSOR;
    }

    @Override
    public OptionalLong cursorSeq() {
        return resolver.running() ? OptionalLong.of(resolver.cursor()) : OptionalLong.empty();
    }
}
