package ai.foresignal.source.adapters.internet;

import ai.foresignal.reference.CountryCentroids;
import ai.foresignal.reference.CountryRef;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.CredentialField;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.Geo;
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
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code cloudflare-radar-bgp} (prompt 092): Cloudflare Radar BGP hijack and leak events.
 *
 * <p>Each run pages the hijack and leak endpoints (at most five pages each). Hijacks and leaks are separate
 * kinds; {@code observedAt} is the newest message time, {@code eventTime} the earliest. Needs a Radar token.
 */
@Component
public final class CloudflareBgpAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "cloudflare-radar-bgp";

    /** The hijack kind. */
    public static final String KIND_HIJACK = "bgp-hijack";

    /** The leak kind. */
    public static final String KIND_LEAK = "bgp-leak";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.cloudflare.com";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cloudflare-radar-bgp.base-url";

    /** The credential name. */
    public static final String API_TOKEN = "apiToken";

    static final Duration RETENTION = Duration.ofDays(1825);
    static final int MAX_PAGES = 5;

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final URI baseUrl;
    private final CountryCentroids countries;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public CloudflareBgpAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            CountryCentroids countries) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.countries = countries;
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String token = ctx.credentials().get(API_TOKEN);
        if (token == null || token.isBlank()) {
            throw new ai.foresignal.source.spi.CredentialsRejectedException(
                    "cloudflare-radar-bgp needs a Radar API token");
        }
        int minConfidence = ctx.params().integer("minConfidence", 8);
        List<NormalizedRecord> records = new ArrayList<>();
        records.addAll(paged(ctx, "/client/v4/radar/bgp/hijacks/events?dateRange=1d&per_page=100&sortBy=TIME"
                + "&sortOrder=DESC&minConfidence=" + minConfidence, token, true));
        records.addAll(paged(ctx, "/client/v4/radar/bgp/leaks/events?dateRange=1d&per_page=100", token, false));
        return FetchResult.of(records);
    }

    private List<NormalizedRecord> paged(FetchContext ctx, String path, String token, boolean hijack)
            throws AdapterException {
        List<NormalizedRecord> out = new ArrayList<>();
        int total = Integer.MAX_VALUE;
        int page = 1;
        while (page <= MAX_PAGES && out.size() < total) {
            UpstreamResponse response = ctx.http().get(URI.create(baseUrl + path + "&page=" + page),
                    Map.of("Authorization", "Bearer " + token));
            if (response.status() != 200) {
                throw new UpstreamFormatException("Cloudflare Radar BGP answered HTTP " + response.status(), "");
            }
            JsonNode root = JSON.readTree(response.body());
            total = root.path("result_info").path("total_count").asInt(total);
            JsonNode events = root.path("result").path("events");
            if (!events.isArray() || events.isEmpty()) {
                break;
            }
            for (JsonNode event : events) {
                NormalizedRecord record = hijack ? mapHijack(event) : mapLeak(event);
                if (record != null) {
                    out.add(record);
                }
            }
            page++;
        }
        return out;
    }

    private NormalizedRecord mapHijack(JsonNode event) {
        String id = text(event, "id");
        if (id == null) {
            return null;
        }
        Instant observed = time(event, "max_msg_ts");
        NormalizedRecord.Builder b = NormalizedRecord.builder("hijack:" + id, KIND_HIJACK)
                .eventTime(time(event, "min_hijack_ts"))
                .observedAt(observed != null ? observed : time(event, "max_hijack_ts"));
        b.put("confidence_score", number(event, "confidence_score"));
        b.put("event_type", text(event, "event_type"));
        b.put("hijacker_asn", integer(event, "hijacker_asn"));
        b.put("hijacker_country", text(event, "hijacker_country"));
        b.put("victim_asns", integers(event.get("victim_asns")));
        b.put("victim_countries", strings(event.get("victim_countries")));
        b.put("prefixes", cap(strings(event.get("prefixes")), 50));
        b.put("is_stale", bool(event, "is_stale"));
        b.put("duration", text(event, "duration"));
        b.put("tags", tags(event.get("tags")));
        geometry(b, countries.byIso2(text(event, "hijacker_country")));
        return b.build();
    }

    private NormalizedRecord mapLeak(JsonNode event) {
        String id = text(event, "id");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("leak:" + id, KIND_LEAK)
                .eventTime(time(event, "min_ts"))
                .observedAt(time(event, "max_ts"));
        b.put("leak_asn", integer(event, "leak_asn"));
        b.put("leak_type", integer(event, "leak_type"));
        b.put("leak_seg", integer(event, "leak_seg"));
        b.put("countries", strings(event.get("countries")));
        b.put("finished", bool(event, "finished"));
        b.put("origin_count", integer(event, "origin_count"));
        b.put("peer_count", integer(event, "peer_count"));
        b.put("prefix_count", integer(event, "prefix_count"));
        List<String> codes = strings(event.get("countries"));
        if (!codes.isEmpty()) {
            geometry(b, countries.byIso2(codes.get(0)));
        }
        return b.build();
    }

    private void geometry(NormalizedRecord.Builder b, Optional<CountryRef> ref) {
        ref.ifPresent(country -> {
            b.geometry(Geo.point(country.labelLon(), country.labelLat()));
            b.put("geometryApproximate", true);
        });
    }

    private static List<Map<String, Object>> tags(JsonNode node) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode tag : node) {
                if (tag.isObject()) {
                    out.add(Map.of("name", text(tag, "name"), "score", number(tag, "score")));
                }
            }
        }
        return out;
    }

    private static List<String> cap(List<String> values, int max) {
        return values.size() <= max ? values : values.subList(0, max);
    }

    private static List<String> strings(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.isString()) {
                    out.add(item.asString());
                }
            }
        }
        return out;
    }

    private static List<Integer> integers(JsonNode node) {
        List<Integer> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode item : node) {
                if (item.isNumber()) {
                    out.add(item.asInt());
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
        return value == null || value.isNull() || !value.isNumber() ? null : value.asInt();
    }

    private static Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() || !value.isNumber() ? null : value.asDouble();
    }

    private static Boolean bool(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asBoolean();
    }

    private static Instant time(JsonNode node, String field) {
        String value = text(node, field);
        if (value == null) {
            return null;
        }
        try {
            return Instant.ofEpochSecond(Long.parseLong(value.strip()));
        } catch (NumberFormatException e) {
            return ai.foresignal.source.adapters.humanitarian.Instants.parse(value);
        }
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Cloudflare Radar BGP events")
                .description("BGP hijack and leak events from Cloudflare Radar. Needs a Cloudflare API token with "
                        + "Radar read access. Hijacks and leaks are separate kinds; country geometry is approximate.")
                .kinds(KIND_HIJACK, KIND_LEAK)
                .mode(Mode.POLLING)
                .homepage("https://radar.cloudflare.com/routing")
                .attribution("Cloudflare Radar")
                .licence("CC BY-NC 4.0")
                .termsUrl("https://www.cloudflare.com/website-terms/")
                .credential(CredentialField.required(API_TOKEN, "Cloudflare API token with Radar read access"))
                .param(ParamSpec.optional("minConfidence", ParamType.INTEGER,
                        "Minimum hijack confidence score", 8))
                .param(ParamSpec.optional("involvedAsn", ParamType.STRING_LIST, "Filter by involved ASN", List.of()))
                .param(ParamSpec.optional("involvedCountry", ParamType.STRING_LIST, "Filter by country", List.of()))
                .interval(Duration.ofMinutes(15), Duration.ofMinutes(15))
                .defaultBudget(6, 600)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .volatileFields("hijack_msgs_count", "on_going_count", "peer_ip_count", "max_msg_ts", "leak_count")
                .searchFields("hijacker_country", "victim_countries")
                .zeroResultsNormal(true)
                .kindDisplay(KIND_HIJACK, new KindDisplay("Hijack by AS{hijacker_asn}", "danger", "#C92A2A", null))
                .kindDisplay(KIND_LEAK, new KindDisplay("Route leak from AS{leak_asn}", "caution", "#E8590C", null))
                .build();
    }
}
