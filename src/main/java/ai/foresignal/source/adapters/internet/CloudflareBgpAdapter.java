package ai.foresignal.source.adapters.internet;

import ai.foresignal.reference.CountryCentroids;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code cloudflare-radar-bgp} (prompt 092): Cloudflare Radar BGP hijack and leak events.
 *
 * <p>Each run pages the hijack and leak endpoints (at most {@value #MAX_PAGES} pages each, stopping at
 * {@code result_info.total_count}), so a run sends at most ten requests; the default budget (ten a minute) covers a
 * full run. Hijacks and leaks are separate kinds; {@code observedAt} is the newest message time, {@code eventTime}
 * the earliest. AS names come from the response's {@code asn_info}. Geometry is the country centroids of the
 * hijacker and the victims (a leak's {@code countries}), approximate.
 *
 * <p>Radar's {@code involvedAsn} and {@code involvedCountry} filters take one value each: a single configured value is
 * sent upstream, several are applied locally to the unfiltered pages. Needs a Radar token.
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
    static final int MAX_PREFIXES = 50;

    private static final Logger LOG = LoggerFactory.getLogger(CloudflareBgpAdapter.class);
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

    /** The records of one hijacks or leaks body ({@code hijack=true} for a hijacks body). */
    public List<NormalizedRecord> parse(byte[] body, boolean hijack) {
        return parse(JSON.readTree(body), hijack);
    }

    private List<NormalizedRecord> parse(JsonNode root, boolean hijack) {
        List<NormalizedRecord> out = new ArrayList<>();
        JsonNode result = root.path("result");
        JsonNode events = result.path("events");
        if (!events.isArray()) {
            return out;
        }
        Map<Integer, String> asNames = asNames(result.get("asn_info"));
        // observedAt is max_msg_ts; a response whose events lack it entirely falls back to max_hijack_ts, decided
        // once per response so one record set never mixes the two.
        boolean useMaxMsg = !hijack || anyHas(events, "max_msg_ts");
        if (hijack && !useMaxMsg && !events.isEmpty()) {
            LOG.info("{}: this hijacks response carries no max_msg_ts; observedAt falls back to max_hijack_ts", ID);
        }
        for (JsonNode event : events) {
            NormalizedRecord record = hijack ? mapHijack(event, asNames, useMaxMsg) : mapLeak(event, asNames);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        String token = ctx.credentials().get(API_TOKEN);
        if (token == null || token.isBlank()) {
            throw new ai.foresignal.source.spi.CredentialsRejectedException(
                    "cloudflare-radar-bgp needs a Radar API token");
        }
        int minConfidence = ctx.params().integer("minConfidence", 8);
        List<Integer> involvedAsns = asns(ctx.params().stringList("involvedAsn"));
        List<String> involvedCountries = codes(ctx.params().stringList("involvedCountry"));
        StringBuilder filters = new StringBuilder();
        if (involvedAsns.size() == 1) {
            filters.append("&involvedAsn=").append(involvedAsns.get(0));
        }
        if (involvedCountries.size() == 1) {
            filters.append("&involvedCountry=").append(encode(involvedCountries.get(0)));
        }
        List<NormalizedRecord> records = new ArrayList<>();
        records.addAll(paged(ctx, "/client/v4/radar/bgp/hijacks/events?dateRange=1d&per_page=100&sortBy=TIME"
                + "&sortOrder=DESC&minConfidence=" + minConfidence + filters, token, true));
        records.addAll(paged(ctx, "/client/v4/radar/bgp/leaks/events?dateRange=1d&per_page=100" + filters, token,
                false));
        if (involvedAsns.size() > 1 || involvedCountries.size() > 1) {
            records = records.stream().filter(r -> involves(r, involvedAsns, involvedCountries)).toList();
        }
        return FetchResult.of(records);
    }

    private List<NormalizedRecord> paged(FetchContext ctx, String path, String token, boolean hijack)
            throws AdapterException {
        List<NormalizedRecord> out = new ArrayList<>();
        int total = Integer.MAX_VALUE;
        int seen = 0;
        int page = 1;
        while (page <= MAX_PAGES && seen < total) {
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
            seen += events.size();
            out.addAll(parse(root, hijack));
            page++;
        }
        return out;
    }

    /** True when {@code record} involves one of the configured ASNs or countries (each list empty = no filter). */
    static boolean involves(NormalizedRecord record, List<Integer> asns, List<String> countryCodes) {
        Map<String, Object> p = record.payload();
        Set<Object> recordAsns = new LinkedHashSet<>();
        Set<Object> recordCountries = new LinkedHashSet<>();
        if (KIND_HIJACK.equals(record.kind())) {
            recordAsns.add(p.get("hijacker_asn"));
            addAll(recordAsns, p.get("victim_asns"));
            recordCountries.add(p.get("hijacker_country"));
            addAll(recordCountries, p.get("victim_countries"));
        } else {
            recordAsns.add(p.get("leak_asn"));
            addAll(recordCountries, p.get("countries"));
        }
        boolean asnOk = asns.isEmpty() || asns.stream().anyMatch(recordAsns::contains);
        boolean countryOk = countryCodes.isEmpty() || countryCodes.stream().anyMatch(recordCountries::contains);
        return asnOk && countryOk;
    }

    private static void addAll(Set<Object> into, Object values) {
        if (values instanceof List<?> list) {
            into.addAll(list);
        }
    }

    private NormalizedRecord mapHijack(JsonNode event, Map<Integer, String> asNames, boolean useMaxMsg) {
        String id = text(event, "id");
        // A hijack event carries hijacker_asn; a leak event does not, so skip anything else.
        if (id == null || event.get("hijacker_asn") == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("hijack:" + id, KIND_HIJACK)
                .eventTime(time(event, "min_hijack_ts"))
                .observedAt(time(event, useMaxMsg ? "max_msg_ts" : "max_hijack_ts"));
        b.put("confidence_score", number(event, "confidence_score"));
        b.put("event_type", scalar(event, "event_type"));
        Integer hijacker = integer(event, "hijacker_asn");
        b.put("hijacker_asn", hijacker);
        b.put("hijacker_asn_name", hijacker == null ? null : asNames.get(hijacker));
        String hijackerCountry = text(event, "hijacker_country");
        b.put("hijacker_country", hijackerCountry);
        List<Integer> victims = integers(event.get("victim_asns"));
        b.put("victim_asns", victims);
        b.put("victim_asn_names", names(victims, asNames));
        List<String> victimCountries = strings(event.get("victim_countries"));
        b.put("victim_countries", victimCountries);
        b.put("prefixes", cap(strings(event.get("prefixes")), MAX_PREFIXES));
        b.put("is_stale", bool(event, "is_stale"));
        b.put("duration", scalar(event, "duration"));
        b.put("tags", tags(event.get("tags")));
        b.put("hijack_msgs_count", integer(event, "hijack_msgs_count"));
        b.put("on_going_count", integer(event, "on_going_count"));
        b.put("peer_ip_count", integer(event, "peer_ip_count"));
        b.put("max_msg_ts", text(event, "max_msg_ts"));
        List<String> codes = new ArrayList<>();
        if (hijackerCountry != null) {
            codes.add(hijackerCountry);
        }
        codes.addAll(victimCountries);
        geometry(b, codes);
        return b.build();
    }

    private NormalizedRecord mapLeak(JsonNode event, Map<Integer, String> asNames) {
        String id = text(event, "id");
        // A leak event carries leak_asn; a hijack event does not, so skip anything else.
        if (id == null || event.get("leak_asn") == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("leak:" + id, KIND_LEAK)
                .eventTime(time(event, "min_ts"))
                .observedAt(time(event, "max_ts"));
        Integer leakAsn = integer(event, "leak_asn");
        b.put("leak_asn", leakAsn);
        b.put("leak_asn_name", leakAsn == null ? null : asNames.get(leakAsn));
        b.put("leak_type", integer(event, "leak_type"));
        // leak_seg is an AS-path segment (an array of ASNs) in the documented shape.
        JsonNode seg = event.get("leak_seg");
        b.put("leak_seg", seg != null && seg.isArray() ? integers(seg) : integer(event, "leak_seg"));
        List<String> codes = strings(event.get("countries"));
        b.put("countries", codes);
        b.put("finished", bool(event, "finished"));
        b.put("origin_count", integer(event, "origin_count"));
        b.put("peer_count", integer(event, "peer_count"));
        b.put("prefix_count", integer(event, "prefix_count"));
        b.put("leak_count", integer(event, "leak_count"));
        geometry(b, codes);
        return b.build();
    }

    /** The centroids of {@code codes} (a Point for one country, a MultiPoint for several), approximate. */
    private void geometry(NormalizedRecord.Builder b, List<String> codes) {
        List<Point> points = new ArrayList<>();
        Set<String> done = new LinkedHashSet<>();
        for (String code : codes) {
            if (code == null || !done.add(code.toUpperCase(Locale.ROOT))) {
                continue;
            }
            countries.byIso2(code).ifPresent(ref -> points.add(Geo.point(ref.labelLon(), ref.labelLat())));
        }
        if (points.isEmpty()) {
            return;
        }
        b.geometry(points.size() == 1 ? points.get(0) : FACTORY.createMultiPoint(points.toArray(new Point[0])));
        b.put("geometryApproximate", true);
    }

    private static Map<Integer, String> asNames(JsonNode info) {
        Map<Integer, String> out = new HashMap<>();
        if (info != null && info.isArray()) {
            for (JsonNode entry : info) {
                JsonNode asn = entry.get("asn");
                String name = text(entry, "org_name");
                if (asn != null && asn.isNumber() && name != null) {
                    out.put(asn.asInt(), name);
                }
            }
        }
        return out;
    }

    private static List<String> names(List<Integer> asns, Map<Integer, String> asNames) {
        List<String> out = new ArrayList<>();
        for (Integer asn : asns) {
            String name = asNames.get(asn);
            if (name != null) {
                out.add(name);
            }
        }
        return out;
    }

    private static boolean anyHas(JsonNode events, String field) {
        for (JsonNode event : events) {
            JsonNode value = event.get(field);
            if (value != null && !value.isNull()) {
                return true;
            }
        }
        return false;
    }

    private static List<Integer> asns(List<String> values) {
        List<Integer> out = new ArrayList<>();
        for (String value : values) {
            try {
                out.add(Integer.parseInt(value.strip().toUpperCase(Locale.ROOT).replaceFirst("^AS", "")));
            } catch (NumberFormatException | NullPointerException ignored) {
                // a malformed ASN is ignored
            }
        }
        return out;
    }

    private static List<String> codes(List<String> values) {
        List<String> out = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                out.add(value.strip().toUpperCase(Locale.ROOT));
            }
        }
        return out;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static List<Map<String, Object>> tags(JsonNode node) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode tag : node) {
                if (tag.isObject()) {
                    Map<String, Object> entry = new LinkedHashMap<>();
                    entry.put("name", text(tag, "name"));
                    entry.put("score", number(tag, "score"));
                    out.add(entry);
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

    /** A number as a number and anything else as text (Radar documents {@code duration}/{@code event_type} as numbers). */
    private static Object scalar(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isNumber() ? value.numberValue() : value.asString();
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
                .param(ParamSpec.optional("involvedAsn", ParamType.STRING_LIST,
                        "Only events involving these ASNs (one is filtered upstream, several locally)", List.of()))
                .param(ParamSpec.optional("involvedCountry", ParamType.STRING_LIST,
                        "Only events involving these alpha-2 countries (one is filtered upstream, several locally)",
                        List.of()))
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                // A full run is two endpoints of up to five pages each, sent back to back.
                .defaultBudget(2 * MAX_PAGES, 1_000)
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
