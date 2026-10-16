package ai.foresignal.source.spi;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * What a polling adapter is given for one fetch.
 *
 * <p>Everything an adapter legitimately needs, and nothing else: no repository, no scheduler, no
 * other adapter. That is what keeps the dependency rule in Architecture §3 true, and what lets an
 * adapter be tested against a fixture with no Spring context at all.
 */
public interface FetchContext {

    /** The source being fetched. */
    String sourceId();

    /** The descriptor, for TTLs, limits and declared kinds. */
    SourceDescriptor descriptor();

    /**
     * The only path to the network.
     *
     * @throws UnsupportedOperationException for a {@link Mode#DERIVED} source, which makes no
     *     upstream requests
     */
    SourceHttp http();

    /** Validated parameters with defaults applied. */
    ValidatedParams params();

    /** Decrypted credentials keyed by credential field name; empty when the source needs none. */
    Map<String, String> credentials();

    /** Inject this rather than calling {@code Instant.now()}, so tests can control time. */
    Clock clock();

    /** When this source last succeeded, or empty if it never has. */
    Optional<Instant> lastSuccessAt();

    /** Per-source scratch state that survives restarts. */
    AdapterStateStore state();

    /**
     * The raster pipeline (Weather-Imagery-Design §2.1): the only way an adapter stores raster
     * frames. Adapters never touch the frame store directly.
     *
     * <p>Inherited by {@link StreamContext}. During a probe the returned sink is
     * {@linkplain RasterSink#probing() probing} and writes nothing.
     *
     * @throws UnsupportedOperationException when no raster pipeline is installed (a hand-built
     *     context in a unit test)
     */
    default RasterSink rasters() {
        throw new UnsupportedOperationException(
                "No raster pipeline is available to " + sourceId() + " in this context.");
    }

    /**
     * The media store (Weather-Imagery-Design §2.1, prompt 043): the only way an adapter stores
     * images (SatDump composites, thumbnails). Adapters never write image files themselves; the sink
     * fills in the source id and runs {@code ImageGuard} before anything is decoded.
     *
     * <p>Inherited by {@link StreamContext}. During a probe the returned sink is
     * {@linkplain MediaSink#probing() probing} and writes nothing.
     *
     * @throws UnsupportedOperationException when no media store is installed (a hand-built context in
     *     a unit test)
     */
    default MediaSink media() {
        throw new UnsupportedOperationException(
                "No media store is available to " + sourceId() + " in this context.");
    }

    /**
     * The inbox directory (prompt 049): the only way an adapter reads files under
     * {@code FORESIGNAL_INBOX_DIR}. Every read re-checks the realpath and refuses links.
     *
     * <p>Inherited by {@link StreamContext}.
     *
     * @throws UnsupportedOperationException when no inbox is installed (a hand-built context in a unit
     *     test)
     */
    default InboxAccess inbox() {
        throw new UnsupportedOperationException("No inbox is available to " + sourceId() + " in this context.");
    }

    /**
     * Consumes one unit of this source's request budget without sending anything (prompt 049): a
     * SatDump inbox scan costs one request (Weather-Imagery-Design §2.5), although it reads files
     * rather than the network.
     *
     * <p>The default delegates to {@link SourceHttp#chargeRequest}.
     *
     * @param label what the unit pays for, for logs and traces (for example {@code inbox-scan})
     * @throws AdapterException when the budget is spent; nothing should then be done
     */
    default void chargeRequest(String label) throws AdapterException {
        http().chargeRequest(label);
    }

    /**
     * Hands one chunk of a large complete-set fetch to the executor (prompt 094): the executor ingests each
     * chunk as its own batch before the adapter emits the next, so a dataset larger than the heap budget is
     * never held in memory at once. A chunk carries at most 5,000 records.
     *
     * <p>Only a chunked, complete-set run may use this; the executor installs the sink before calling
     * {@link PollingAdapter#fetch}. The default refuses, so a hand-built test context reports the misuse
     * instead of silently dropping records.
     *
     * @param records the chunk, at most 5,000 records
     * @throws UnsupportedOperationException when this context has no chunk sink
     */
    default void emitChunk(List<NormalizedRecord> records) {
        throw new UnsupportedOperationException("This context cannot accept chunks from " + sourceId());
    }

    /**
     * True when the operator released the shrink guard for this one run (prompt 094's
     * {@code POST /v1/sources/{id}/fetch?allowShrink=true}). An adapter that keeps its own delisting guard
     * (prompt 091's sanctions lists) honors it the same way the complete-set reconciliation does: the run
     * tombstones what disappeared even when the list shrank implausibly.
     *
     * <p>The default is false: a scheduled run, a probe or a hand-built test context never releases the guard.
     */
    default boolean allowShrink() {
        return false;
    }

    /**
     * The logger for this source, named {@code foresignal.source.<id>}, so an operator can raise
     * one source's log level without touching the rest.
     */
    default Logger log() {
        return LoggerFactory.getLogger("foresignal.source." + sourceId());
    }

    /** Convenience for {@code clock().instant()}. */
    default Instant now() {
        return clock().instant();
    }
}
