package ai.foresignal.testsupport.adapters;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.support.ReferenceSupport;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A test polling adapter that emits a settable set of records in chunks through {@code ctx.emitChunk}
 * (prompt 094), for {@code ChunkedFetchIT}. It makes no upstream request.
 */
public class ChunkedTestAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "test-chunked";

    /** The record kind. */
    public static final String KIND = "test-chunk";

    /** Records per chunk. */
    public static final int CHUNK = ReferenceSupport.CHUNK;

    private static final SourceDescriptor DESCRIPTOR = SourceDescriptor.builder(ID)
            .name("Chunked test source")
            .kind(KIND)
            .mode(Mode.POLLING)
            .interval(Duration.ofDays(1), Duration.ofDays(7))
            .defaultBudget(100, 1000)
            .historyTier(HistoryTier.STANDARD)
            .observedAtMode(ObservedAtMode.RECEIVED)
            .ttlAnchor(TtlAnchor.RECEIVED)
            .defaultIngestPolicy(IngestPolicy.all())
            .defaultRetention(ReferenceSupport.RETENTION)
            .build();

    private volatile List<NormalizedRecord> records = List.of();
    private volatile boolean complete = true;
    private volatile Consumer<Integer> afterChunk = index -> {};

    /** Sets the records the next fetch emits. */
    public void set(List<NormalizedRecord> records) {
        this.records = List.copyOf(records);
        this.complete = true;
    }

    /** Sets whether the next fetch reports a complete set. */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /** A callback invoked with the chunk index after each chunk is ingested (back-pressure probe). */
    public void afterChunk(Consumer<Integer> callback) {
        this.afterChunk = callback == null ? index -> {} : callback;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> snapshot = records;
        List<NormalizedRecord> chunk = new ArrayList<>(CHUNK);
        int index = 0;
        for (NormalizedRecord record : snapshot) {
            chunk.add(record);
            if (chunk.size() == CHUNK) {
                ctx.emitChunk(chunk);
                afterChunk.accept(++index);
                chunk = new ArrayList<>(CHUNK);
            }
        }
        if (!chunk.isEmpty()) {
            ctx.emitChunk(chunk);
            afterChunk.accept(++index);
        }
        return complete ? FetchResult.complete(List.of()) : FetchResult.of(List.of());
    }
}
