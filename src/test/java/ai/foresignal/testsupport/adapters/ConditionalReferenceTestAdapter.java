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
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.ReferenceSupport;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * A complete-set reference adapter that reads its dataset with a conditional GET (prompt 094, review fix): the
 * body is a comma-separated list of external ids served by WireMock at {@link #PATH}, and a 304 skips the run.
 * {@code TombstoneAbsentIT} uses it to prove that the operator's {@code allowShrink} re-run bypasses the stored
 * validators and that a shrink-guard degradation clears once a reconciliation runs.
 */
public class ConditionalReferenceTestAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "test-reference-conditional";

    /** The record kind. */
    public static final String KIND = "test-conditional-record";

    /** The path the dataset is served at. */
    public static final String PATH = "/reference/dataset.csv";

    /** The conditional-GET state key. */
    public static final String STATE_KEY = "dataset";

    private static final SourceDescriptor DESCRIPTOR = SourceDescriptor.builder(ID)
            .name("Conditional reference test source")
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

    private volatile String baseUrl = "http://localhost";

    /** Points the adapter at the test's WireMock server. */
    public void baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public SourceDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(URI.create(baseUrl + PATH), STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        List<NormalizedRecord> records = new ArrayList<>();
        for (String id : new String(response.body(), StandardCharsets.UTF_8).split(",")) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty()) {
                records.add(NormalizedRecord.builder(trimmed, KIND).put("name", trimmed).build());
            }
        }
        ReferenceSupport.emit(ctx, records);
        return FetchResult.complete(List.of(), response.header("ETag"));
    }
}
