package ai.foresignal.collect;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.DownloadResult;
import ai.foresignal.source.spi.S3Listing;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.WebSocketOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.GZIPInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link SourceHttp} bound to one source, created by {@link UpstreamClient}.
 *
 * <p>Every request goes through {@link #execute}, which is where the ordering matters:
 *
 * <ol>
 *   <li><strong>backoff first</strong>, because a request we are not going to send must not cost a
 *       budget token — otherwise an upstream throttling us would also drain our daily allowance;
 *   <li>then, for a source whose descriptor sets {@code hostPacing} (prompt 080), the per-host
 *       {@link HostPacer} gate, held until the response has been read, so requests to one host never
 *       overlap; a request that cannot get the gate within the bounded wait is not sent
 *       ({@link HostPacingTimeoutException});
 *   <li>then the budget, refusing rather than queueing;
 *   <li>then the request, with Foresignal's User-Agent and a byte cap.
 * </ol>
 */
class BoundSourceHttp implements SourceHttp {

    private static final Logger log = LoggerFactory.getLogger(BoundSourceHttp.class);

    static final String ETAG_STATE_PREFIX = SourceHttp.ETAG_STATE_PREFIX;
    static final String LAST_MODIFIED_STATE_PREFIX = SourceHttp.LAST_MODIFIED_STATE_PREFIX;

    /** OpenSky sends this instead of Retry-After. */
    static final String OPENSKY_RETRY_AFTER_HEADER = "x-rate-limit-retry-after-seconds";

    private final UpstreamClient parent;
    private final String sourceId;
    private final SourceDescriptor descriptor;
    private final RequestRecorder recorder;
    private final UrlRedactor redactor;
    private final PersistentAdapterStateStore state;

    /** Host -> the address its {@code LAN_URL} parameter resolved to during validation. */
    private final Map<String, InetAddress> pinnedHosts;

    /** Traces of this binding's requests, drained by the fetch executor in prompt 008. */
    private final List<FetchTrace> traces = new CopyOnWriteArrayList<>();

    BoundSourceHttp(
            UpstreamClient parent,
            String sourceId,
            SourceDescriptor descriptor,
            RequestRecorder recorder,
            UrlRedactor redactor,
            PersistentAdapterStateStore state,
            Map<String, InetAddress> pinnedHosts) {
        this.parent = parent;
        this.sourceId = sourceId;
        this.descriptor = descriptor;
        this.recorder = recorder;
        this.redactor = redactor;
        this.state = state;
        this.pinnedHosts = pinnedHosts == null ? Map.of() : Map.copyOf(pinnedHosts);
    }

    /** The traces recorded so far, oldest first. */
    List<FetchTrace> traces() {
        return List.copyOf(traces);
    }

    /** The adapter state backing conditional GET. */
    PersistentAdapterStateStore state() {
        return state;
    }

    // ---------- SourceHttp ----------

    @Override
    public UpstreamResponse get(URI uri) throws AdapterException {
        return get(uri, Map.of());
    }

    @Override
    public UpstreamResponse get(URI uri, Map<String, String> headers) throws AdapterException {
        return execute("GET", uri, headers, null, null, descriptor.maxResponseBytes(), descriptor.followRedirects(), 0);
    }

    @Override
    public UpstreamResponse getNoFollow(URI uri) throws AdapterException {
        return execute("GET", uri, Map.of(), null, null, descriptor.maxResponseBytes(), false, 0);
    }

    @Override
    public UpstreamResponse getConditional(URI uri, String stateKey) throws AdapterException {
        return getConditional(uri, stateKey, Map.of());
    }

    /** The conditional GET with the adapter's extra headers (prompt 070); the validators win over a duplicate. */
    @Override
    public UpstreamResponse getConditional(URI uri, String stateKey, Map<String, String> extraHeaders)
            throws AdapterException {
        Map<String, String> headers = new LinkedHashMap<>();
        if (extraHeaders != null) {
            extraHeaders.forEach((name, value) -> {
                if (!"if-none-match".equalsIgnoreCase(name) && !"if-modified-since".equalsIgnoreCase(name)) {
                    headers.put(name, value);
                }
            });
        }
        state.get(ETAG_STATE_PREFIX + stateKey).ifPresent(etag -> headers.put("If-None-Match", etag));
        state.get(LAST_MODIFIED_STATE_PREFIX + stateKey)
                .ifPresent(lastModified -> headers.put("If-Modified-Since", lastModified));

        UpstreamResponse response =
                execute("GET", uri, headers, null, null, descriptor.maxResponseBytes(), descriptor.followRedirects(), 0);

        if (response.status() == 200 && recorder == null) {
            // Store the new validators immediately; see PersistentAdapterStateStore for why this is
            // not tied to the fetch's transaction. A probe stores none (prompt 047): otherwise the next
            // real fetch would get a 304 and never ingest what only the probe saw.
            String etag = response.header("etag");
            if (etag != null) {
                state.put(ETAG_STATE_PREFIX + stateKey, etag);
            }
            String lastModified = response.header("last-modified");
            if (lastModified != null) {
                state.put(LAST_MODIFIED_STATE_PREFIX + stateKey, lastModified);
            }
        }
        return response;
    }

    /** A HEAD through the single request path: budgeted, metered and traced like a GET (prompt 079). */
    @Override
    public UpstreamResponse head(URI uri) throws AdapterException {
        return execute("HEAD", uri, Map.of(), null, null, descriptor.maxResponseBytes(), descriptor.followRedirects(), 0);
    }

    @Override
    public UpstreamResponse post(
            URI uri, String contentType, byte[] body, Map<String, String> headers)
            throws AdapterException {
        Map<String, String> merged = new LinkedHashMap<>(headers == null ? Map.of() : headers);
        if (contentType != null) {
            merged.put("Content-Type", contentType);
        }
        return execute("POST", uri, merged, body, null, descriptor.maxResponseBytes(), descriptor.followRedirects(), 0);
    }

    @Override
    public UpstreamResponse getRange(URI uri, long start, long end, long maxBytes)
            throws AdapterException {
        if (start < 0 || end < start) {
            throw new AdapterException("Invalid byte range " + start + "-" + end);
        }
        long requested = end - start + 1;
        Map<String, String> headers = Map.of("Range", "bytes=" + start + "-" + end);

        // No redirects: a redirect on a ranged read usually drops the Range header, and the result
        // would be a whole object silently parsed as if it were the requested slice.
        UpstreamResponse response =
                execute("GET", uri, headers, null, null, maxBytes, false, requested);

        boolean acceptable = response.status() == 206 || (response.status() == 200 && start == 0);
        if (!acceptable) {
            throw new UpstreamHttpException(
                    response.status(),
                    "a ranged read of "
                            + redactedUrl(uri)
                            + " returned "
                            + response.status()
                            + "; only 206, or 200 when the range starts at 0, is acceptable");
        }
        if (!response.truncated() && response.body().length < requested) {
            // Short of what was asked for: the upstream either ignored the range or the object is
            // smaller than the index claimed. Either way, parsing it would be wrong.
            throw new AdapterException(
                    "A ranged read of "
                            + redactedUrl(uri)
                            + " returned "
                            + response.body().length
                            + " bytes for a "
                            + requested
                            + "-byte range");
        }
        return response;
    }

    @Override
    public DownloadResult download(URI uri, Path target, long maxBytes) throws AdapterException {
        return download(uri, target, maxBytes, 0);
    }

    /**
     * Streams a body to a file, charging {@code expectedBytes} against the byte budget in advance
     * and correcting the charge to the bytes written (prompt 046). A relative {@code target} lands
     * under {@code rasters/tmp}.
     */
    @Override
    public DownloadResult download(URI uri, Path requestedTarget, long maxBytes, long expectedBytes)
            throws AdapterException {
        Instant startedAt = parent.clock().instant();
        checkBackoff();
        long charged = Math.max(0, expectedBytes);
        consumeBudget(charged);

        // A bare or relative name is a temporary file of the raster pipeline, never a path relative
        // to the process's working directory.
        Path target = requestedTarget.isAbsolute() ? requestedTarget : parent.rasterTempDir().resolve(requestedTarget);
        HttpRequest request = requestBuilder(uri, Map.of()).GET().build();
        try {
            Files.createDirectories(target.getParent() == null ? parent.rasterTempDir() : target.getParent());
            HttpResponse<InputStream> response =
                    parent.client(true).send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() >= 400) {
                if (charged > 0) {
                    // Nothing was transferred, so nothing stays charged.
                    parent.budgets().correctByteCharge(sourceId, charged, 0);
                }
                handleErrorStatus(response, startedAt, "GET", uri, 0);
            }

            MessageDigest digest = sha256();
            long written;
            try (InputStream in = decode(response, response.body());
                    OutputStream out =
                            Files.newOutputStream(
                                    target,
                                    StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING,
                                    StandardOpenOption.WRITE)) {
                written = copy(in, out, maxBytes, digest);
            }

            parent.usage().record(sourceId, written, true);
            if (charged > 0) {
                parent.budgets().correctByteCharge(sourceId, charged, written);
            }
            parent.backoff().onSuccess(sourceId);
            String contentType = response.headers().firstValue("content-type").orElse(null);
            String sha = HexFormat.of().formatHex(digest.digest());
            traces.add(
                    new FetchTrace(
                            sourceId,
                            "GET",
                            redactedUrl(uri),
                            response.statusCode(),
                            response.headers().firstValue("etag").orElse(null),
                            written,
                            sha,
                            false,
                            false,
                            Duration.between(startedAt, parent.clock().instant()),
                            startedAt,
                            null));
            return new DownloadResult(target, written, contentType, sha);
        } catch (IOException e) {
            parent.usage().record(sourceId, 0, false);
            if (charged > 0) {
                parent.budgets().correctByteCharge(sourceId, charged, 0);
            }
            throw UpstreamClient.io("Could not download " + redactedUrl(uri) + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw UpstreamClient.io("Interrupted downloading " + redactedUrl(uri), e);
        }
    }

    @Override
    public S3Listing listS3(String bucket, String prefix, String startAfter) throws AdapterException {
        StringBuilder query = new StringBuilder("?list-type=2");
        if (prefix != null && !prefix.isBlank()) {
            query.append("&prefix=").append(urlEncode(prefix));
        }
        if (startAfter != null && !startAfter.isBlank()) {
            query.append("&start-after=").append(urlEncode(startAfter));
        }
        URI uri = parent.s3ListUri(bucket, query.toString());

        // No redirects: a bucket listing redirect usually means a region mismatch, which should
        // surface rather than be silently followed to a different bucket.
        UpstreamResponse response =
                execute("GET", uri, Map.of(), null, null, descriptor.maxResponseBytes(), false, 0);
        return S3ListingParser.parse(response.bodyAsString());
    }

    @Override
    public WebSocket openWebSocket(URI uri, WebSocket.Listener listener, Map<String, String> headers)
            throws AdapterException {
        checkBackoff();
        // One budget unit per connection attempt, which is what bounds a reconnect storm: a stream
        // that keeps dropping burns its budget rather than hammering the upstream.
        consumeBudget(0);

        WebSocket.Builder builder =
                parent.client(true).newWebSocketBuilder().connectTimeout(UpstreamClient.CONNECT_TIMEOUT);
        builder.header("User-Agent", parent.userAgent(descriptor));
        if (headers != null) {
            headers.forEach(
                    (name, value) -> {
                        if (!"user-agent".equalsIgnoreCase(name)) {
                            builder.header(name, value);
                        }
                    });
        }
        try {
            WebSocket socket = builder.buildAsync(uri, listener).join();
            parent.usage().record(sourceId, 0, true);
            parent.backoff().onSuccess(sourceId);
            return socket;
        } catch (RuntimeException e) {
            parent.usage().record(sourceId, 0, false);
            throw UpstreamClient.io(
                    "Could not open a WebSocket to " + redactedUrl(uri) + ": " + e.getMessage(), e);
        }
    }

    /**
     * Opens a WebSocket with {@code options} (prompt 063).
     *
     * <p>Without {@code permessage-deflate} this is {@link #openWebSocket(URI, WebSocket.Listener, Map)}.
     * With it, the connection is opened by {@link DeflateWebSocket}, after the same backoff check and budget
     * unit, with the same User-Agent and the same request metering and backoff bookkeeping; the largest frame
     * accepted is the descriptor's {@code maxResponseBytes}. Like the JDK path, no {@link FetchTrace} is
     * recorded for a connection.
     */
    @Override
    public WebSocket openWebSocket(
            URI uri, WebSocket.Listener listener, Map<String, String> headers, WebSocketOptions options)
            throws AdapterException {
        if (options == null || !options.perMessageDeflate()) {
            return openWebSocket(uri, listener, headers);
        }
        checkBackoff();
        consumeBudget(0);

        Map<String, String> handshake = new LinkedHashMap<>();
        if (headers != null) {
            headers.forEach(
                    (name, value) -> {
                        if (!"user-agent".equalsIgnoreCase(name)) {
                            handshake.put(name, value);
                        }
                    });
        }
        handshake.put("User-Agent", parent.userAgent(descriptor));
        try {
            WebSocket socket = DeflateWebSocket.open(
                    uri, listener, handshake, UpstreamClient.CONNECT_TIMEOUT, descriptor.maxResponseBytes());
            parent.usage().record(sourceId, 0, true);
            parent.backoff().onSuccess(sourceId);
            return socket;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            parent.usage().record(sourceId, 0, false);
            throw UpstreamClient.io("Interrupted while opening a WebSocket to " + redactedUrl(uri), e);
        } catch (IOException | RuntimeException e) {
            parent.usage().record(sourceId, 0, false);
            throw UpstreamClient.io(
                    "Could not open a WebSocket to " + redactedUrl(uri) + ": " + e.getMessage(), e);
        }
    }

    /**
     * Takes one budget unit and meters it as a successful zero-byte request (prompt 049). No backoff
     * check: nothing is sent upstream, so an upstream's throttling has no say over it.
     */
    @Override
    public void chargeRequest(String label) throws AdapterException {
        consumeBudget(0);
        parent.usage().record(sourceId, 0, true);
        log.debug("{}: charged one budget unit for {}", sourceId, label);
    }

    // ---------- the single request path ----------

    /**
     * Sends one request, applying every policy in order.
     *
     * @param allowRedirects false for ranged reads, S3 listings and pinned LAN requests
     * @param expectedBytes a known size to charge against a byte budget in advance, or 0
     */
    private UpstreamResponse execute(
            String method,
            URI uri,
            Map<String, String> headers,
            byte[] body,
            String contentType,
            long maxBytes,
            boolean allowRedirects,
            long expectedBytes)
            throws AdapterException {

        // 1. Backoff before budget: see the class note.
        checkBackoff();

        // 2. Host pacing (prompt 080): wait for the host's gate before the budget, so a request that is
        //    never sent costs nothing. The gate is held until the response has been read.
        HostPacer.Permit permit = pace(uri);
        try {
            // 3. Budget. A refusal sends nothing.
            consumeBudget(expectedBytes);
            return send(method, uri, headers, body, contentType, maxBytes, allowRedirects, expectedBytes);
        } finally {
            if (permit != null) {
                permit.close();
            }
        }
    }

    /**
     * Waits for this source's host gate when its descriptor sets {@code hostPacing} (prompt 080).
     *
     * @return the held gate, or null when the source is not paced
     * @throws HostPacingTimeoutException when the gate could not be had within the bounded wait
     */
    private HostPacer.Permit pace(URI uri) throws AdapterException {
        if (descriptor.hostPacing() == null) {
            return null;
        }
        HostPacer pacer = parent.hostPacer();
        try {
            return pacer.acquire(uri.getHost(), descriptor.hostPacing())
                    .orElseThrow(() -> new HostPacingTimeoutException(
                            sourceId, uri.getHost(), pacer.maxWait(), parent.clock().instant()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw UpstreamClient.io("Interrupted waiting for the request slot on " + uri.getHost(), e);
        }
    }

    /** Sends one request whose backoff, pacing and budget have been checked. */
    private UpstreamResponse send(
            String method,
            URI uri,
            Map<String, String> headers,
            byte[] body,
            String contentType,
            long maxBytes,
            boolean allowRedirects,
            long expectedBytes)
            throws AdapterException {

        Instant startedAt = parent.clock().instant();

        HttpRequest.Builder builder = requestBuilder(uri, headers);
        if ("POST".equals(method)) {
            builder.POST(
                    body == null
                            ? HttpRequest.BodyPublishers.noBody()
                            : HttpRequest.BodyPublishers.ofByteArray(body));
        } else if ("HEAD".equals(method)) {
            builder.method("HEAD", HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }
        HttpRequest request = builder.build();

        try {
            // A pinned LAN request never follows a redirect: following one would re-resolve a
            // host and undo the pinning.
            HttpResponse<InputStream> response =
                    parent.client(allowRedirects && !isPinned(uri))
                            .send(request, HttpResponse.BodyHandlers.ofInputStream());

            int status = response.statusCode();

            if (status == 304) {
                // Still a request, still metered as a success: it cost the upstream a round trip,
                // and counting it keeps the budget honest.
                parent.usage().record(sourceId, 0, true);
                parent.backoff().onSuccess(sourceId);
                UpstreamResponse result = UpstreamResponse.notModified(headerMap(response));
                recordTrace(method, uri, startedAt, status, result, null, null);
                recordExchange(method, uri, headers, startedAt, status, result, null);
                return result;
            }

            MessageDigest digest = sha256();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            boolean truncated;
            try (InputStream in = decode(response, response.body())) {
                long read = copy(in, buffer, maxBytes, digest);
                // Only a body that filled the cap *and* has more waiting is truncated; one that
                // happens to be exactly maxBytes long is complete.
                truncated = read >= maxBytes && in.read() != -1;
            }
            byte[] bytes = buffer.toByteArray();
            String bodySha = bytes.length == 0 ? null : HexFormat.of().formatHex(digest.digest());

            parent.usage().record(sourceId, bytes.length, status < 400);
            if (expectedBytes > 0) {
                parent.budgets().correctByteCharge(sourceId, expectedBytes, bytes.length);
            }

            UpstreamResponse result =
                    new UpstreamResponse(
                            status,
                            headerMap(response),
                            bytes,
                            response.headers().firstValue("content-type").orElse(null),
                            truncated,
                            false);

            recordTrace(method, uri, startedAt, status, result, bodySha, null);
            recordExchange(method, uri, headers, startedAt, status, result, null);

            if (status >= 400) {
                throwForStatus(status, response, result);
            }
            parent.backoff().onSuccess(sourceId);
            return result;

        } catch (IOException e) {
            parent.usage().record(sourceId, 0, false);
            recordFailure(method, uri, headers, startedAt, e);
            throw UpstreamClient.io(
                    method + " " + redactedUrl(uri) + " failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            parent.usage().record(sourceId, 0, false);
            recordFailure(method, uri, headers, startedAt, e);
            throw UpstreamClient.io("Interrupted during " + method + " " + redactedUrl(uri), e);
        }
    }

    private void checkBackoff() throws InBackoffException {
        if (parent.backoff().isInBackoff(sourceId)) {
            throw new InBackoffException(sourceId, parent.backoff().backoffUntil(sourceId));
        }
    }

    private void consumeBudget(long expectedBytes) throws BudgetExceededException {
        boolean granted =
                expectedBytes > 0
                        ? parent.budgets().tryConsume(sourceId, expectedBytes)
                        : parent.budgets().tryConsume(sourceId);
        if (!granted) {
            parent.usage().recordRefused(sourceId);
            BudgetStatus status = parent.budgets().status(sourceId);
            throw new BudgetExceededException(
                    sourceId,
                    "The budget for "
                            + sourceId
                            + " is spent: "
                            + status.minuteRemaining()
                            + " left this minute, "
                            + status.remainingToday()
                            + " left today of "
                            + status.perDay()
                            + ". The request was not sent.");
        }
    }

    /**
     * Builds a request with Foresignal's headers.
     *
     * <p>A caller cannot override the User-Agent: identifying ourselves honestly is what lets an
     * upstream rate-limit or contact one install instead of blocking every Foresignal user. Only the
     * descriptor may override it, for an upstream that demands a specific format.
     */
    private HttpRequest.Builder requestBuilder(URI uri, Map<String, String> headers) {
        PinnedTarget target = PinnedTarget.of(uri, pinnedAddress(uri));
        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(target.uri())
                        .timeout(UpstreamClient.DEFAULT_REQUEST_TIMEOUT)
                        .header("User-Agent", parent.userAgent(descriptor))
                        .header("Accept-Encoding", "gzip");
        target.hostHeader().ifPresent(host -> setHostHeader(builder, host));

        if (headers != null) {
            headers.forEach(
                    (name, value) -> {
                        if ("user-agent".equalsIgnoreCase(name) || "accept-encoding".equalsIgnoreCase(name)) {
                            log.debug("Ignoring a caller-supplied {} header for {}", name, sourceId);
                            return;
                        }
                        builder.header(name, value);
                    });
        }
        return builder;
    }

    /**
     * Sets the {@code Host} header, or carries on without it.
     *
     * <p>The JDK allows this header only when {@code jdk.httpclient.allowRestrictedHeaders} named
     * it before its HTTP utilities initialized, which something else in the JVM can win. Losing the
     * header costs a courtesy to a receiver serving several names; failing the fetch would cost the
     * data, and the pinned address — the actual protection — is already in the URI.
     */
    private void setHostHeader(HttpRequest.Builder builder, String host) {
        try {
            builder.header("Host", host);
        } catch (IllegalArgumentException e) {
            log.warn(
                    "Could not send Host: {} for {}; the JDK's restricted-header property was read"
                            + " before Foresignal set it. Connecting to the pinned address regardless.",
                    host,
                    sourceId);
        }
    }

    /**
     * The address a {@code LAN_URL} parameter resolved to, when this URI names one of its hosts.
     *
     * <p>Empty for ordinary URLs. Looked up by host because an adapter may declare several LAN
     * parameters and a request only says which host it is for.
     */
    private Optional<InetAddress> pinnedAddress(URI uri) {
        if (pinnedHosts.isEmpty() || uri.getHost() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(pinnedHosts.get(uri.getHost().toLowerCase()));
    }

    /** True when this URI is a pinned LAN host. Such requests must not follow redirects. */
    private boolean isPinned(URI uri) {
        return pinnedAddress(uri).isPresent();
    }

    // ---------- status handling ----------

    private void throwForStatus(int status, HttpResponse<InputStream> response, UpstreamResponse result)
            throws AdapterException {
        String text = result.bodyAsString();
        String snippet = UpstreamHttpException.snippet(text);
        throw errorFor(status, response, snippet, text);
    }

    /**
     * Applies the backoff an error status earns and returns the exception to throw (prompts 005 and 062).
     *
     * <ul>
     *   <li><strong>Refused client:</strong> a 451, or a 403 from a source whose descriptor sets
     *       {@code accessDeniedIsTerminal}, puts the source in a one-hour backoff and becomes
     *       {@link UpstreamAccessDeniedException}. Nothing else is tried: no other User-Agent, no browser
     *       headers, no other egress. When the body contains a key of the descriptor's
     *       {@code accessDeniedReasons} (prompt 080), the exception carries that reason.
     *   <li><strong>{@code Retry-After}</strong> (or OpenSky's {@code X-Rate-Limit-Retry-After-Seconds}) on
     *       any 4xx or 5xx is honored, clamped to [5 s, 1 h] by {@link BackoffRegistry}.
     *   <li><strong>429 without the header:</strong> the descriptor's {@code defaultRetryAfter} when it sets
     *       one (30 s for adsb.lol), else the exponential ladder; a 503 without it uses the ladder.
     *   <li><strong>Challenge:</strong> a response with {@code cf-mitigated: challenge} (prompt 082) carries the
     *       reason {@link UpstreamHttpException#REASON_BLOCKED_CHALLENGE}. The challenge is never solved and the
     *       request never retried with other headers.
     * </ul>
     */
    private UpstreamHttpException errorFor(
            int status, HttpResponse<InputStream> response, String snippet, String body) {
        if (status == 451 || (status == 403 && descriptor.accessDeniedIsTerminal())) {
            parent.backoff().onThrottled(sourceId, Optional.of(UpstreamAccessDeniedException.REFUSAL_BACKOFF));
            return new UpstreamAccessDeniedException(status, snippet, refusalDetail(body));
        }
        Optional<Duration> retryAfter = retryAfterFrom(response);
        if (retryAfter.isPresent()) {
            parent.backoff().onThrottled(sourceId, retryAfter);
        } else if (status == 429 && descriptor.defaultRetryAfter() != null) {
            parent.backoff().onThrottled(sourceId, Optional.of(descriptor.defaultRetryAfter()));
        } else if (status == 429 || status == 503) {
            parent.backoff().onThrottled(sourceId, Optional.empty());
        }
        boolean challenge = response != null && response.headers().firstValue("cf-mitigated")
                .filter(value -> value.strip().equalsIgnoreCase("challenge")).isPresent();
        return new UpstreamHttpException(status, snippet,
                challenge ? UpstreamHttpException.REASON_BLOCKED_CHALLENGE : null);
    }

    /**
     * The descriptor's refusal reason whose key the body contains (prompt 080), or null.
     *
     * @param body the refusal's response body, possibly empty
     */
    private String refusalDetail(String body) {
        if (body == null || descriptor.accessDeniedReasons().isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> reason : descriptor.accessDeniedReasons().entrySet()) {
            if (body.contains(reason.getKey())) {
                return reason.getValue();
            }
        }
        return null;
    }

    private void handleErrorStatus(
            HttpResponse<InputStream> response, Instant startedAt, String method, URI uri, long expected)
            throws AdapterException {
        int status = response.statusCode();
        parent.usage().record(sourceId, 0, false);
        UpstreamHttpException error = errorFor(status, response, "", "");
        traces.add(
                new FetchTrace(
                        sourceId,
                        method,
                        redactedUrl(uri),
                        status,
                        null,
                        0,
                        null,
                        false,
                        false,
                        Duration.between(startedAt, parent.clock().instant()),
                        startedAt,
                        "HTTP " + status));
        throw error;
    }

    /**
     * The upstream's requested delay.
     *
     * <p>Checks {@code Retry-After} and then OpenSky's {@code X-Rate-Limit-Retry-After-Seconds},
     * which it sends instead.
     */
    private Optional<Duration> retryAfterFrom(HttpResponse<InputStream> response) {
        Optional<Duration> standard =
                response.headers().firstValue("retry-after").flatMap(parent.backoff()::parseRetryAfter);
        if (standard.isPresent()) {
            return standard;
        }
        return response
                .headers()
                .firstValue(OPENSKY_RETRY_AFTER_HEADER)
                .flatMap(parent.backoff()::parseRetryAfter);
    }

    // ---------- body handling ----------

    /**
     * Decompresses when the upstream used gzip.
     *
     * <p>{@code HttpClient} does not do this: it sends whatever {@code Accept-Encoding} we set and
     * hands back the raw bytes, so asking for gzip without decoding it would give every adapter a
     * binary blob.
     */
    private InputStream decode(HttpResponse<InputStream> response, InputStream body) throws IOException {
        String encoding = response.headers().firstValue("content-encoding").orElse("");
        return encoding.toLowerCase().contains("gzip") ? new GZIPInputStream(body) : body;
    }

    /**
     * Copies up to {@code maxBytes}, hashing as it goes.
     *
     * <p>Capped because the body size is the upstream's choice and the heap is ours; hashed while
     * streaming so a change can be detected without keeping the body.
     */
    private static long copy(InputStream in, OutputStream out, long maxBytes, MessageDigest digest)
            throws IOException {
        byte[] chunk = new byte[16 * 1024];
        long total = 0;
        while (total < maxBytes) {
            int wanted = (int) Math.min(chunk.length, maxBytes - total);
            int read = in.read(chunk, 0, wanted);
            if (read < 0) {
                break;
            }
            out.write(chunk, 0, read);
            digest.update(chunk, 0, read);
            total += read;
        }
        return total;
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required of every JVM", e);
        }
    }

    private static Map<String, List<String>> headerMap(HttpResponse<?> response) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        response.headers().map().forEach((name, values) -> headers.put(name.toLowerCase(), List.copyOf(values)));
        return headers;
    }

    // ---------- recording ----------

    private void recordTrace(
            String method,
            URI uri,
            Instant startedAt,
            int status,
            UpstreamResponse result,
            String bodySha256,
            String failure) {
        traces.add(
                new FetchTrace(
                        sourceId,
                        method,
                        redactedUrl(uri),
                        status,
                        result.header("etag"),
                        result.body().length,
                        bodySha256,
                        result.truncated(),
                        result.notModified(),
                        Duration.between(startedAt, parent.clock().instant()),
                        startedAt,
                        failure));
    }

    private void recordExchange(
            String method,
            URI uri,
            Map<String, String> requestHeaders,
            Instant startedAt,
            int status,
            UpstreamResponse result,
            String failure) {
        if (recorder == null) {
            return;
        }
        recorder.record(
                new RecordedExchange(
                        method,
                        redactedUrl(uri),
                        redactHeaders(requestHeaders),
                        status,
                        redactHeaderLists(result.headers()),
                        result.contentType(),
                        result.body().length,
                        result.truncated(),
                        recorder.capBody(UrlRedactor.redactTokenMembers(redact(result.bodyAsString()))),
                        Duration.between(startedAt, parent.clock().instant()),
                        failure));
    }

    private void recordFailure(
            String method, URI uri, Map<String, String> headers, Instant startedAt, Exception e) {
        traces.add(
                FetchTrace.notSent(
                        sourceId, method, redactedUrl(uri), startedAt, e.getClass().getSimpleName()));
        if (recorder != null) {
            recorder.record(
                    new RecordedExchange(
                            method,
                            redactedUrl(uri),
                            redactHeaders(headers),
                            0,
                            Map.of(),
                            null,
                            0,
                            false,
                            null,
                            Duration.between(startedAt, parent.clock().instant()),
                            e.getMessage()));
        }
    }

    /** The URL with credential values masked, or a placeholder when redaction is unavailable. */
    private String redactedUrl(URI uri) {
        if (redactor == null) {
            return "(url withheld: credentials could not be loaded for redaction)";
        }
        return redactor.redact(uri);
    }

    private String redact(String text) {
        return redactor == null ? null : redactor.redact(text);
    }

    private Map<String, List<String>> redactHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Map.of();
        }
        Map<String, List<String>> result = new LinkedHashMap<>();
        headers.forEach(
                (name, value) ->
                        result.put(
                                name,
                                List.of(
                                        redactor == null
                                                ? UrlRedactor.MASK
                                                : redactor.redactHeader(name, value))));
        return result;
    }

    private Map<String, List<String>> redactHeaderLists(Map<String, List<String>> headers) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        headers.forEach(
                (name, values) -> {
                    List<String> redacted = new ArrayList<>(values.size());
                    for (String value : values) {
                        redacted.add(
                                redactor == null ? UrlRedactor.MASK : redactor.redactHeader(name, value));
                    }
                    result.put(name, List.copyOf(redacted));
                });
        return result;
    }

    private static String urlEncode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * A URI rewritten to connect to a pinned address, with the original Host preserved.
     *
     * @param uri what to connect to
     * @param hostHeader the Host header to send, when it differs from the URI's authority
     */
    record PinnedTarget(URI uri, Optional<String> hostHeader) {

        /** Rewrites {@code original} to {@code pinned} when an address was pinned. */
        static PinnedTarget of(URI original, Optional<InetAddress> pinned) {
            if (pinned.isEmpty()) {
                return new PinnedTarget(original, Optional.empty());
            }
            String originalAuthority = original.getAuthority();
            String literal = pinned.get().getHostAddress();
            // IPv6 literals need brackets in an authority.
            String hostPart = literal.contains(":") ? "[" + literal + "]" : literal;
            try {
                URI rewritten =
                        new URI(
                                original.getScheme(),
                                original.getUserInfo(),
                                hostPart,
                                original.getPort(),
                                original.getPath(),
                                original.getQuery(),
                                original.getFragment());
                return new PinnedTarget(rewritten, Optional.ofNullable(originalAuthority));
            } catch (URISyntaxException e) {
                // Cannot pin; connect normally rather than failing the fetch.
                return new PinnedTarget(original, Optional.empty());
            }
        }
    }
}
