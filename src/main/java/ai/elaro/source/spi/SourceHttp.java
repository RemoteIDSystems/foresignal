package ai.elaro.source.spi;

import java.net.URI;
import java.net.http.WebSocket;
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
     * Opens a WebSocket for a streaming adapter.
     *
     * <p>Counts as one budgeted request per connection attempt, so the descriptor's budget bounds
     * reconnect storms.
     */
    WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
            throws AdapterException;
}
