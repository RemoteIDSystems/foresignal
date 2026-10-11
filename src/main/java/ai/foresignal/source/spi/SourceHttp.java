package ai.foresignal.source.spi;

import java.net.URI;
import java.net.http.WebSocket;
import java.nio.file.Path;
import java.util.Map;

/**
 * The only way an adapter reaches the network (adapter rule 2).
 *
 * <p>Every call is budgeted, metered, capped and attributed to the source, which is what makes the
 * per-source spend guarantee enforceable rather than advisory. An adapter that built its own
 * {@code HttpClient} would bypass all of it, so the SPI gives adapters no way to do that.
 *
 * <p>Declared here, implemented in prompt 005.
 *
 * <p>For a {@link Mode#DERIVED} source this is unavailable: those make no upstream requests at all,
 * and {@code FetchContext.http()} throws rather than returning an instance.
 */
public interface SourceHttp {

    /** A GET. */
    UpstreamResponse get(URI uri) throws AdapterException;

    /** A GET with extra request headers. */
    UpstreamResponse get(URI uri, Map<String, String> headers) throws AdapterException;

    /**
     * A GET that sends {@code If-None-Match} / {@code If-Modified-Since} from stored validators and
     * records the new ones under {@code stateKey}.
     *
     * <p>A 304 comes back as a response with {@code notModified() == true}. Some upstreams require this —
     * CelesTrak's fair-use policy among them — so it is part of the SPI rather than something each
     * adapter reimplements.
     */
    UpstreamResponse getConditional(URI uri, String stateKey) throws AdapterException;

    /** A POST. */
    UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers)
            throws AdapterException;

    /**
     * A ranged GET, for reading part of a large object.
     *
     * <p>Accepts only 206, or 200 when {@code start} is 0, and rejects a body shorter than the
     * range asked for. An upstream that ignores {@code Range} and sends the whole object would
     * otherwise be read as if it had honoured it, and a GRIB message index would be parsed from the
     * wrong offset (Weather-Imagery-Design §2.2).
     *
     * <p>Follows no redirects: a redirect on a ranged read usually means the range was dropped.
     *
     * @param start first byte, inclusive
     * @param end last byte, inclusive
     * @param maxBytes cap for this read, overriding the descriptor's
     */
    UpstreamResponse getRange(URI uri, long start, long end, long maxBytes) throws AdapterException;

    /**
     * Streams a body to a file instead of into memory.
     *
     * @param target where to write; the caller owns the file afterwards
     * @param maxBytes cap for this download
     */
    DownloadResult download(URI uri, Path target, long maxBytes) throws AdapterException;

    /**
     * Lists a public S3 bucket anonymously (ListObjectsV2).
     *
     * <p>No credentials and no SDK: the buckets Foresignal reads — NOAA's, NASA's — are public, and the
     * XML is parsed with external entities disabled.
     *
     * @param startAfter continue after this key, or null for the first page
     */
    S3Listing listS3(String bucket, String prefix, String startAfter) throws AdapterException;

    /**
     * Opens a WebSocket for a streaming adapter.
     *
     * <p>Counts as one budgeted request per connection attempt, so the descriptor's budget bounds
     * reconnect storms.
     */
    WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
            throws AdapterException;
}
