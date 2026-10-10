package ai.elaro.source.spi;

import java.util.List;

/**
 * Where a streaming adapter hands records over.
 *
 * <p><strong>{@link #accept} may block.</strong> The ingest queue is bounded, and when it is full
 * the sink waits rather than dropping records or growing without limit. That back-pressure is
 * deliberate: a streaming adapter that is producing faster than the writer can persist should slow
 * down, and an adapter must therefore not call {@code accept} while holding a lock that its own
 * read loop needs.
 *
 * <p>The sink stamps {@code receivedAt} as each record arrives, so an adapter must not try to set
 * it.
 */
public interface RecordSink {

    /** Hands over one record. May block (see the class note). */
    void accept(NormalizedRecord record);

    /** Hands over several records. May block. */
    default void acceptAll(List<NormalizedRecord> records) {
        for (NormalizedRecord record : records) {
            accept(record);
        }
    }
}
