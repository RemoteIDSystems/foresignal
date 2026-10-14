package ai.foresignal.source.adapters.wikistream;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.CredentialsRejectedException;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.HttpStream;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.RecordSink;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.StreamContext;
import ai.foresignal.source.spi.StreamHandle;
import ai.foresignal.source.spi.StreamingAdapter;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import ai.foresignal.collect.UpstreamHttpException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * {@code wiki-edit-surge} (prompt 095): a Wikipedia edit-surge detector over the Wikimedia EventStreams
 * {@code recentchange} SSE feed.
 *
 * <p>Disabled by default. One connection is held; the server closes every connection after about 15 minutes, so
 * a clean close reconnects at once (without counting a failure) with {@code Last-Event-ID} set to the last event
 * id (stored verbatim). Transport errors back off 1 s → 5 s → 30 s → 120 s with jitter; a 429/503 waits 30 s.
 * {@link SurgeDetector} decides when a page is surging, and a record is pushed only when the banded state
 * changes, so a steady surge becomes a touch. Contributor usernames, comments and parsed comments are never
 * stored; only a hashed editor name is held in memory inside the detector.
 *
 * <p>A surge's page is geotagged with one budgeted {@code action=query&prop=coordinates} request against the
 * wiki's own API, restricted to a {@code *.wikipedia.org} host and cached 24 h.
 */
@Component
public final class WikiStreamAdapter implements StreamingAdapter {

    /** The source id. */
    public static final String ID = "wiki-edit-surge";

    /** The record kind. */
    public static final String KIND = "wiki-edit-surge";

    /** The production stream endpoint. */
    public static final String DEFAULT_STREAM_URL = "https://stream.wikimedia.org/v2/stream/recentchange";

    /** Test-only override of {@link #DEFAULT_STREAM_URL}. */
    public static final String STREAM_URL_PROPERTY = "foresignal.test.adapters.wiki-edit-surge.url";

    /** The {@code wikis} parameter. */
    public static final String WIKIS_PARAM = "wikis";

    /** The {@code minEdits} parameter. */
    public static final String MIN_EDITS_PARAM = "minEdits";

    /** The {@code minEditors} parameter. */
    public static final String MIN_EDITORS_PARAM = "minEditors";

    /** The default wikis. */
    public static final List<String> DEFAULT_WIKIS = List.of(
            "enwiki", "dewiki", "frwiki", "eswiki", "ruwiki", "ukwiki", "arwiki", "zhwiki");

    /** The state key of the last SSE event id, stored verbatim. */
    static final String LAST_EVENT_ID_KEY = "wiki:lastEventId";

    /** The geotag cache time-to-live. */
    static final Duration GEO_CACHE_TTL = Duration.ofHours(24);

    /** How long a connection must have lasted to count a clean close as normal (not a hot loop). */
    static final Duration NORMAL_CLOSE = Duration.ofSeconds(30);

    /** The error ladder. */
    static final Duration[] LADDER = {
        Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(30), Duration.ofSeconds(120)
    };

    /** Raw retention: Wikipedia edit surges are {@code bulk-event} (Conventions), 90 days. */
    static final Duration RETENTION = Duration.ofDays(90);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI streamUri;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    public WikiStreamAdapter(@Value("${" + STREAM_URL_PROPERTY + ":}") String urlOverride) {
        String chosen = urlOverride == null || urlOverride.isBlank() ? DEFAULT_STREAM_URL : urlOverride.strip();
        this.streamUri = URI.create(chosen);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The stream endpoint. */
    URI streamUri() {
        return streamUri;
    }

    @Override
    public StreamHandle start(StreamContext ctx, RecordSink sink) throws AdapterException {
        ValidatedParams params = ctx.params();
        Set<String> wikis = params.has(WIKIS_PARAM) && !params.stringList(WIKIS_PARAM).isEmpty()
                ? Set.copyOf(params.stringList(WIKIS_PARAM)) : Set.copyOf(DEFAULT_WIKIS);
        SurgeDetector.Config config = new SurgeDetector.Config(
                params.integer(MIN_EDITS_PARAM, 15), params.integer(MIN_EDITORS_PARAM, 5), 10, 5,
                Duration.ofMinutes(15), Duration.ofMinutes(30), Duration.ofMinutes(60), 200_000);
        WikiSession session = new WikiSession(this, ctx, sink, wikis, new SurgeDetector(config));
        session.start();
        return session;
    }

    /**
     * The delay before the next connection attempt.
     *
     * @param attempt reconnects since the ladder last reset
     * @param failure why the stream ended, or null for a clean close
     */
    Duration delayFor(int attempt, Throwable failure) {
        if (failure instanceof UpstreamHttpException http && (http.getStatus() == 429 || http.getStatus() == 503)) {
            return Duration.ofSeconds(30);
        }
        Duration base = LADDER[Math.max(0, Math.min(attempt, LADDER.length - 1))];
        long jitter = Math.round(base.toMillis() * 0.25 * (ThreadLocalRandom.current().nextDouble() * 2 - 1));
        return Duration.ofMillis(Math.max(1, base.toMillis() + jitter));
    }

    /** True when {@code host} is {@code wikipedia.org} or a subdomain of it. */
    static boolean allowedGeotagHost(String host) {
        if (host == null) {
            return false;
        }
        String lower = host.toLowerCase(Locale.ROOT);
        return lower.equals("wikipedia.org") || lower.endsWith(".wikipedia.org");
    }

    /** The geotag query URL for a title on a wiki's own server, or null when the host is not allowed. */
    static URI geotagUri(String serverUrl, String title) {
        if (serverUrl == null || title == null || serverUrl.isBlank()) {
            return null;
        }
        URI server;
        try {
            server = URI.create(serverUrl);
        } catch (RuntimeException e) {
            return null;
        }
        if (!allowedGeotagHost(server.getHost())) {
            return null;
        }
        String base = serverUrl.endsWith("/") ? serverUrl.substring(0, serverUrl.length() - 1) : serverUrl;
        return URI.create(base + "/w/api.php?action=query&prop=coordinates&titles="
                + URLEncoder.encode(title, StandardCharsets.UTF_8).replace("+", "%20")
                + "&format=json&formatversion=2");
    }

    /** The {@code action=query} coordinates of the first page, or null. */
    static double[] coordinates(JsonNode root) {
        JsonNode pages = root == null ? null : root.get("query");
        pages = pages == null ? null : pages.get("pages");
        if (pages == null || !pages.isArray() || pages.isEmpty()) {
            return null;
        }
        JsonNode coordinates = pages.get(0).get("coordinates");
        if (coordinates == null || !coordinates.isArray() || coordinates.isEmpty()) {
            return null;
        }
        JsonNode first = coordinates.get(0);
        JsonNode lon = first.get("lon");
        JsonNode lat = first.get("lat");
        if (lon == null || lat == null || !lon.isNumber() || !lat.isNumber()) {
            return null;
        }
        return new double[] {lon.asDouble(), lat.asDouble()};
    }

    static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Wikipedia edit surges")
                .description("A Wikipedia edit-surge detector over the Wikimedia EventStreams recentchange SSE"
                        + " feed. Per (wiki, title) it watches a sliding window of edits and distinct editors"
                        + " (hashed, in memory only) and emits a wiki-edit-surge record only when the banded"
                        + " state changes. Disabled by default. Contributor usernames, comments and parsed"
                        + " comments are never stored; the page title is untrusted text.")
                .kind(KIND)
                .homepage("https://wikitech.wikimedia.org/wiki/Event_Platform/EventStreams_HTTP_Service")
                .attribution("Wikimedia Foundation EventStreams")
                .licence("Wikimedia metadata (titles CC BY-SA 4.0)")
                .termsUrl("https://wikimediafoundation.org/our-work/policy/terms-of-use/")
                .mode(Mode.STREAMING)
                .param(new ParamSpec(WIKIS_PARAM, ParamType.STRING_LIST, "Wikis watched (database names)", DEFAULT_WIKIS,
                        false, List.of()))
                .param(ParamSpec.optional(MIN_EDITS_PARAM, ParamType.INTEGER, "Edits in the 15-minute window needed"
                        + " for a surge", 15))
                .param(ParamSpec.optional(MIN_EDITORS_PARAM, ParamType.INTEGER, "Distinct editors in the 15-minute"
                        + " window needed for a surge", 5))
                .defaultBudget(Budget.of(2, 500, 3000))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("title")
                .searchFields("title")
                .embedFields("title")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{title} ({phase})", null, "#1971C2", null));
        b.fieldSpec("wiki", new FieldSpec(FieldType.STRING, null, "Wiki", "Wiki database name (enwiki, dewiki, ...)",
                null, Set.of()));
        b.fieldSpec("title", new FieldSpec(FieldType.STRING, null, "Page title", "Page title (untrusted)", null,
                Set.of()));
        b.fieldSpec("url", new FieldSpec(FieldType.STRING, null, "Page URL", "Canonical page URL", null, Set.of()));
        b.fieldSpec("edits15m", new FieldSpec(FieldType.INTEGER, "1", "Edits (15 m)",
                "Edits in the sliding window", null, Set.of()));
        b.fieldSpec("editors15m", new FieldSpec(FieldType.INTEGER, "1", "Editors (15 m)",
                "Distinct editors in the sliding window (hashed upstream-side, never stored)", null, Set.of()));
        b.fieldSpec("firstEditAt", new FieldSpec(FieldType.TIMESTAMP, null, "First edit",
                "The earliest edit still in the window", null, Set.of()));
        b.fieldSpec("isNewPage", new FieldSpec(FieldType.BOOLEAN, null, "New page",
                "True when the page was created within the surge window", null, Set.of()));
        b.fieldSpec("phase", new FieldSpec(FieldType.STRING, null, "Phase", "rising, peak or fading", null,
                Set.of()));
        return b.build();
    }

    /** One stream: the connect/read/reconnect loop and the emitter. */
    private static final class WikiSession implements StreamHandle, Runnable {

        private final WikiStreamAdapter adapter;
        private final StreamContext ctx;
        private final RecordSink sink;
        private final Set<String> wikis;
        private final SurgeDetector detector;
        private final Map<String, CachedPoint> geoCache = new ConcurrentHashMap<>();

        private volatile boolean running = true;
        private volatile boolean everConnected;
        private volatile HttpStream current;
        private volatile Consumer<Throwable> onClose = e -> {};
        private volatile Throwable lastFailure;
        private Thread thread;

        private record CachedPoint(Instant at, double[] point) {}

        WikiSession(WikiStreamAdapter adapter, StreamContext ctx, RecordSink sink, Set<String> wikis,
                SurgeDetector detector) {
            this.adapter = adapter;
            this.ctx = ctx;
            this.sink = sink;
            this.wikis = wikis;
            this.detector = detector;
        }

        void start() {
            thread = Thread.ofVirtual().name("foresignal-stream-" + ID).start(this);
        }

        @Override
        public void run() {
            int attempt = 0;
            Instant lastSweep = Instant.EPOCH;
            try {
                while (running) {
                    long began = System.nanoTime();
                    Throwable failure = null;
                    try {
                        connect();
                    } catch (CredentialsRejectedException e) {
                        lastFailure = e;
                        break;
                    } catch (Throwable t) {
                        failure = t;
                    }
                    if (!running) {
                        break;
                    }
                    long connectedMs = (System.nanoTime() - began) / 1_000_000L;
                    Instant now = ctx.clock().instant();
                    if (lastSweep == Instant.EPOCH || Duration.between(lastSweep, now).compareTo(Duration.ofMinutes(1)) >= 0) {
                        lastSweep = now;
                        emit(detector.sweep(now));
                    }
                    Duration delay;
                    if (failure == null && connectedMs >= NORMAL_CLOSE.toMillis()) {
                        delay = Duration.ofMillis(50);
                        attempt = 0;
                    } else {
                        lastFailure = failure;
                        delay = adapter.delayFor(attempt, failure);
                        attempt++;
                    }
                    sleep(delay);
                }
            } finally {
                running = false;
                try {
                    onClose.accept(lastFailure);
                } catch (RuntimeException ignored) {
                    // the supervisor's callback must not break teardown
                }
            }
        }

        /** One connection: open the SSE stream and read events until the server closes it. */
        private void connect() throws AdapterException, IOException {
            Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Accept", "text/event-stream");
            headers.put("Cache-Control", "no-cache");
            ctx.state().get(LAST_EVENT_ID_KEY).ifPresent(id -> headers.put("Last-Event-ID", id));
            HttpStream stream = ctx.http().openStream(adapter.streamUri(), headers);
            current = stream;
            if (!everConnected) {
                everConnected = true;
                ctx.connected();
            }
            String eventId = null;
            try (InputStream body = new CountingInputStream(stream.body(), ctx);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        dispatch(eventId, takeData());
                        eventId = null;
                    } else if (line.startsWith(":")) {
                        // a comment (a keep-alive)
                    } else if (line.startsWith("id:")) {
                        eventId = strip(line.substring(3));
                    } else if (line.startsWith("data:")) {
                        appendData(strip(line.substring(5)));
                    }
                }
                if (running) {
                    dispatch(eventId, takeData());
                }
            } finally {
                current = null;
                stream.close();
            }
        }

        private final StringBuilder dataBuffer = new StringBuilder();

        private synchronized void appendData(String value) {
            if (dataBuffer.length() > 0) {
                dataBuffer.append('\n');
            }
            dataBuffer.append(value);
        }

        private synchronized String takeData() {
            String value = dataBuffer.toString();
            dataBuffer.setLength(0);
            return value.isEmpty() ? null : value;
        }

        /** The value after an SSE field's colon, with one optional leading space removed. */
        private static String strip(String value) {
            return value.startsWith(" ") ? value.substring(1) : value;
        }

        private void dispatch(String eventId, String data) {
            if (eventId != null && !eventId.isBlank()) {
                ctx.state().put(LAST_EVENT_ID_KEY, eventId);
            }
            if (data == null) {
                return;
            }
            Optional<RecentChangeEvent> parsed;
            try {
                parsed = RecentChangeEvent.parse(data);
            } catch (RuntimeException e) {
                ctx.log().debug("{}: ignoring an unreadable recentchange event: {}", ID, e.getMessage());
                return;
            }
            if (parsed.isEmpty() || !parsed.get().kept(wikis)) {
                return;
            }
            RecentChangeEvent event = parsed.get();
            detector.onEdit(event.wiki(), event.title(), event.titleUrl(), event.isNew(), event.user(), event.dt())
                    .ifPresent(surge -> emit(List.of(surge)));
        }

        private void emit(List<SurgeDetector.Surge> surges) {
            for (SurgeDetector.Surge surge : surges) {
                if (surge.tombstone()) {
                    sink.accept(NormalizedRecord.tombstone(externalId(surge), KIND, surge.observedAt()));
                    continue;
                }
                NormalizedRecord.Builder b = NormalizedRecord.builder(externalId(surge), KIND)
                        .observedAt(surge.observedAt());
                if (SurgeDetector.RISING.equals(surge.phase())) {
                    double[] point = geotag(surge);
                    if (point != null) {
                        b.point(point[0], point[1]);
                    }
                }
                b.put("wiki", surge.wiki());
                b.put("title", surge.title());
                b.put("url", surge.url());
                b.put("edits15m", surge.edits());
                b.put("editors15m", surge.editors());
                b.put("firstEditAt", surge.firstEditAt() == null ? null : surge.firstEditAt().toString());
                b.put("isNewPage", surge.isNewPage());
                b.put("phase", surge.phase());
                sink.accept(b.build());
            }
        }

        private static String externalId(SurgeDetector.Surge surge) {
            return surge.wiki() + ":" + surge.title();
        }

        /** The cached geotag point of a rising surge, fetched once with one budgeted request. */
        private double[] geotag(SurgeDetector.Surge surge) {
            URI uri = geotagUri(surge.url() == null ? null : serverUrl(surge.url()), surge.title());
            if (uri == null) {
                return null;
            }
            String key = externalId(surge);
            CachedPoint cached = geoCache.get(key);
            Instant now = ctx.clock().instant();
            if (cached != null && Duration.between(cached.at(), now).compareTo(GEO_CACHE_TTL) < 0) {
                return cached.point();
            }
            try {
                UpstreamResponse response = ctx.http().get(uri, Map.of("Accept", "application/json"));
                double[] point = coordinates(JSON.readTree(response.body()));
                geoCache.put(key, new CachedPoint(now, point));
                return point;
            } catch (RuntimeException | AdapterException e) {
                ctx.log().debug("{}: geotag of {} failed: {}", ID, surge.title(), e.getMessage());
                geoCache.put(key, new CachedPoint(now, null));
                return null;
            }
        }

        /** The {@code server_url} is the title URL's origin; the geotag query runs against it. */
        private static String serverUrl(String titleUrl) {
            try {
                URI uri = URI.create(titleUrl);
                return uri.getScheme() + "://" + uri.getAuthority();
            } catch (RuntimeException e) {
                return null;
            }
        }

        private void sleep(Duration delay) {
            if (delay == null || delay.isZero() || delay.isNegative()) {
                return;
            }
            try {
                Thread.sleep(delay.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
            }
        }

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void onClose(Consumer<Throwable> callback) {
            if (callback != null) {
                onClose = callback;
            }
            if (!running) {
                callback.accept(lastFailure);
            }
        }

        @Override
        public void close() {
            running = false;
            HttpStream stream = current;
            if (stream != null) {
                stream.close();
            }
            Thread t = thread;
            if (t != null) {
                t.interrupt();
            }
        }
    }

    /** Reports the bytes read on the stream to the metering context, in coarse increments. */
    private static final class CountingInputStream extends InputStream {

        private static final int REPORT_EVERY = 64 * 1024;

        private final InputStream delegate;
        private final StreamContext ctx;
        private int sinceReport;

        CountingInputStream(InputStream delegate, StreamContext ctx) {
            this.delegate = delegate;
            this.ctx = ctx;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value >= 0) {
                report(1);
            }
            return value;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = delegate.read(b, off, len);
            if (read > 0) {
                report(read);
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            if (sinceReport > 0) {
                ctx.reportBytes(sinceReport);
                sinceReport = 0;
            }
            delegate.close();
        }

        private void report(int bytes) {
            sinceReport += bytes;
            if (sinceReport >= REPORT_EVERY) {
                ctx.reportBytes(sinceReport);
                sinceReport = 0;
            }
        }
    }
}
