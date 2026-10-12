package ai.foresignal.source.spi;

import java.time.Clock;
import java.time.Instant;
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
