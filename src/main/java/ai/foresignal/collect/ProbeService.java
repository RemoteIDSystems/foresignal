package ai.foresignal.collect;

import ai.foresignal.api.error.BudgetExceededProblem;
import ai.foresignal.api.error.ConflictException;
import ai.foresignal.api.error.ValidationException;
import ai.foresignal.source.SourceRegistry;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * The operator's diagnostic dry run (Product Charter FR-7; {@code POST /v1/sources/{id}/probe},
 * whose endpoint arrives in prompt 013).
 *
 * <p>A probe <strong>never ingests, never writes {@code fetch_log} and never updates health</strong>;
 * it does consume budget, because it really asks the upstream. It shows what the upstream returned
 * (the recorded exchanges, credentials redacted, bodies cut at {@code maxBytes}) and what the adapter
 * made of it (up to 100 previews). A parse failure fills {@code parseError} and still returns the
 * exchanges, which is the case a probe exists for.
 *
 * <ul>
 *   <li>Polling: one {@code adapter.fetch} with a {@link RequestRecorder}, under the descriptor's
 *       {@code fetchTimeout}.
 *   <li>Streaming: the stream is started with a capturing sink for {@code seconds}, then closed.
 *   <li>Push and derived sources have no upstream to probe: 409 {@code NOT_SUPPORTED_FOR_MODE}.
 * </ul>
 */
@Service
public class ProbeService {

    private static final Logger log = LoggerFactory.getLogger(ProbeService.class);

    /** The default body cap, from the API. */
    public static final int DEFAULT_MAX_BYTES = 262_144;

    /** The smallest body cap the API accepts. */
    public static final int MIN_MAX_BYTES = 1024;

    /** The largest body cap the API accepts. */
    public static final int MAX_MAX_BYTES = 10_485_760;

    /** The default stream capture window, in seconds. */
    public static final int DEFAULT_SECONDS = 10;

    /** The longest stream capture window, in seconds. */
    public static final int MAX_SECONDS = 60;

    private final SourceRegistry registry;
    private final FetchContextFactory contexts;
    private final FetchDispatcher dispatcher;
    private final HealthTracker health;

    /** Creates the service. */
    public ProbeService(
            SourceRegistry registry, FetchContextFactory contexts, FetchDispatcher dispatcher, HealthTracker health) {
        this.registry = registry;
        this.contexts = contexts;
        this.dispatcher = dispatcher;
        this.health = health;
    }

    /**
     * Probes {@code sourceId}.
     *
     * @param maxBytes the body cap, {@value #MIN_MAX_BYTES}..{@value #MAX_MAX_BYTES}; null for the
     *     default
     * @param seconds the stream capture window, 1..{@value #MAX_SECONDS}; null for the default
     * @throws ai.foresignal.api.error.NotFoundException (404) for an unknown source
     * @throws ValidationException (400) for an out-of-range argument
     * @throws ConflictException (409) {@code NOT_SUPPORTED_FOR_MODE} for push and derived sources
     * @throws BudgetExceededProblem (429) when the budget refused the request
     */
    public ProbeResult probe(String sourceId, Integer maxBytes, Integer seconds) {
        SourceRegistry.Registration registration = registry.get(sourceId);
        int cap = maxBytes == null ? DEFAULT_MAX_BYTES : maxBytes;
        int window = seconds == null ? DEFAULT_SECONDS : seconds;
        if (cap < MIN_MAX_BYTES || cap > MAX_MAX_BYTES) {
            throw ValidationException.forField(
                    "maxBytes", "must be between " + MIN_MAX_BYTES + " and " + MAX_MAX_BYTES);
        }
        if (window < 1 || window > MAX_SECONDS) {
            throw ValidationException.forField("seconds", "must be between 1 and " + MAX_SECONDS);
        }
        SourceDescriptor descriptor = registration.descriptor();
        SourceAdapter adapter = registration.adapterIfPresent().orElse(null);
        if (descriptor.mode() == Mode.POLLING && adapter instanceof PollingAdapter polling) {
            return probePolling(descriptor, polling, cap);
        }
        if (descriptor.mode() == Mode.STREAMING && adapter instanceof StreamingAdapter streaming) {
            return probeStreaming(descriptor, streaming, cap, Duration.ofSeconds(window));
        }
        throw new ConflictException(
                ConflictException.NOT_SUPPORTED_FOR_MODE,
                "'" + sourceId + "' is a " + descriptor.mode().wireValue()
                        + " source; it has no upstream to probe.");
    }

    private ProbeResult probePolling(SourceDescriptor descriptor, PollingAdapter adapter, int maxBytes) {
        RequestRecorder recorder = new RequestRecorder(maxBytes, RequestRecorder.DEFAULT_MAX_EXCHANGES);
        DefaultFetchContext ctx = contexts.forFetch(descriptor, recorder);
        Duration timeout =
                descriptor.fetchTimeout() == null ? SourceDescriptor.DEFAULT_FETCH_TIMEOUT : descriptor.fetchTimeout();
        List<NormalizedRecordPreview> previews = List.of();
        String parseError = null;
        try {
            FetchResult result = dispatcher.call(descriptor, () -> adapter.fetch(ctx), timeout);
            previews = previews(result == null ? List.of() : result.records());
        } catch (Exception e) {
            if (FetchExecutor.causeOf(e, BudgetExceededException.class) != null) {
                throw new BudgetExceededProblem(
                        "The budget for '" + descriptor.id() + "' is spent; the probe was not sent.");
            }
            parseError = health.errorMessage(descriptor.id(), e);
        }
        return new ProbeResult(descriptor.id(), requests(recorder, maxBytes), previews, parseError);
    }

    private ProbeResult probeStreaming(
            SourceDescriptor descriptor, StreamingAdapter adapter, int maxBytes, Duration window) {
        RequestRecorder recorder = new RequestRecorder(maxBytes, RequestRecorder.DEFAULT_MAX_EXCHANGES);
        DefaultStreamContext ctx = contexts.forStream(descriptor, recorder, DefaultStreamContext.Callbacks.none());
        CapturingSink sink = new CapturingSink();
        Duration timeout =
                descriptor.fetchTimeout() == null ? SourceDescriptor.DEFAULT_FETCH_TIMEOUT : descriptor.fetchTimeout();
        String parseError = null;
        StreamHandle handle = null;
        try {
            handle = dispatcher.call(descriptor, () -> adapter.start(ctx, sink), timeout);
            TimeUnit.MILLISECONDS.sleep(window.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            parseError = "interrupted";
        } catch (Exception e) {
            if (FetchExecutor.causeOf(e, BudgetExceededException.class) != null) {
                throw new BudgetExceededProblem(
                        "The budget for '" + descriptor.id() + "' is spent; the probe was not sent.");
            }
            parseError = health.errorMessage(descriptor.id(), e);
        } finally {
            if (handle != null) {
                try {
                    handle.close();
                } catch (RuntimeException e) {
                    log.debug("{}: closing the probe stream failed", descriptor.id(), e);
                }
            }
        }
        return new ProbeResult(descriptor.id(), requests(recorder, maxBytes), previews(sink.records()), parseError);
    }

    private static List<ProbeResult.Request> requests(RequestRecorder recorder, int maxBytes) {
        return recorder.exchanges().stream().map(exchange -> ProbeResult.Request.of(exchange, maxBytes)).toList();
    }

    private static List<NormalizedRecordPreview> previews(List<NormalizedRecord> records) {
        return records.stream().limit(ProbeResult.MAX_RECORDS).map(NormalizedRecordPreview::of).toList();
    }

    /** Keeps the first 100 records a probe stream delivers and ignores the rest. Never blocks. */
    static final class CapturingSink implements RecordSink {

        private final List<NormalizedRecord> records = new ArrayList<>();

        @Override
        public synchronized void accept(NormalizedRecord record) {
            if (records.size() < ProbeResult.MAX_RECORDS) {
                records.add(record);
            }
        }

        synchronized List<NormalizedRecord> records() {
            return List.copyOf(records);
        }
    }
}
