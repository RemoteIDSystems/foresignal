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

    /** Adapter-state key prefix of the {@code ETag} that {@link #getConditional} stores. */
    String ETAG_STATE_PREFIX = "etag:";

    /** Adapter-state key prefix of the {@code Last-Modified} that {@link #getConditional} stores. */
    String LAST_MODIFIED_STATE_PREFIX = "lastmod:";

    /**
     * The credential whose value, when a descriptor declares a credential of this name, is appended to the
     * User-Agent as {@code ; contact=<value>} (prompt 091: the SEC requires contact details), taking precedence
     * over the descriptor's {@code userAgentContact}. Operators set it like any other credential.
     */
    String USER_AGENT_CONTACT_CREDENTIAL = "contactEmail";

    /**
     * Forgets the validators {@link #getConditional} stored under {@code stateKey}, so the next request
     * is unconditional (prompt 047). An adapter calls it when a 200 body it was given could not be used
     * (truncated or unparseable) but the fetch still succeeds: the validators are recorded as soon as the
     * response arrives, and without this the next run would get a 304 and never see the content. Inside a
     * fetch run by the collector the recorded validators are staged with the fetch's other state, so a fetch
     * that throws discards them anyway (Adapter-Guide §7); calling this before throwing is harmless.
     */
    static void forgetValidators(AdapterStateStore state, String stateKey) {
        state.remove(ETAG_STATE_PREFIX + stateKey);
        state.remove(LAST_MODIFIED_STATE_PREFIX + stateKey);
    }

    /** A GET. */
    UpstreamResponse get(URI uri) throws AdapterException;

    /** A GET with extra request headers. */
    UpstreamResponse get(URI uri, Map<String, String> headers) throws AdapterException;

    /**
     * A GET that does not follow a redirect (prompt 091): a 3xx comes back as the response, with its
     * {@code Location} header, so a source can change-check a pre-signed redirect target cheaply. Budgeted
     * and metered like {@link #get(URI)}.
     *
     * <p>The default refuses (a hand-built test double); {@code UpstreamClient}'s implementation sends it.
     */
    default UpstreamResponse getNoFollow(URI uri) throws AdapterException {
        throw new UnsupportedOperationException("This SourceHttp cannot send a non-following GET");
    }

    /**
     * A GET that sends {@code If-None-Match} / {@code If-Modified-Since} from stored validators and
     * records the new ones under {@code stateKey}.
     *
     * <p>A 304 comes back as a response with {@code notModified() == true}. Some upstreams require this —
     * CelesTrak's fair-use policy among them — so it is part of the SPI rather than something each
     * adapter reimplements.
     */
    UpstreamResponse getConditional(URI uri, String stateKey) throws AdapterException;

    /**
     * {@link #getConditional(URI, String)} with extra request headers (prompt 070: Entur's mandatory
     * {@code ET-Client-Name}). The validators and the stored state are exactly those of the two-argument form;
     * a {@code User-Agent} among {@code headers} is ignored, so the request always carries Foresignal's own.
     *
     * <p>The default handles no headers only (a test double); {@code UpstreamClient}'s implementation sends them.
     *
     * @param headers extra request headers, possibly empty
     * @throws UnsupportedOperationException from the default when {@code headers} is not empty
     */
    default UpstreamResponse getConditional(URI uri, String stateKey, Map<String, String> headers)
            throws AdapterException {
        if (headers == null || headers.isEmpty()) {
            return getConditional(uri, stateKey);
        }
        throw new UnsupportedOperationException("This SourceHttp cannot send extra headers on a conditional GET");
    }

    /**
     * A HEAD request (prompt 079: {@code usdm} confirms a map date without downloading the map). Budgeted, metered
     * and traced like a GET; the response has headers and an empty body. Redirects follow the descriptor.
     *
     * <p>The default refuses (a test double); {@code UpstreamClient}'s implementation sends it.
     *
     * @throws UnsupportedOperationException from the default
     */
    default UpstreamResponse head(URI uri) throws AdapterException {
        throw new UnsupportedOperationException("This SourceHttp cannot send a HEAD request");
    }

    /** A POST. */
    UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers)
            throws AdapterException;

    /**
     * {@link #post(URI, String, byte[], Map)} whose response may take up to {@code requestTimeout} rather than the
     * client's default (20 s): for an upstream with a documented longer limit, such as the Wikidata SPARQL
     * endpoint's 60 s query timeout (prompt 094). The fetch's own {@code fetchTimeout} still bounds the run.
     *
     * <p>The default ignores the timeout (a hand-built test double); {@code UpstreamClient}'s implementation
     * applies it.
     */
    default UpstreamResponse post(URI uri, String contentType, byte[] body, Map<String, String> headers,
            java.time.Duration requestTimeout) throws AdapterException {
        return post(uri, contentType, body, headers);
    }

    /**
     * A ranged GET, for reading part of a large object.
     *
     * <p>Accepts only 206, or 200 when {@code start} is 0, and rejects a body shorter than the
     * range asked for. An upstream that ignores {@code Range} and sends the whole object would
     * otherwise be read as if it had honored it, and a GRIB message index would be parsed from the
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
     * Streams a body to a file, charging a known size against the byte budget first
     * (Weather-Imagery-Design §2.2, BudgetGovernor row).
     *
     * <p>{@code expectedBytes} normally comes from the S3 listing's {@code Size}. It is charged
     * before anything is sent, so an object the day's {@code mbPerDay} cannot cover is refused
     * without being paid for, and the charge is corrected to the bytes actually written afterwards.
     * A relative {@code target} is resolved under {@code ${FORESIGNAL_DATA_DIR}/rasters/tmp}.
     *
     * <p>The default ignores {@code expectedBytes}; {@code UpstreamClient}'s implementation honors it
     * (prompt 046).
     *
     * @param target where to write; the caller owns the file afterwards
     * @param maxBytes cap for this download
     * @param expectedBytes the known size to charge in advance, or 0 when unknown
     */
    default DownloadResult download(URI uri, Path target, long maxBytes, long expectedBytes) throws AdapterException {
        return download(uri, target, maxBytes);
    }

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
     * Consumes one unit of the request budget without sending a request (prompt 049): for work that
     * Weather-Imagery-Design §2.5 prices as a request although it is not one, such as a SatDump inbox
     * scan.
     *
     * <p>The default does nothing (a test double); {@code UpstreamClient}'s implementation takes the
     * unit and refuses with a budget error when it is spent.
     *
     * @param label what the unit pays for
     * @throws AdapterException when the budget is spent
     */
    default void chargeRequest(String label) throws AdapterException {}

    /**
     * Opens a long-lived HTTP response and hands its body stream back unread (prompt 095): the only
     * way a streaming adapter consumes Server-Sent Events, such as Wikimedia's
     * {@code recentchange} stream.
     *
     * <p>Counts as one budgeted request per connection attempt, after the backoff check, with
     * Foresignal's own User-Agent, exactly like {@link #get(URI)}. The caller owns the returned
     * stream and must close it. A non-2xx status is thrown as an upstream HTTP error, never returned
     * as a stream.
     *
     * <p>The default refuses (a hand-built test double); {@code UpstreamClient}'s implementation
     * sends it.
     *
     * @param uri the stream endpoint
     * @param headers extra request headers, possibly empty
     * @throws UnsupportedOperationException from the default
     */
    default HttpStream openStream(URI uri, Map<String, String> headers) throws AdapterException {
        throw new UnsupportedOperationException("This SourceHttp cannot open a streaming HTTP connection");
    }

    /**
     * Opens a WebSocket for a streaming adapter.
     *
     * <p>Counts as one budgeted request per connection attempt, so the descriptor's budget bounds
     * reconnect storms.
     */
    WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
            throws AdapterException;

    /**
     * Opens a WebSocket with {@code options} (prompt 063).
     *
     * <p>With {@link WebSocketOptions#perMessageDeflate()} the connection offers {@code permessage-deflate}
     * and the returned socket is a {@link MeteredWebSocket}. The budget unit, the backoff check, the
     * User-Agent and the request metering are exactly those of {@link #openWebSocket(URI, WebSocket.Listener,
     * Map)}; the listener receives whole messages ({@code last == true}) and gets the next one only after it
     * calls {@code request(1)}, as with the JDK client.
     *
     * <p>The default ignores {@code options} (a test double); {@code UpstreamClient}'s implementation honors
     * them.
     */
    default WebSocket openWebSocket(
            URI uri, WebSocket.Listener listener, Map<String, String> headers, WebSocketOptions options)
            throws AdapterException {
        return openWebSocket(uri, listener, headers);
    }
}
