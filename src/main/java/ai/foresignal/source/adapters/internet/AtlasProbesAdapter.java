package ai.foresignal.source.adapters.internet;

import ai.foresignal.source.adapters.sanctions.DelistingGuard;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
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
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ripe-atlas-probes} (prompt 092): the RIPE Atlas probe dump ({@code meta-latest}, bzip2 JSON), plus an
 * optional intraday refresh of disconnected probes from the Probe API.
 *
 * <p><strong>Schedule.</strong> The dump is fetched (conditional GET) when the last dump check is at least
 * {@link #DUMP_MIN_AGE} old, which is every run at the default 24 h interval. With {@code intradayDisconnected} on
 * and the source's interval set to 30 minutes, the runs in between page the Probe API's disconnected list instead.
 * A run does one or the other, never both, so a run sends at most {@link #MAX_INTRADAY_PAGES} requests and the
 * default budget (six a minute, 260 a day: 48 intraday runs of five pages plus four dump checks) covers it.
 *
 * <p><strong>Intraday records are whole records.</strong> They ask the Probe API for every stored field (location,
 * ASNs, prefixes, country, flags, tags), never the host-identifying ones, and map them exactly as the dump is
 * mapped, so an intraday version replaces a dump version without losing geometry or attributes.
 *
 * <p>Host-identifying fields ({@code address_v4}, {@code address_v6}, {@code description}) are never stored.
 * {@code observedAt} is {@code status_since} (constant per status, so a status change moves it forward), and
 * {@code lastConnected}/{@code totalUptime} are volatile. The dump is decompressed as a stream, capped at
 * {@link #MAX_JSON_BYTES}.
 */
@Component
public final class AtlasProbesAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ripe-atlas-probes";

    /** The record kind. */
    public static final String KIND = "probe-status";

    /** The default base URL (the dump host). */
    public static final String DEFAULT_BASE_URL = "https://ftp.ripe.net";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.ripe-atlas-probes.base-url";

    /** Test-only base override for the intraday Probe API (a different host from the dump). */
    public static final String API_BASE_URL_PROPERTY = "foresignal.test.adapters.ripe-atlas-probes.api-base-url";

    static final String IDS_KEY = "atlas:ids";
    static final String LAST_DAILY_KEY = "atlas:lastDaily";
    static final String DUMP_KEY = "atlas:dump";
    static final Duration RETENTION = Duration.ofDays(365);
    static final int MAX_BYTES = 64 * 1024 * 1024;

    /** The decompressed-size cap of the dump (about 25 MB of JSON in 2026-10). */
    static final long MAX_JSON_BYTES = 256L * 1024 * 1024;

    /** The dump's minimum interval (prompt 092: "min 6 h"); a run sooner than this does not re-check it. */
    static final Duration DUMP_MIN_AGE = Duration.ofHours(6);

    /** The intraday page size and page cap (about 2,300 disconnected probes in 2026-10). */
    static final int INTRADAY_PAGE_SIZE = 500;
    static final int MAX_INTRADAY_PAGES = 5;

    /** The Probe API fields an intraday record needs: everything stored, nothing host-identifying. */
    static final String INTRADAY_FIELDS = "id,status,status_since,last_connected,total_uptime,geometry,asn_v4,asn_v6,"
            + "prefix_v4,prefix_v6,country_code,is_anchor,is_public,tags";

    private static final JsonMapper JSON = JsonMapper.builder().build();

    /**
     * Reads the dump one probe tree at a time. Jackson 3 enables {@code FAIL_ON_TRAILING_TOKENS} by default, which
     * refuses a tree read that leaves the next probe behind it; streaming needs it off (as in
     * {@code source.support.ReferenceStreams}).
     */
    private static final JsonMapper STREAM = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    private final URI baseUrl;
    private final URI apiBaseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter (tests). */
    public AtlasProbesAdapter(String baseUrlOverride) {
        this(baseUrlOverride, null);
    }

    /** Creates the adapter. */
    @Autowired
    public AtlasProbesAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            @Value("${" + API_BASE_URL_PROPERTY + ":}") String apiBaseOverride) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.apiBaseUrl = URI.create(apiBaseOverride == null || apiBaseOverride.isBlank()
                ? "https://atlas.ripe.net" : apiBaseOverride.strip());
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** {@code 0} Never Connected, {@code 1} Connected, {@code 2} Disconnected, {@code 3} Abandoned, {@code 4} Written Off. */
    static String statusName(int status) {
        return switch (status) {
            case 0 -> "never-connected";
            case 1 -> "connected";
            case 2 -> "disconnected";
            case 3 -> "abandoned";
            case 4 -> "written-off";
            default -> "unknown";
        };
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Set<Integer> statuses = statuses(ctx.params().string("statuses"));
        Instant lastDaily = ai.foresignal.source.adapters.humanitarian.Instants
                .parse(ctx.state().get(LAST_DAILY_KEY).orElse(null));
        boolean dumpDue = lastDaily == null || !lastDaily.plus(DUMP_MIN_AGE).isAfter(ctx.now());
        if (!dumpDue) {
            if (ctx.params().bool("intradayDisconnected", false) && statuses.contains(2)) {
                return intraday(ctx);
            }
            return FetchResult.unchanged();
        }
        UpstreamResponse response = ctx.http().getConditional(
                URI.create(baseUrl + "/ripe/atlas/probes/archive/meta-latest"), DUMP_KEY);
        if (response.notModified()) {
            ctx.state().put(LAST_DAILY_KEY, ctx.now().toString());
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The RIPE Atlas dump answered HTTP " + response.status(), "");
        }
        List<NormalizedRecord> records;
        try (InputStream in = new CappedInputStream(
                new BZip2CompressorInputStream(new ByteArrayInputStream(response.body())), MAX_JSON_BYTES)) {
            records = parse(in, statuses);
        } catch (IOException | RuntimeException e) {
            SourceHttp.forgetValidators(ctx.state(), DUMP_KEY);
            throw new UpstreamFormatException("The RIPE Atlas dump is not readable bzip2 JSON: " + e.getMessage(), "",
                    e);
        }
        Set<String> current = new LinkedHashSet<>();
        records.forEach(r -> current.add(r.externalId()));
        Set<String> previous = ids(ctx);
        List<NormalizedRecord> out = new ArrayList<>(records);
        for (String id : DelistingGuard.gone(previous, current)) {
            out.add(NormalizedRecord.tombstone(id, KIND, ctx.now()));
        }
        ctx.state().put(IDS_KEY, String.join("\n", DelistingGuard.sorted(current)));
        ctx.state().put(LAST_DAILY_KEY, ctx.now().toString());
        return FetchResult.complete(out);
    }

    /**
     * The intraday refresh of disconnected probes: up to {@link #MAX_INTRADAY_PAGES} pages of the Probe API,
     * stopping at the last page. Each record is a whole probe record (see the class comment).
     */
    private FetchResult intraday(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> out = new ArrayList<>();
        for (int page = 1; page <= MAX_INTRADAY_PAGES; page++) {
            UpstreamResponse response = ctx.http().get(URI.create(apiBaseUrl + "/api/v2/probes/?status=2&page_size="
                    + INTRADAY_PAGE_SIZE + "&fields=" + INTRADAY_FIELDS.replace(",", "%2C") + "&page=" + page));
            if (response.status() != 200) {
                throw new UpstreamFormatException("The RIPE Atlas Probe API answered HTTP " + response.status(), "");
            }
            JsonNode root;
            try {
                root = JSON.readTree(response.body());
            } catch (RuntimeException e) {
                throw new UpstreamFormatException("The RIPE Atlas Probe API answered unreadable JSON", "", e);
            }
            List<NormalizedRecord> records = parseApiPage(root);
            out.addAll(records);
            JsonNode next = root.get("next");
            int results = root.path("results").size();
            if (next == null || next.isNull() || results < INTRADAY_PAGE_SIZE) {
                break;
            }
        }
        // A probe first seen intraday joins the emitted set, so a later dump can tombstone it if it leaves the filter.
        Set<String> ids = ids(ctx);
        boolean grew = false;
        for (NormalizedRecord record : out) {
            grew |= ids.add(record.externalId());
        }
        if (grew) {
            ctx.state().put(IDS_KEY, String.join("\n", DelistingGuard.sorted(ids)));
        }
        return FetchResult.of(out);
    }

    /** The records of one Probe API page ({@code {"results":[…]}}), disconnected probes only. */
    public List<NormalizedRecord> parseApiPage(JsonNode root) {
        List<NormalizedRecord> out = new ArrayList<>();
        JsonNode results = root == null ? null : root.get("results");
        if (results == null || !results.isArray()) {
            return out;
        }
        for (JsonNode probe : results) {
            NormalizedRecord record = map(probe, Set.of(2));
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private static Set<String> ids(FetchContext ctx) {
        Set<String> out = new LinkedHashSet<>();
        ctx.state().get(IDS_KEY).orElse("").lines().filter(s -> !s.isBlank()).forEach(out::add);
        return out;
    }

    private static Set<Integer> statuses(String value) {
        Set<Integer> out = new LinkedHashSet<>();
        for (String part : (value == null || value.isBlank() ? "1,2" : value).split(",")) {
            try {
                out.add(Integer.parseInt(part.strip()));
            } catch (NumberFormatException ignored) {
                // ignore a malformed entry
            }
        }
        return out;
    }

    /** The records of a decompressed Atlas dump body. */
    public List<NormalizedRecord> parse(byte[] json, Set<Integer> statuses) {
        try {
            return parse(new ByteArrayInputStream(json), statuses);
        } catch (IOException e) {
            throw new UpstreamFormatException("The RIPE Atlas dump is not valid JSON", "", e);
        }
    }

    /**
     * The records of a decompressed Atlas dump stream, read object by object so the whole document is never held as
     * one tree.
     */
    public List<NormalizedRecord> parse(InputStream json, Set<Integer> statuses) throws IOException {
        List<NormalizedRecord> out = new ArrayList<>();
        try (JsonParser parser = STREAM.createParser(json)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new UpstreamFormatException("The RIPE Atlas dump is not a JSON object", "");
            }
            while (parser.nextToken() == JsonToken.PROPERTY_NAME) {
                String name = parser.currentName();
                JsonToken value = parser.nextToken();
                if (!"objects".equals(name) || value != JsonToken.START_ARRAY) {
                    parser.skipChildren();
                    continue;
                }
                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    JsonNode probe = STREAM.readTree(parser);
                    NormalizedRecord record = map(probe, statuses);
                    if (record != null) {
                        out.add(record);
                    }
                }
            }
        } catch (JacksonException e) {
            if (e.getCause() instanceof IOException io) {
                throw io;
            }
            throw new UpstreamFormatException("The RIPE Atlas dump is not valid JSON", "", e);
        }
        return out;
    }

    private static NormalizedRecord map(JsonNode probe, Set<Integer> statuses) {
        String id = text(probe, "id");
        if (id == null) {
            return null;
        }
        // The dump carries the status as a number; the Probe API as {"id":2,"name":"Disconnected","since":"…"}.
        JsonNode statusNode = probe.path("status");
        int status = statusNode.isObject() ? statusNode.path("id").asInt(-1) : statusNode.asInt(-1);
        if (!statuses.contains(status)) {
            return null;
        }
        Instant statusSince = time(probe, "status_since");
        if (statusSince == null) {
            // status_since is the only time base; a probe without it is skipped (Core-Data-Model §2).
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("probe:" + id, KIND);
        b.observedAt(statusSince);
        Double lon = number(probe, "longitude");
        Double lat = number(probe, "latitude");
        JsonNode coordinates = probe.path("geometry").path("coordinates");
        if ((lon == null || lat == null) && coordinates.isArray() && coordinates.size() >= 2
                && coordinates.get(0).isNumber() && coordinates.get(1).isNumber()) {
            lon = coordinates.get(0).asDouble();
            lat = coordinates.get(1).asDouble();
        }
        if (lon != null && lat != null) {
            b.point(lon, lat);
        }
        b.put("status", status);
        b.put("statusName", statusName(status));
        b.put("statusSince", statusSince.toString());
        b.put("asnV4", integer(probe, "asn_v4"));
        b.put("asnV6", integer(probe, "asn_v6"));
        b.put("prefixV4", text(probe, "prefix_v4"));
        b.put("prefixV6", text(probe, "prefix_v6"));
        b.put("countryCode", text(probe, "country_code"));
        b.put("isAnchor", bool(probe, "is_anchor"));
        b.put("isPublic", bool(probe, "is_public"));
        b.put("tags", tags(probe.get("tags")));
        Instant lastConnected = time(probe, "last_connected");
        if (lastConnected != null) {
            b.put("lastConnected", lastConnected.toString());
        }
        Long totalUptime = longValue(probe, "total_uptime");
        if (totalUptime != null) {
            b.put("totalUptime", totalUptime);
        }
        return b.build();
    }

    /** Tag slugs: the dump lists slugs, the Probe API {@code {name, slug}} objects, so both map to the slug. */
    private static List<String> tags(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode tag : node) {
                if (tag.isString()) {
                    out.add(tag.asString());
                } else if (tag.get("slug") != null && !tag.get("slug").isNull()) {
                    out.add(tag.get("slug").asString());
                } else if (tag.get("name") != null) {
                    out.add(tag.get("name").asString());
                }
            }
        }
        return out;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Integer integer(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asInt();
    }

    private static Long longValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asLong();
    }

    private static Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private static Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isNumber() ? value.asDouble() : null;
    }

    private static Instant time(JsonNode node, String field) {
        // status_since and last_connected are unix epoch seconds in both the dump and the Probe API.
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        if (value.isNumber()) {
            return Instant.ofEpochSecond(value.asLong());
        }
        String text = value.asString(null);
        return text == null || text.isBlank() ? null : ai.foresignal.source.adapters.humanitarian.Instants.parse(text);
    }

    /** An input stream that fails once more than {@code cap} bytes have been read through it. */
    static final class CappedInputStream extends FilterInputStream {

        private final long cap;
        private long count;

        /** Wraps {@code in}, failing past {@code cap} bytes. */
        CappedInputStream(InputStream in, long cap) {
            super(in);
            this.cap = cap;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                charge(1);
            }
            return b;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int n = super.read(buffer, offset, length);
            if (n > 0) {
                charge(n);
            }
            return n;
        }

        private void charge(long n) throws IOException {
            count += n;
            if (count > cap) {
                throw new IOException("decompressed body exceeds " + cap + " bytes");
            }
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("RIPE Atlas probe status")
                .description("The RIPE Atlas probe dump (meta-latest, bzip2 JSON): the status of the network's "
                        + "probes, checked at most every six hours. With intradayDisconnected on and a 30-minute "
                        + "interval, the runs in between refresh the disconnected probes from the Probe API. "
                        + "Host-identifying fields (address_v4, address_v6, description) are never stored.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://atlas.ripe.net")
                .attribution("RIPE NCC / RIPE Atlas")
                .licence("RIPE Atlas Service Terms v3.5 (attribution; commercial use by permission)")
                .termsUrl("https://atlas.ripe.net/legal/terms/")
                .param(ParamSpec.optional("statuses", ParamType.STRING, "Comma-separated status codes to keep", "1,2"))
                .param(ParamSpec.optional("intradayDisconnected", ParamType.BOOLEAN,
                        "Refresh disconnected probes from the Probe API between dump checks (set the interval to "
                                + "30 minutes)", false))
                .interval(Duration.ofMinutes(30), Duration.ofHours(24))
                .defaultBudget(MAX_INTRADAY_PAGES + 1, 260)
                .maxResponseBytes(MAX_BYTES)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .volatileFields("lastConnected", "totalUptime")
                .zeroResultsNormal(false)
                .kindDisplay(KIND, new KindDisplay("Probe {statusName}", "antenna", "#1971C2", null))
                .build();
    }
}
