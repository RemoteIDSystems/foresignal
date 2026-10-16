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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code cloudflare-radar-outages} (prompt 092): Cloudflare Radar internet-outage annotations.
 *
 * <p>Needs a Cloudflare API token with Radar read access (Bearer). Annotations carry no update time, so the
 * source's time base is RECEIVED and {@code eventTime} is the outage start. Geometry is the centroids of the
 * affected locations, else of the affected ASNs' countries. Radar's {@code location} filter takes one alpha-2 code,
 * so a single code is sent upstream and several are filtered locally; every query value is URL-encoded.
 */
@Component
public final class CloudflareOutageAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "cloudflare-radar-outages";

    /** The record kind. */
    public static final String KIND = "internet-outage";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://api.cloudflare.com";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.cloudflare-radar-outages.base-url";

    /** The credential name. */
    public static final String API_TOKEN = "apiToken";

    static final Duration RETENTION = Duration.ofDays(1825);

    private static final JsonMapper JSON = JsonMapper.builder().build();
    private static final GeometryFactory FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    private final URI baseUrl;
    private final CountryCentroids countries;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public CloudflareOutageAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride,
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
                    "cloudflare-radar-outages needs a Radar API token");
        }
        String dateRange = ctx.params().string("dateRange");
        List<String> locations = new ArrayList<>();
        for (String code : ctx.params().stringList("location")) {
            if (code != null && !code.isBlank()) {
                locations.add(code.strip().toUpperCase(Locale.ROOT));
            }
        }
        StringBuilder url = new StringBuilder(baseUrl + "/client/v4/radar/annotations/outages?format=JSON&limit=100")
                .append("&dateRange=").append(encode(dateRange == null || dateRange.isBlank() ? "7d" : dateRange));
        // Radar's location filter takes one alpha-2 code; several are filtered here instead.
        if (locations.size() == 1) {
            url.append("&location=").append(encode(locations.get(0)));
        }
        Integer asn = ctx.params().integer("asn");
        if (asn != null) {
            url.append("&asn=").append(asn);
        }
        UpstreamResponse response = ctx.http().get(URI.create(url.toString()),
                Map.of("Authorization", "Bearer " + token));
        if (response.status() != 200) {
            throw new UpstreamFormatException("Cloudflare Radar answered HTTP " + response.status(), "");
        }
        List<NormalizedRecord> records = parse(response.body());
        if (locations.size() > 1) {
            records = records.stream().filter(r -> touches(r, locations)).toList();
        }
        return FetchResult.of(records);
    }

    private static boolean touches(NormalizedRecord record, List<String> locations) {
        Object codes = record.payload().get("locations");
        if (codes instanceof List<?> list) {
            for (Object code : list) {
                if (code != null && locations.contains(code.toString().toUpperCase(Locale.ROOT))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /** The records of a Radar outages body. */
    public List<NormalizedRecord> parse(byte[] body) {
        JsonNode annotations = JSON.readTree(body).path("result").path("annotations");
        List<NormalizedRecord> out = new ArrayList<>();
        if (!annotations.isArray()) {
            return out;
        }
        for (JsonNode annotation : annotations) {
            NormalizedRecord record = map(annotation);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord map(JsonNode annotation) {
        String id = text(annotation, "id");
        if (id == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("cf-outage:" + id, KIND)
                .eventTime(time(annotation, "startDate"));
        b.put("startDate", text(annotation, "startDate"));
        b.put("endDate", text(annotation, "endDate"));
        b.put("ongoing", text(annotation, "endDate") == null);
        List<String> locationCodes = strings(annotation.get("locations"));
        b.put("locations", locationCodes);
        List<Integer> asns = integers(annotation.get("asns"));
        b.put("asns", asns);
        b.put("asnNames", asnNames(annotation.get("asnsDetails")));
        // The documented shape nests the cause and type under "outage"; older captures carry them flat.
        JsonNode outage = annotation.path("outage");
        b.put("outageCause", outage.isObject() && text(outage, "outageCause") != null
                ? text(outage, "outageCause") : text(annotation, "outageCause"));
        b.put("outageType", outage.isObject() && text(outage, "outageType") != null
                ? text(outage, "outageType") : text(annotation, "outageType"));
        b.put("scope", text(annotation, "scope"));
        b.put("description", ai.foresignal.source.adapters.humanitarian.HtmlText.strip(
                text(annotation, "description"), 2048));
        b.put("linkedUrl", text(annotation, "linkedUrl"));
        b.put("eventType", text(annotation, "eventType"));
        b.put("dataSource", text(annotation, "dataSource"));
        List<Point> points = new ArrayList<>();
        for (String code : locationCodes) {
            countries.byIso2(code).ifPresent(ref -> points.add(Geo.point(ref.labelLon(), ref.labelLat())));
        }
        if (points.isEmpty()) {
            for (JsonNode detail : asnDetails(annotation.get("asnsDetails"))) {
                // asnsDetails[].location is {"code":"US","name":"United States"}; accept a bare code as well.
                JsonNode location = detail.get("location");
                String code = location == null ? null
                        : location.isObject() ? text(location, "code") : text(detail, "location");
                countries.byIso2(code).ifPresent(ref -> {
                    Point point = Geo.point(ref.labelLon(), ref.labelLat());
                    if (points.stream().noneMatch(p -> p.equalsExact(point))) {
                        points.add(point);
                    }
                });
            }
        }
        if (points.size() == 1) {
            b.geometry(points.get(0));
        } else if (!points.isEmpty()) {
            b.geometry(FACTORY.createMultiPoint(points.toArray(new Point[0])));
        }
        if (!points.isEmpty()) {
            b.put("geometryApproximate", true);
        }
        return b.build();
    }

    private static List<JsonNode> asnDetails(JsonNode node) {
        List<JsonNode> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode detail : node) {
                out.add(detail);
            }
        }
        return out;
    }

    private static List<String> asnNames(JsonNode node) {
        List<String> out = new ArrayList<>();
        if (node != null && node.isArray()) {
            for (JsonNode detail : node) {
                String name = text(detail, "name");
                if (name != null) {
                    out.add(name);
                }
            }
        }
        return out;
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

    private static Instant time(JsonNode node, String field) {
        return ai.foresignal.source.adapters.humanitarian.Instants.parse(text(node, field));
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("Cloudflare Radar outages")
                .description("Internet-outage annotations from Cloudflare Radar. Needs a Cloudflare API token with "
                        + "Radar read access (Bearer). Annotations carry no update time, so the time base is "
                        + "RECEIVED. Country geometry is approximate.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://radar.cloudflare.com/outage-center")
                .attribution("Cloudflare Radar")
                .licence("CC BY-NC 4.0")
                .termsUrl("https://www.cloudflare.com/website-terms/")
                .credential(CredentialField.required(API_TOKEN, "Cloudflare API token with Radar read access"))
                .param(ParamSpec.optional("dateRange", ParamType.STRING, "Radar date range (e.g. 7d)", "7d"))
                .param(ParamSpec.optional("location", ParamType.STRING_LIST, "Alpha-2 country codes", List.of()))
                .param(ParamSpec.optional("asn", ParamType.INTEGER, "Restrict to one ASN", null))
                .interval(Duration.ofMinutes(15), Duration.ofHours(1))
                .defaultBudget(2, 60)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("description")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("Outage {scope}", "globe", "#C92A2A", null))
                .build();
    }
}
