package ai.foresignal.source.adapters.radio;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.FieldSpec;
import ai.foresignal.source.spi.FieldType;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * The Radio Browser station directory (prompt 075): every station with a position, as {@code radio-station}
 * records.
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>the mirrors come from {@link RadioMirrors} (discovery cached 6 h, a fallback list when it fails);
 *   <li>the crawl reads {@code /json/stations/search?has_geo_info=true&hidebroken=true&order=name&limit=1000
 *       &offset=N} until a short page or {@value #MAX_STATIONS_PARAM} stations (default {@value #DEFAULT_MAX}). On a
 *       mirror failure (an I/O error or a 5xx) the same page is retried on the next mirror within the same fetch;
 *       every attempt is a budgeted request. A 4xx, a refused budget or a backoff ends the run;
 *   <li>each row becomes a record through {@link RadioBrowserParser}; skips are counted in {@value #SKIPPED_METRIC};
 *   <li>after an untruncated crawl, a station missing from two consecutive crawls is tombstoned
 *       ({@link AbsenceTombstoner}). A crawl that stopped at {@value #MAX_STATIONS_PARAM} tombstones nothing.
 * </ol>
 * Requests carry Foresignal's own User-Agent, as the API asks for a descriptive one. Stream URLs are links for
 * clients; Foresignal never fetches a stream.
 */
@Component
public class RadioBrowserAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "radio-browser";

    /** The test-only property moving every Radio Browser URL under one base as {@code {base}/{host}{path}}. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.radio.base-url";

    /** The parameter capping the crawl. */
    public static final String MAX_STATIONS_PARAM = "maxStations";

    /** The default crawl cap. */
    public static final int DEFAULT_MAX = 40_000;

    /** Stations per page. */
    public static final int PAGE_SIZE = 1000;

    /** The counter of rows the parser skipped. */
    public static final String SKIPPED_METRIC = "foresignal.radio.skipped";

    /** The search path and fixed query. */
    static final String SEARCH = "/json/stations/search?has_geo_info=true&hidebroken=true&order=name";

    /** Raw retention: a station directory is reference data (Conventions), 90 days of history. */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The response cap of one page (about 1.6 MB per 1000 stations on 2026-10-13). */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    private final String baseUrlOverride;
    private final RadioMirrors mirrors;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public RadioBrowserAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, new Random(),
                meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a chosen shuffle and a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param random shuffles the discovered mirrors
     * @param meters supplies the meter registry, or null
     */
    public RadioBrowserAdapter(String baseUrlOverride, Random random, Supplier<MeterRegistry> meters) {
        this.baseUrlOverride = baseUrlOverride;
        this.mirrors = new RadioMirrors(URI.create(KitUrls.rebased(RadioMirrors.DISCOVERY_URL, baseUrlOverride)),
                random);
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /**
     * The URL of one page on {@code mirror}.
     *
     * @param mirror an accepted mirror host name
     * @param offset the first row
     * @param limit the page size
     */
    public URI pageUri(String mirror, int offset, int limit) {
        return URI.create(KitUrls.rebased("https://" + mirror + SEARCH + "&limit=" + limit + "&offset=" + offset,
                baseUrlOverride));
    }

    /**
     * Crawls the directory once (see the class note).
     *
     * @throws AdapterException when every mirror fails for a page, or the budget or a backoff refuses a request
     * @throws UpstreamFormatException on a non-200 answer, a truncated body or a page that is not a JSON array
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        List<String> hosts = mirrors.resolve(ctx);
        int max = Math.max(1, ctx.params() != null && ctx.params().has(MAX_STATIONS_PARAM)
                ? ctx.params().integer(MAX_STATIONS_PARAM, DEFAULT_MAX) : DEFAULT_MAX);
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        Map<String, Integer> skipped = new TreeMap<>();
        int[] mirror = {0};
        int offset = 0;
        boolean complete = false;
        while (offset < max) {
            int limit = Math.min(PAGE_SIZE, max - offset);
            RadioBrowserParser.Page page;
            try {
                page = RadioBrowserParser.parse(page(ctx, hosts, mirror, offset, limit));
            } catch (UpstreamFormatException e) {
                AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.PARSE_ERROR, List.of());
                throw e;
            }
            page.skipped().forEach((reason, n) -> skipped.merge(reason, n, Integer::sum));
            for (NormalizedRecord r : page.records()) {
                if (ids.add(r.externalId())) {
                    records.add(r);
                } else {
                    skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
                }
            }
            if (page.rows() < limit) {
                complete = true;
                break;
            }
            offset += page.rows();
        }
        SkipCounter.count(meters, SKIPPED_METRIC, "Radio Browser rows the parser skipped", ctx.sourceId(), skipped);
        ctx.log().debug("{}: {} station(s) from {} mirror(s), complete {}, skipped {}", ctx.sourceId(), records.size(),
                hosts.size(), complete, skipped);
        if (!complete) {
            AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.TRUNCATED, List.of());
            ctx.log().warn("{}: the crawl stopped at {} stations; tombstoning nothing", ctx.sourceId(), max);
            return FetchResult.of(records);
        }
        if (records.isEmpty()) {
            // An empty directory is an upstream fault, never "every station closed".
            return FetchResult.complete(records);
        }
        List<String> gone = AbsenceTombstoner.update(ctx.state(), AbsenceTombstoner.Outcome.COMPLETE,
                records.stream().map(NormalizedRecord::externalId).toList());
        List<NormalizedRecord> out = new ArrayList<>(records);
        out.addAll(AbsenceTombstoner.tombstones(gone, RadioBrowserParser.KIND, ctx.now()));
        return FetchResult.complete(out);
    }

    /**
     * One page body, failing over to the next mirror on an I/O error or a 5xx.
     *
     * @param mirror the index of the mirror to start with; left on the one that answered
     */
    private byte[] page(FetchContext ctx, List<String> hosts, int[] mirror, int offset, int limit)
            throws AdapterException {
        AdapterException last = null;
        for (int attempt = 0; attempt < hosts.size(); attempt++) {
            int index = (mirror[0] + attempt) % hosts.size();
            UpstreamResponse response;
            try {
                response = ctx.http().get(pageUri(hosts.get(index), offset, limit), Map.of("Accept",
                        "application/json"));
            } catch (AdapterException e) {
                if (!retryable(e)) {
                    throw e;
                }
                ctx.log().warn("{}: mirror {} failed ({}); trying the next one", ctx.sourceId(), hosts.get(index),
                        e.getMessage());
                last = e;
                continue;
            }
            mirror[0] = index;
            if (response.status() != 200) {
                throw new UpstreamFormatException(ID + ": " + hosts.get(index) + " answered HTTP " + response.status(),
                        "");
            }
            if (response.truncated()) {
                throw new UpstreamFormatException(ID + ": a page exceeds the " + MAX_RESPONSE_BYTES + "-byte cap", "");
            }
            return response.body();
        }
        throw last == null ? new AdapterException(ID + ": no Radio Browser mirror is available") : last;
    }

    /**
     * True when a failed request is worth retrying on another mirror: a 5xx answer or an I/O failure. A 4xx, a
     * refused budget and a backoff are not.
     *
     * @param e the failure
     */
    static boolean retryable(AdapterException e) {
        if (e.httpStatus().isPresent()) {
            return e.httpStatus().getAsInt() >= 500;
        }
        for (Throwable t = e.getCause(); t != null; t = t.getCause()) {
            if (t instanceof IOException) {
                return true;
            }
        }
        return false;
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = RadioBrowserParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Radio Browser stations")
                .description("Internet radio stations with a position from the community Radio Browser directory"
                        + " (api.radio-browser.info, mirror discovery and failover): name, stream and home page"
                        + " links, tags, country, languages, codec, bitrate and whether the last check succeeded."
                        + " Stream URLs are links for clients; Foresignal never fetches a stream. A station missing"
                        + " from two consecutive complete crawls is tombstoned.")
                .kind(kind)
                .homepage("https://www.radio-browser.info/")
                .attribution("Radio Browser (radio-browser.info) community directory")
                .licence("Free and open: \"You may use it in free and non free software. You may install it on your"
                        + " own server and mirror all its data.\" (api.radio-browser.info)")
                .termsUrl("https://api.radio-browser.info/")
                .mode(Mode.POLLING)
                .interval(Duration.ofHours(6), Duration.ofHours(6))
                .defaultBudget(Budget.of(60, 200))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofMinutes(5))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(kind, RETENTION)
                .titleField("name")
                .searchFields("name", "tags", "state")
                .tileProperties("name", "codec", "online")
                .volatileFields("online")
                .param(ParamSpec.optional(MAX_STATIONS_PARAM, ParamType.INTEGER, "The most stations one crawl reads"
                        + " (1000 per request); a crawl that reaches it tombstones nothing", DEFAULT_MAX))
                .kindDisplay(kind, new KindDisplay("{name}", "radio", "#1C7ED6", null));
        text(b, "name", "Name", "The station name (control characters replaced, whitespace collapsed)");
        text(b, "streamUrl", "Stream", "The resolved stream URL (url_resolved); a link for clients, never fetched");
        text(b, "homepage", "Home page", "The station's home page");
        text(b, "favicon", "Icon", "The station's icon URL");
        b.fieldSpec("tags", new FieldSpec(FieldType.ARRAY, null, "Tags", "The station's tags, at most 15", null,
                Set.of()));
        text(b, "countryCode", "Country", "ISO 3166-1 alpha-2 country code");
        text(b, "state", "State", "The state or region, as listed");
        b.fieldSpec("languages", new FieldSpec(FieldType.ARRAY, null, "Languages", "The broadcast languages, as listed",
                null, Set.of()));
        text(b, "codec", "Codec", "The stream codec (for example MP3, AAC)");
        b.fieldSpec("bitrate", new FieldSpec(FieldType.INTEGER, "kbit/s", "Bitrate", "The stream bitrate", null,
                Set.of()));
        b.fieldSpec("hls", new FieldSpec(FieldType.BOOLEAN, null, "HLS", "True when the stream is HLS", null,
                Set.of()));
        b.fieldSpec("online", new FieldSpec(FieldType.BOOLEAN, null, "Online", "True when Radio Browser's last check"
                + " of the stream succeeded (lastcheckok); volatile", null, Set.of()));
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
