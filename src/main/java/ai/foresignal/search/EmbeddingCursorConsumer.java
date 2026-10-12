package ai.foresignal.search;

import ai.foresignal.retention.RetentionConsumer;
import ai.foresignal.settings.SettingKeys;
import java.util.OptionalLong;
import org.springframework.stereotype.Component;

/**
 * Registers the embedding worker's durable cursor ({@code app_setting['embedding.cursor']}) with
 * retention's consumer clamp (Core-Data-Model §13), so retention never deletes a version the worker
 * has not embedded, except after the documented release at {@code 2 × window}.
 */
@Component
public class EmbeddingCursorConsumer implements RetentionConsumer {

    private final EmbeddingWorker worker;

    /** Creates the consumer. */
    public EmbeddingCursorConsumer(EmbeddingWorker worker) {
        this.worker = worker;
    }

    @Override
    public String name() {
        return SettingKeys.EMBEDDING_CURSOR;
    }

    /** The worker's last processed seq; empty while it is not running (it then holds nothing back). */
    @Override
    public OptionalLong cursorSeq() {
        return worker.running() ? OptionalLong.of(worker.cursor()) : OptionalLong.empty();
    }
}
