package ai.foresignal.source.adapters.internet;

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
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code ripe-atlas-probes} (prompt 092): the RIPE Atlas probe dump ({@code meta-latest}, bzip2 JSON).
 *
 * <p>Host-identifying fields ({@code address_v4}, {@code address_v6}, {@code description}) are never stored.
 * {@code observedAt} is {@code status_since} (constant per status, so a status change moves it forward), and
 * {@code lastConnected}/{@code totalUptime} are volatile.
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
    static final Duration RETENTION = Duration.ofDays(365);
    static final int MAX_BYTES = 64 * 1024 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

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
        if (ctx.params().bool("intradayDisconnected", false)) {
            Instant lastDaily = ai.foresignal.source.adapters.humanitarian.Instants
                    .parse(ctx.state().get(LAST_DAILY_KEY).orElse(null));
            if (lastDaily != null && lastDaily.plus(Duration.ofHours(20)).isAfter(ctx.now())) {
                return FetchResult.of(intraday(ctx));
            }
        }
        UpstreamResponse response = ctx.http().getConditional(
                URI.create(baseUrl + "/ripe/atlas/probes/archive/meta-latest"), "atlas:dump");
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        if (response.status() != 200) {
            throw new UpstreamFormatException("The RIPE Atlas dump answered HTTP " + response.status(), "");
        }
        byte[] json;
        try (var in = new BZip2CompressorInputStream(new ByteArrayInputStream(response.body()))) {
            json = in.readAllBytes();
        } catch (IOException e) {
            throw new UpstreamFormatException("The RIPE Atlas dump is not readable bzip2", "", e);
        }
        List<NormalizedRecord> records = parse(json, statuses);
        Set<String> current = new LinkedHashSet<>();
        records.forEach(r -> current.add(r.externalId()));
        Set<String> previous = new LinkedHashSet<>();
        ctx.state().get(IDS_KEY).orElse("").lines().filter(s -> !s.isBlank()).forEach(previous::add);
        List<NormalizedRecord> out = new ArrayList<>(records);
        for (String id : ai.foresignal.source.adapters.sanctions.DelistingGuard.gone(previous, current)) {
            out.add(NormalizedRecord.tombstone(id, KIND, ctx.now()));
        }
        ctx.state().put(IDS_KEY,
                String.join("\n", ai.foresignal.source.adapters.sanctions.DelistingGuard.sorted(current)));
        ctx.state().put(LAST_DAILY_KEY, ctx.now().toString());
        return FetchResult.complete(out);
    }

    /** The intraday refresh of disconnected probes (at most five pages of the Probe API). */
    private List<NormalizedRecord> intraday(FetchContext ctx) throws AdapterException {
        List<NormalizedRecord> out = new ArrayList<>();
        for (int page = 1; page <= 5; page++) {
            UpstreamResponse response = ctx.http().get(URI.create(apiBaseUrl
                    + "/api/v2/probes/?status=2&page_size=500&fields=id,status,status_since,last_connected&page="
                    + page));
            if (response.status() != 200) {
                throw new UpstreamFormatException("The RIPE Atlas Probe API answered HTTP " + response.status(), "");
            }
            JsonNode results = JSON.readTree(response.body()).get("results");
            if (results == null || !results.isArray() || results.isEmpty()) {
                break;
            }
            for (JsonNode probe : results) {
                NormalizedRecord record = map(probe, java.util.Set.of(2));
                if (record != null) {
                    out.add(record);
                }
            }
        }
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
        List<NormalizedRecord> out = new ArrayList<>();
        JsonNode objects;
        try {
            objects = JSON.readTree(json).get("objects");
        } catch (RuntimeException e) {
            throw new UpstreamFormatException("The RIPE Atlas dump is not valid JSON", "", e);
        }
        if (objects == null || !objects.isArray()) {
            return out;
        }
        for (JsonNode probe : objects) {
            NormalizedRecord record = map(probe, statuses);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private static NormalizedRecord map(JsonNode probe, Set<Integer> statuses) {
        String id = text(probe, "id");
        if (id == null) {
            return null;
        }
        int status = probe.path("status").asInt(-1);
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
        if (lon != null && lat != null) {
            b.point(lon, lat);
        }
        b.put("status", status);
        b.put("statusName", statusName(status));
        b.put("statusSince", statusSince == null ? null : statusSince.toString());
        b.put("asnV4", integer(probe, "asn_v4"));
        b.put("asnV6", integer(probe, "asn_v6"));
        b.put("prefixV4", text(probe, "prefix_v4"));
        b.put("prefixV6", text(probe, "prefix_v6"));
        b.put("countryCode", text(probe, "country_code"));
        b.put("isAnchor", bool(probe, "is_anchor"));
        b.put("isPublic", bool(probe, "is_public"));
        b.put("tags", tags(probe.get("tags")));
        return b.build();
    }

    private static List<String> tags(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode tag : node) {
                if (tag.isString()) {
                    out.add(tag.asString());
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
        // status_since is a unix epoch second in the dump.
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

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("RIPE Atlas probe status")
                .description("The RIPE Atlas probe dump (meta-latest, bzip2 JSON): the status of the network's "
                        + "probes. Host-identifying fields (address_v4, address_v6, description) are never stored.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://atlas.ripe.net")
                .attribution("RIPE NCC / RIPE Atlas")
                .licence("RIPE Atlas Service Terms v3.5 (attribution; commercial use by permission)")
                .termsUrl("https://atlas.ripe.net/legal/terms/")
                .param(ParamSpec.optional("statuses", ParamType.STRING, "Comma-separated status codes to keep", "1,2"))
                .param(ParamSpec.optional("intradayDisconnected", ParamType.BOOLEAN,
                        "Also refresh disconnected probes intraday", false))
                .interval(Duration.ofHours(24), Duration.ofHours(24))
                .defaultBudget(1, 10)
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
