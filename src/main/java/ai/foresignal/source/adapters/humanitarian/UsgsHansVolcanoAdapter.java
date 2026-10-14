package ai.foresignal.source.adapters.humanitarian;

import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.IngestPolicy;
import ai.foresignal.source.spi.KindDisplay;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code usgs-hans-volcanoes} (prompt 090): USGS HANS monitored-volcano alert levels, with coordinates from the
 * US volcano list cached in adapter state and refreshed weekly.
 */
@Component
public final class UsgsHansVolcanoAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "usgs-hans-volcanoes";

    /** The record kind. */
    public static final String KIND = "volcano-alert";

    /** The default base URL. */
    public static final String DEFAULT_BASE_URL = "https://volcanoes.usgs.gov";

    /** Test-only base override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.usgs-hans-volcanoes.base-url";

    static final String COORDS_KEY = "hans:coords";
    static final String COORDS_AT_KEY = "hans:coordsAt";
    static final Duration COORDS_TTL = Duration.ofDays(7);
    static final Duration RETENTION = Duration.ofDays(365);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final SourceDescriptor descriptor;

    /** Creates the adapter. */
    @Autowired
    public UsgsHansVolcanoAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this.baseUrl = URI.create(baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL : baseUrlOverride.strip());
        this.descriptor = buildDescriptor();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        Map<String, JsonNode> coords = coordinates(ctx);
        UpstreamResponse response = ctx.http().get(URI.create(
                baseUrl + "/hans-public/api/volcano/getMonitoredVolcanoes"));
        if (response.status() != 200) {
            throw new UpstreamFormatException("USGS HANS answered HTTP " + response.status(), "");
        }
        return FetchResult.of(parse(response.body(), coords));
    }

    private Map<String, JsonNode> coordinates(FetchContext ctx) throws AdapterException {
        Instant at = parseInstant(ctx.state().get(COORDS_AT_KEY).orElse(null));
        if (at != null && at.plus(COORDS_TTL).isAfter(ctx.now())) {
            return readCoords(ctx.state().get(COORDS_KEY).orElse(null));
        }
        UpstreamResponse response = ctx.http().get(URI.create(
                baseUrl + "/hans-public/api/volcano/getUSVolcanoes"));
        if (response.status() != 200) {
            return readCoords(ctx.state().get(COORDS_KEY).orElse(null));
        }
        JsonNode root = JSON.readTree(response.body());
        Map<String, JsonNode> coords = new HashMap<>();
        for (JsonNode volcano : root.path("features").isArray() ? root.get("features") : root) {
            JsonNode properties = volcano.path("properties").isObject() ? volcano.get("properties") : volcano;
            String vnum = text(properties, "vnum");
            if (vnum != null) {
                coords.put(vnum, properties);
            }
        }
        ctx.state().put(COORDS_KEY, serialize(coords));
        ctx.state().put(COORDS_AT_KEY, ctx.now().toString());
        return coords;
    }

    /** The records of a monitored-volcanoes body. */
    public List<NormalizedRecord> parse(byte[] body, Map<String, JsonNode> coords) {
        JsonNode root = JSON.readTree(body);
        JsonNode array = root.isArray() ? root : root.path("volcanoes");
        List<NormalizedRecord> out = new ArrayList<>();
        for (JsonNode volcano : array) {
            NormalizedRecord record = record(volcano, coords);
            if (record != null) {
                out.add(record);
            }
        }
        return out;
    }

    private NormalizedRecord record(JsonNode volcano, Map<String, JsonNode> coords) {
        String vnum = text(volcano, "vnum");
        if (vnum == null) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder("usgs:" + vnum, KIND)
                .observedAt(parseInstant(text(volcano, "sent_utc")));
        JsonNode coord = coords.get(vnum);
        if (coord != null) {
            Double lon = number(coord, "longitude");
            Double lat = number(coord, "latitude");
            if (lon != null && lat != null) {
                b.point(lon, lat);
            }
            b.put("elevationM", number(coord, "elevation_meters"));
        }
        b.put("name", text(volcano, "volcano_name"));
        b.put("alertLevel", text(volcano, "alert_level"));
        b.put("aviationColor", text(volcano, "color_code"));
        b.put("observatory", text(volcano, "obs_abbr"));
        b.put("noticeId", text(volcano, "notice_identifier"));
        b.put("noticeUrl", text(volcano, "notice_url"));
        return b.build();
    }

    private static String serialize(Map<String, JsonNode> coords) {
        var root = JSON.createObjectNode();
        coords.forEach(root::set);
        return root.toString();
    }

    private static Map<String, JsonNode> readCoords(String stored) {
        Map<String, JsonNode> coords = new HashMap<>();
        if (stored == null || stored.isBlank()) {
            return coords;
        }
        try {
            JSON.readTree(stored).properties().forEach(entry -> coords.put(entry.getKey(), entry.getValue()));
        } catch (RuntimeException e) {
            // an unreadable cache is refetched next run
        }
        return coords;
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Double number(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.isNumber() ? value.asDouble() : null;
    }

    static Instant parseInstant(String value) {
        return value == null || value.isBlank() ? null : CopernicusEmsAdapter.parseInstant(value);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("USGS HANS volcano alert levels")
                .description("Alert level and aviation colour code of every monitored US volcano, from USGS HANS. "
                        + "Coordinates, cached weekly, come from the US volcano list.")
                .kind(KIND)
                .mode(Mode.POLLING)
                .homepage("https://volcanoes.usgs.gov/hans2/")
                .attribution("USGS Volcano Hazards Program")
                .licence("US-PD")
                .termsUrl("https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits")
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(10))
                .defaultBudget(4, 400)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .titleField("name")
                .searchFields("name", "observatory")
                .significantFields("alertLevel", "aviationColor")
                .zeroResultsNormal(true)
                .kindDisplay(KIND, new KindDisplay("{name} {aviationColor}", "mountain", "#E8590C", null))
                .build();
    }
}
