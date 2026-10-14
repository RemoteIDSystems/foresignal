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
import java.util.List;

/**
 * A test complete-set reference adapter (prompt 094) for {@code TombstoneAbsentIT}: it emits a settable
 * record set in chunks, always including the {@code dataset-release} kind the executor reconciles on, and can
 * report {@link FetchResult#unchanged()} without emitting anything. It makes no upstream request.
 */
public class ReferenceTestAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "test-reference";

    /** The record kind. */
    public static final String KIND = "test-record";

    /** The release identity the dataset-release record carries. */
    public static final String RELEASE = "test-release-1";

    private static final SourceDescriptor DESCRIPTOR = SourceDescriptor.builder(ID)
            .name("Reference test source")
            .kind(KIND)
            .kind("dataset-release")
            .mode(Mode.POLLING)
            .interval(Duration.ofDays(1), Duration.ofDays(7))
            .defaultBudget(100, 1000)
            .historyTier(HistoryTier.STANDARD)
            .observedAtMode(ObservedAtMode.RECEIVED)
            .ttlAnchor(TtlAnchor.RECEIVED)
            .defaultIngestPolicy(IngestPolicy.all())
            .defaultRetention(ReferenceSupport.RETENTION)
            .reconcileMaxAbsentRatio(0.2)
            .build();

    private volatile List<NormalizedRecord> records = List.of();
    private volatile boolean unchanged;

    /** Sets the records the next fetch emits. */
    public void set(List<NormalizedRecord> records) {
        this.records = List.copyOf(records);
        this.unchanged = false;
    }

    /** Makes the next fetch report a 304. */
    public void unchanged() {
        this.unchanged = true;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        if (unchanged) {
            return FetchResult.unchanged();
        }
        ReferenceSupport.emit(ctx, records);
        return FetchResult.complete(List.of(), RELEASE);
    }
}
