package ai.foresignal.source.adapters.usgs;

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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.simplify.TopologyPreservingSimplifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * {@code usgs-shakemap} (prompt 089): USGS ShakeMap and PAGER impact products for recent events.
 *
 * <p>The FDSN event index is queried for events with a {@code shakemap} product updated since the cursor. For each
 * changed event the detail document is fetched once and read for the shakemap product (maxMMI, version), the
 * loss-PAGER product (alert levels) and the {@code cont_mmi.json} contour file. Two kinds are written:
 * {@code earthquake-impact} (one per event) and {@code shakemap-mmi} (one per MMI level, a simplified
 * MultiLineString; a level that disappears in a newer version is tombstoned).
 */
@Component
public final class UsgsShakeMapAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "usgs-shakemap";

    /** The {@code earthquake-impact} kind. */
    public static final String KIND_IMPACT = "earthquake-impact";

    /** The {@code shakemap-mmi} kind. */
    public static final String KIND_MMI = "shakemap-mmi";

    /** The default FDSN base. */
    public static final String DEFAULT_BASE_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    /** Test-only override of the FDSN base. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.usgs-shakemap.base-url";

    /** The cursor key (the newest index {@code updated}). */
    static final String CURSOR_KEY = "shakemap:last";

    /** The most vertices a contour may keep. */
    static final int MAX_VERTICES = 10_000;

    /** The first simplification tolerance, degrees. */
    static final double FIRST_TOLERANCE = 0.001;

    static final Duration RETENTION = Duration.ofDays(1825);
    static final Duration WINDOW = Duration.ofDays(7);
    static final Duration OVERLAP = Duration.ofMinutes(5);

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI baseUrl;
    private final SourceDescriptor descriptor;
    private final GeoJsonReader reader = new GeoJsonReader(500_000, 8);

    /** Creates the adapter. */
    @Autowired
    public UsgsShakeMapAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
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
        double minMagnitude = ctx.params().number("minMagnitude") == null ? 4.5 : ctx.params().number("minMagnitude");
        String from = ctx.state().get(CURSOR_KEY).orElseGet(() -> ctx.now().minus(WINDOW).toString());
        String url = baseUrl + (baseUrl.toString().contains("?") ? "&" : "?")
                + "format=geojson&producttype=shakemap&minmagnitude=" + minMagnitude + "&orderby=time"
                + "&updatedafter=" + from;
        UpstreamResponse index = ctx.http().get(URI.create(url));
        if (index.status() != 200) {
            throw new UpstreamFormatException("The USGS ShakeMap index answered HTTP " + index.status(), "");
        }
        JsonNode features = JSON.readTree(index.body()).get("features");
        if (features == null || !features.isArray()) {
            throw new UpstreamFormatException("The USGS ShakeMap index has no features array", "");
        }
        List<NormalizedRecord> records = new ArrayList<>();
        Instant newest = null;
        for (JsonNode feature : features) {
            JsonNode props = feature.get("properties");
            if (props == null) {
                continue;
            }
            Instant updated = epochMs(props.get("updated"));
            String eventId = text(feature, "id");
            if (eventId == null || updated == null) {
                continue;
            }
            if (newest == null || updated.isAfter(newest)) {
                newest = updated;
            }
            if (alreadyProcessed(ctx, eventId, updated)) {
                continue;
            }
            process(ctx, feature, eventId, updated, records);
            ctx.state().put("shakemap:seen:" + eventId, Long.toString(updated.toEpochMilli()));
        }
        if (newest != null) {
            ctx.state().put(CURSOR_KEY, newest.minus(OVERLAP).toString());
        }
        return FetchResult.of(records);
    }

    private boolean alreadyProcessed(FetchContext ctx, String eventId, Instant updated) {
        String seen = ctx.state().get("shakemap:seen:" + eventId).orElse(null);
        if (seen == null) {
            return false;
        }
        try {
            return Long.parseLong(seen) >= updated.toEpochMilli();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void process(FetchContext ctx, JsonNode feature, String eventId, Instant updated,
            List<NormalizedRecord> records) throws AdapterException {
        JsonNode props = feature.get("properties");
        String detailUrl = text(props, "detail");
        if (detailUrl == null) {
            return;
        }
        JsonNode detail;
        try {
            UpstreamResponse response = ctx.http().get(URI.create(detailUrl));
            if (response.status() != 200) {
                return;
            }
            detail = JSON.readTree(response.body());
        } catch (AdapterException e) {
            return;
        }
        JsonNode products = detail.get("properties") == null ? null : detail.get("properties").get("products");
        if (products == null) {
            return;
        }
        Instant origin = epochMs(props.get("time"));
        JsonNode shakemap = first(products.get("shakemap"));
        JsonNode pager = first(products.get("losspager"));
        String place = text(props, "place");
        Double mag = number(props.get("mag"));
        Double maxMmi = shakemap == null ? null : doubleOrNull(text(shakemap.get("properties"), "maxmmi"));
        String version = shakemap == null ? null : text(shakemap.get("properties"), "version");
        NormalizedRecord.Builder impact = NormalizedRecord.builder(eventId, KIND_IMPACT)
                .observedAt(updated)
                .eventTime(origin);
        point(feature, impact);
        impact.put("maxMmi", maxMmi);
        impact.put("pagerAlert", pager == null ? null : text(pager.get("properties"), "alertlevel"));
        impact.put("pagerFatalityAlert", pager == null ? null : text(pager.get("properties"), "alertfatality"));
        impact.put("pagerEconomicAlert", pager == null ? null : text(pager.get("properties"), "alertecon"));
        impact.put("shakemapVersion", version);
        impact.put("mag", mag);
        impact.put("place", place);
        records.add(impact.build());
        contours(ctx, shakemap, eventId, updated, origin, records);
    }

    private void contours(FetchContext ctx, JsonNode shakemap, String eventId, Instant updated, Instant origin,
            List<NormalizedRecord> records) throws AdapterException {
        Set<String> levels = new TreeSet<>();
        String contourUrl = null;
        if (shakemap != null && shakemap.get("contents") != null) {
            JsonNode content = shakemap.get("contents").get("download/cont_mmi.json");
            if (content != null) {
                contourUrl = text(content, "url");
            }
        }
        if (contourUrl != null) {
            try {
                UpstreamResponse response = ctx.http().get(URI.create(contourUrl));
                if (response.status() == 200) {
                    JsonNode features = JSON.readTree(response.body()).get("features");
                    if (features != null && features.isArray()) {
                        for (JsonNode contour : features) {
                            String level = level(contour);
                            if (level == null) {
                                continue;
                            }
                            Geometry geometry = simplify(reader.read(contour.get("geometry")));
                            levels.add(level);
                            NormalizedRecord.Builder mmi = NormalizedRecord.builder(eventId + ":mmi" + level, KIND_MMI)
                                    .observedAt(updated)
                                    .eventTime(origin)
                                    .geometry(geometry);
                            mmi.put("mmi", doubleOrNull(level));
                            mmi.put("units", "mmi");
                            records.add(mmi.build());
                        }
                    }
                }
            } catch (AdapterException e) {
                // A missing contour file is not fatal; a previous version's levels stay until one disappears.
                return;
            }
        }
        tombstoneGone(ctx, eventId, updated, levels, records);
    }

    private void tombstoneGone(FetchContext ctx, String eventId, Instant updated, Set<String> current,
            List<NormalizedRecord> records) {
        String key = "shakemap:mmi:" + eventId;
        String stored = ctx.state().get(key).orElse("");
        Set<String> previous = new LinkedHashSet<>();
        for (String level : stored.split(",")) {
            if (!level.isBlank()) {
                previous.add(level);
            }
        }
        for (String level : previous) {
            if (!current.contains(level)) {
                records.add(NormalizedRecord.tombstone(eventId + ":mmi" + level, KIND_MMI, updated));
            }
        }
        if (current.isEmpty()) {
            ctx.state().remove(key);
        } else {
            ctx.state().put(key, String.join(",", current));
        }
    }

    private static String level(JsonNode contour) {
        JsonNode props = contour.get("properties");
        if (props == null || props.get("value") == null || props.get("value").isNull()) {
            return null;
        }
        double value = props.get("value").asDouble();
        return value == Math.rint(value) ? Long.toString((long) value) : Double.toString(value);
    }

    /** Simplifies {@code geometry} with a growing topology-preserving tolerance until it is under the vertex cap. */
    static Geometry simplify(Geometry geometry) {
        if (geometry == null || geometry.getNumPoints() <= MAX_VERTICES) {
            return geometry;
        }
        double tolerance = FIRST_TOLERANCE;
        Geometry simplified = geometry;
        while (simplified.getNumPoints() > MAX_VERTICES && tolerance < 10) {
            simplified = TopologyPreservingSimplifier.simplify(geometry, tolerance);
            tolerance *= 2;
        }
        return simplified;
    }

    private static void point(JsonNode feature, NormalizedRecord.Builder builder) {
        JsonNode coordinates = feature.get("geometry") == null ? null : feature.get("geometry").get("coordinates");
        if (coordinates != null && coordinates.isArray() && coordinates.size() >= 2) {
            builder.point(coordinates.get(0).asDouble(), coordinates.get(1).asDouble());
        }
    }

    private static JsonNode first(JsonNode array) {
        return array != null && array.isArray() && !array.isEmpty() ? array.get(0) : null;
    }

    private static String text(JsonNode node, String field) {
        if (node == null) {
            return null;
        }
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asString();
    }

    private static Double number(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asDouble();
        }
        return doubleOrNull(node.asString());
    }

    private static Double doubleOrNull(String value) {
        if (value == null || value.isBlank() || "nan".equalsIgnoreCase(value)) {
            return null;
        }
        try {
            return Double.parseDouble(value.strip());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Instant epochMs(JsonNode node) {
        if (node == null || node.isNull() || !node.isNumber()) {
            return null;
        }
        return Instant.ofEpochMilli(node.asLong()).truncatedTo(ChronoUnit.MILLIS);
    }

    private static SourceDescriptor buildDescriptor() {
        return SourceDescriptor.builder(ID)
                .name("USGS ShakeMap and PAGER")
                .description("USGS ShakeMap and loss-PAGER impact products for recent earthquakes. The FDSN event "
                        + "index is polled for events whose shakemap product changed; the detail document is fetched "
                        + "once per change for maxMMI, the PAGER alert levels and the MMI contour lines. Contours are "
                        + "simplified to at most " + MAX_VERTICES + " vertices; a level that disappears in a newer "
                        + "version is tombstoned.")
                .kinds(KIND_IMPACT, KIND_MMI)
                .mode(Mode.POLLING)
                .homepage("https://earthquake.usgs.gov/data/shakemap/")
                .attribution("United States Geological Survey (USGS)")
                .licence("US-PD")
                .termsUrl("https://www.usgs.gov/information-policies-and-instructions/copyrights-and-credits")
                .param(ParamSpec.optional("minMagnitude", ParamType.NUMBER,
                        "Only events at or above this magnitude are indexed", 4.5))
                .interval(Duration.ofMinutes(10), Duration.ofMinutes(10))
                .defaultBudget(20, 1_500)
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .searchFields("place")
                .zeroResultsNormal(true)
                .kindDisplay(KIND_IMPACT, new KindDisplay("Impact: {place}", "earthquake", "#C92A2A", null))
                .kindDisplay(KIND_MMI, new KindDisplay("MMI {mmi} contour", "outlook", "#9C36B5", null))
                .build();
    }
}
