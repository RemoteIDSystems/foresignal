package ai.foresignal.source.adapters.stac;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
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
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.spi.ValidatedParams;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * {@code stac-sentinel2}, {@code stac-landsat} and {@code stac-sentinel1} (prompt 096): new Sentinel-2,
 * Landsat and (optionally) Sentinel-1 scenes over the operator's AOI from the Earth Search STAC API
 * ({@code https://earth-search.aws.element84.com/v1}, STAC 1.0.0).
 *
 * <p><strong>An AOI is required</strong> ({@code aoi}, a {@code west,south,east,north} box or a 036 place
 * id): the source cannot be enabled without it, so a global scan can never be configured. A box that
 * crosses the antimeridian is split into two requests.
 *
 * <p><strong>Search.</strong> One {@code POST {base}/search} per page with {@code collections},
 * {@code intersects} (a polygon for a place AOI) or {@code bbox} (for a box AOI), {@code datetime},
 * the {@code query} cloud clause ({@code eo:cloud_cover < maxCloudCover}; CQL2 {@code filter} is silently
 * ignored by Earth Search and is never used), {@code sortby} newest-first, {@code limit} 100 and the
 * {@code fields} projection of {@link #FIELD_INCLUDE}. The {@code rel=next} link is followed verbatim at
 * most {@code maxPages} times. The newest acquisition seen is stored in adapter state after a successful
 * run, and the next run starts {@value #OVERLAP_DAYS} days behind it to absorb late processing.
 *
 * <p><strong>Links only.</strong> Foresignal never downloads, proxies or signs an imagery asset; the
 * record carries the item link and any {@code https://} thumbnail. {@link StacItemParser} maps each item.
 */
public class EarthSearchAdapter implements PollingAdapter {

    /** The record kind. */
    public static final String KIND = StacItemParser.KIND;

    /** The production upstream. */
    public static final String DEFAULT_BASE_URL = "https://earth-search.aws.element84.com/v1";

    /** The search path. */
    static final String SEARCH_PATH = "/search";

    /** The {@code aoi} parameter: a bbox or a place id. */
    public static final String AOI_PARAM = "aoi";

    /** The {@code maxCloudCover} parameter. */
    public static final String MAX_CLOUD_COVER_PARAM = "maxCloudCover";

    /** The {@code lookbackDays} parameter (first run only). */
    public static final String LOOKBACK_DAYS_PARAM = "lookbackDays";

    /** The {@code maxPages} parameter. */
    public static final String MAX_PAGES_PARAM = "maxPages";

    /** The default cloud-cover ceiling, percent. */
    public static final double DEFAULT_MAX_CLOUD_COVER = 30.0;

    /** The default first-run lookback, days. */
    public static final int DEFAULT_LOOKBACK_DAYS = 3;

    /** The default page cap. */
    public static final int DEFAULT_MAX_PAGES = 5;

    /** The most pages a run may read. */
    public static final int MAX_PAGES_CEILING = 10;

    /** The items requested per page. */
    public static final int PAGE_LIMIT = 100;

    /** How far behind the last acquisition a run starts, absorbing late processing. */
    public static final Duration OVERLAP = Duration.ofDays(2);

    /** How far after now an acquisition may lie (upstream and local clock skew). */
    static final Duration FORWARD_SLACK = Duration.ofDays(1);

    /** The half-width of the box built around a place point, degrees. */
    static final double PLACE_DEGREES = 0.25;

    /** The adapter-state key of the newest acquisition already handed off. */
    static final String LAST_ACQUISITION = "stac:lastAcquisition";

    /** Raw retention: the prompt's 90 days (the {@code bulk-event} analytic class). */
    static final Duration RETENTION = Duration.ofDays(90);

    /** The response cap. */
    static final int MAX_RESPONSE_BYTES = 8 * 1024 * 1024;

    /** The {@code fields.include} projection of every search request (prompt 096). */
    static final List<String> FIELD_INCLUDE = List.of(
            "id",
            "collection",
            "geometry",
            "bbox",
            "properties.datetime",
            "properties.created",
            "properties.updated",
            "properties.eo:cloud_cover",
            "properties.platform",
            "properties.constellation",
            "properties.instruments",
            "properties.s2:mgrs_tile",
            "properties.grid:code",
            "properties.landsat:wrs_path",
            "properties.landsat:wrs_row",
            "properties.view:sun_elevation",
            "properties.sar:instrument_mode",
            "properties.sar:polarizations",
            "assets",
            "links");

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private static final Pattern BBOX = Pattern.compile(
            "^\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*,\\s*-?\\d+(?:\\.\\d+)?\\s*$");

    private final StacSource source;
    private final String baseUrl;
    private final Supplier<PlacePoints> places;
    private final SourceDescriptor descriptor;

    /**
     * Creates the adapter.
     *
     * @param source the reviewed feed
     * @param baseUrlOverride the test base URL, or blank for {@link #DEFAULT_BASE_URL}
     * @param places resolves a place id to a point
     */
    public EarthSearchAdapter(StacSource source, String baseUrlOverride, Supplier<PlacePoints> places) {
        this.source = source;
        String chosen = baseUrlOverride == null || baseUrlOverride.isBlank()
                ? DEFAULT_BASE_URL
                : baseUrlOverride.strip();
        this.baseUrl = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.places = places == null ? () -> null : places;
        this.descriptor = buildDescriptor(source);
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The reviewed feed this adapter serves. */
    public StacSource stacSource() {
        return source;
    }

    /** The base URL, without a trailing slash. */
    public String baseUrl() {
        return baseUrl;
    }

    /** Refuses an {@code aoi} that is neither a bbox nor a resolvable place id, and out-of-range numbers. */
    @Override
    public Map<String, String> checkParams(ValidatedParams params) {
        Map<String, String> problems = new LinkedHashMap<>();
        String aoi = params.string(AOI_PARAM);
        if (aoi == null || aoi.isBlank()) {
            problems.put(AOI_PARAM, "is required: give a bbox (west,south,east,north) or a place id");
        } else if (!isBbox(aoi) && resolvePlace(aoi) == null) {
            problems.put(AOI_PARAM, "'" + aoi + "' is neither a west,south,east,north box nor a known place id");
        }
        if (params.has(MAX_CLOUD_COVER_PARAM) && source.cloudFiltered()) {
            Double max = params.number(MAX_CLOUD_COVER_PARAM);
            if (max == null || max < 0 || max > 100) {
                problems.put(MAX_CLOUD_COVER_PARAM, "must be between 0 and 100, but was " + max);
            }
        }
        if (params.has(LOOKBACK_DAYS_PARAM)) {
            Integer days = params.integer(LOOKBACK_DAYS_PARAM);
            if (days == null || days < 1 || days > 30) {
                problems.put(LOOKBACK_DAYS_PARAM, "must be between 1 and 30 days, but was " + days);
            }
        }
        if (params.has(MAX_PAGES_PARAM)) {
            Integer pages = params.integer(MAX_PAGES_PARAM);
            if (pages == null || pages < 1 || pages > MAX_PAGES_CEILING) {
                problems.put(MAX_PAGES_PARAM, "must be between 1 and " + MAX_PAGES_CEILING + ", but was " + pages);
            }
        }
        return problems;
    }

    /**
     * One run: new scenes over the AOI, one page after another, newest-first.
     *
     * @throws AdapterException when the AOI cannot be resolved or a request fails
     * @throws UpstreamFormatException when a body is truncated, unparseable, or the cloud/time filter was
     *     ignored
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        ValidatedParams params = ctx.params();
        Aoi aoi = resolveAoi(params);
        if (aoi == null) {
            throw new AdapterException(id() + ": the aoi parameter is required and must resolve to a box or place");
        }
        double maxCloudCover = maxCloudCover(params);
        int maxPages = maxPages(params);
        Instant now = ctx.now();
        Instant from = lastAcquisition(ctx.state()).map(mark -> mark.minus(OVERLAP))
                .orElse(now.minus(Duration.ofDays(lookbackDays(params))));
        Instant until = now.plus(FORWARD_SLACK);
        StacItemParser.Options options = new StacItemParser.Options(source.collection(), source.cloudFiltered(),
                maxCloudCover, from, until, aoi.geometry());

        Map<String, NormalizedRecord> out = new LinkedHashMap<>();
        Instant newest = null;
        for (AoiPart part : aoi.parts()) {
            ObjectNode body = body(part, from, maxCloudCover);
            for (int page = 0; page < maxPages; page++) {
                UpstreamResponse response = ctx.http().post(searchUri(), "application/json",
                        body.toString().getBytes(StandardCharsets.UTF_8), Map.of("Accept", "application/geo+json"));
                if (response.status() != 200) {
                    throw new UpstreamFormatException(
                            "Earth Search answered HTTP " + response.status() + " for " + source.collection(), "");
                }
                if (response.truncated()) {
                    throw new UpstreamFormatException("The Earth Search response exceeded maxResponseBytes; lower"
                            + " maxPages or narrow the AOI", null);
                }
                StacItemParser.Result parsed = StacItemParser.parse(response.body(), options);
                for (NormalizedRecord record : parsed.records()) {
                    out.putIfAbsent(record.externalId(), record);
                }
                if (parsed.newest() != null && (newest == null || parsed.newest().isAfter(newest))) {
                    newest = parsed.newest();
                }
                if (parsed.olderSeen()) {
                    break;
                }
                ObjectNode next = nextBody(response.body(), body);
                if (next == null) {
                    break;
                }
                body = next;
            }
        }
        if (newest != null) {
            ctx.state().put(LAST_ACQUISITION, newest.toString());
        }
        ctx.log().debug("{}: {} scene(s) from {}, since {}", id(), out.size(), source.collection(), from);
        return FetchResult.of(new ArrayList<>(out.values()));
    }

    /** The search URI of this adapter's base. */
    URI searchUri() {
        return URI.create(baseUrl + SEARCH_PATH);
    }

    /** The request body for one AOI part. */
    ObjectNode body(AoiPart part, Instant from, double maxCloudCover) {
        ObjectNode root = JSON.createObjectNode();
        root.putArray("collections").add(source.collection());
        if (part.bbox() != null) {
            ArrayNode bbox = root.putArray("bbox");
            for (double value : part.bbox()) {
                bbox.add(value);
            }
        } else {
            root.set("intersects", part.intersects());
        }
        root.put("datetime", DateTimeFormatter.ISO_INSTANT.format(from) + "/..");
        if (source.cloudFiltered()) {
            root.putObject("query").putObject("eo:cloud_cover").put("lt", maxCloudCover);
        }
        ArrayNode sortby = root.putArray("sortby");
        ObjectNode sort = sortby.addObject();
        sort.put("field", "properties.datetime");
        sort.put("direction", "desc");
        root.put("limit", PAGE_LIMIT);
        ArrayNode include = root.putObject("fields").putArray("include");
        FIELD_INCLUDE.forEach(include::add);
        return root;
    }

    /** The next page's body from the {@code rel=next} link, or null when the page is the last. */
    static ObjectNode nextBody(byte[] response, ObjectNode current) {
        JsonNode root;
        try {
            root = JSON.readTree(response);
        } catch (JacksonException e) {
            return null;
        }
        JsonNode links = root == null ? null : root.get("links");
        if (links == null || !links.isArray()) {
            return null;
        }
        for (JsonNode link : links) {
            if (link == null || !"next".equals(text(link.get("rel")))) {
                continue;
            }
            JsonNode body = link.get("body");
            if (body != null && body.isObject()) {
                return (ObjectNode) body.deepCopy();
            }
            String next = text(link.get("next"));
            if (next == null) {
                next = text(link.get("token"));
            }
            if (next != null) {
                ObjectNode copy = current.deepCopy();
                copy.put("next", next);
                return copy;
            }
        }
        return null;
    }

    private Aoi resolveAoi(ValidatedParams params) {
        String aoi = params.string(AOI_PARAM);
        if (aoi == null || aoi.isBlank()) {
            return null;
        }
        if (isBbox(aoi)) {
            String[] parts = aoi.split(",");
            double west = Double.parseDouble(parts[0].strip());
            double south = Double.parseDouble(parts[1].strip());
            double east = Double.parseDouble(parts[2].strip());
            double north = Double.parseDouble(parts[3].strip());
            return bboxAoi(west, south, east, north);
        }
        double[] point = resolvePlace(aoi);
        if (point == null) {
            return null;
        }
        double[] box = {point[0] - PLACE_DEGREES, point[1] - PLACE_DEGREES,
            point[0] + PLACE_DEGREES, point[1] + PLACE_DEGREES};
        List<AoiPart> parts = List.of(new AoiPart(null, polygonNode(box)));
        return new Aoi(boxGeometry(List.of(box)), parts);
    }

    /** A box AOI, split into two parts when it crosses the antimeridian. */
    static Aoi bboxAoi(double west, double south, double east, double north) {
        List<double[]> boxes = new ArrayList<>();
        if (west <= east) {
            boxes.add(new double[] {west, south, east, north});
        } else {
            boxes.add(new double[] {west, south, 180.0, north});
            boxes.add(new double[] {-180.0, south, east, north});
        }
        List<AoiPart> parts = new ArrayList<>();
        for (double[] box : boxes) {
            parts.add(new AoiPart(box, null));
        }
        return new Aoi(boxGeometry(boxes), parts);
    }

    private double[] resolvePlace(String placeId) {
        PlacePoints lookup = places.get();
        return lookup == null ? null : lookup.point(placeId).orElse(null);
    }

    private static Geometry boxGeometry(List<double[]> boxes) {
        Polygon[] polygons = new Polygon[boxes.size()];
        for (int i = 0; i < polygons.length; i++) {
            polygons[i] = boxPolygon(boxes.get(i));
        }
        Geometry multi = ai.foresignal.source.spi.Geo.factory().createMultiPolygon(polygons);
        multi.setSRID(ai.foresignal.source.spi.Geo.SRID);
        return multi;
    }

    private static Polygon boxPolygon(double[] box) {
        Polygon polygon = (Polygon) ai.foresignal.source.spi.Geo.factory()
                .toGeometry(new Envelope(box[0], box[2], box[1], box[3]));
        polygon.setSRID(ai.foresignal.source.spi.Geo.SRID);
        return polygon;
    }

    /** A JSON GeoJSON Polygon for the {@code intersects} member. */
    private static ObjectNode polygonNode(double[] box) {
        ObjectNode geometry = JSON.createObjectNode();
        geometry.put("type", "Polygon");
        ArrayNode ring = geometry.putArray("coordinates").addArray();
        ring.addArray().add(box[0]).add(box[1]);
        ring.addArray().add(box[2]).add(box[1]);
        ring.addArray().add(box[2]).add(box[3]);
        ring.addArray().add(box[0]).add(box[3]);
        ring.addArray().add(box[0]).add(box[1]);
        return geometry;
    }

    private double maxCloudCover(ValidatedParams params) {
        if (!source.cloudFiltered()) {
            return DEFAULT_MAX_CLOUD_COVER;
        }
        Double value = params.number(MAX_CLOUD_COVER_PARAM);
        if (value == null) {
            return DEFAULT_MAX_CLOUD_COVER;
        }
        return Math.max(0, Math.min(100, value));
    }

    private int lookbackDays(ValidatedParams params) {
        Integer days = params.integer(LOOKBACK_DAYS_PARAM);
        return days == null ? DEFAULT_LOOKBACK_DAYS : days;
    }

    private int maxPages(ValidatedParams params) {
        Integer pages = params.integer(MAX_PAGES_PARAM);
        if (pages == null) {
            return DEFAULT_MAX_PAGES;
        }
        return Math.max(1, Math.min(MAX_PAGES_CEILING, pages));
    }

    private static Optional<Instant> lastAcquisition(AdapterStateStore state) {
        return state.get(LAST_ACQUISITION).map(value -> {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException e) {
                return null;
            }
        });
    }

    static boolean isBbox(String value) {
        return value != null && BBOX.matcher(value).matches();
    }

    private static String text(JsonNode node) {
        if (node == null || node.isNull() || node.isContainer()) {
            return null;
        }
        String value = node.asString().strip();
        return value.isEmpty() ? null : value;
    }

    /** One AOI part: an array box for a box AOI, or a GeoJSON polygon for a place AOI. */
    record AoiPart(double[] bbox, ObjectNode intersects) {}

    /** The resolved AOI: the geometry coverage is measured against and the request parts. */
    record Aoi(Geometry geometry, List<AoiPart> parts) {
        Aoi {
            parts = List.copyOf(parts);
        }
    }

    private static SourceDescriptor buildDescriptor(StacSource source) {
        SourceDescriptor.Builder b = SourceDescriptor.builder(source.id())
                .name(source.name())
                .description("New " + source.name() + " scenes (" + source.collection() + ") over the operator's"
                        + " area of interest, from the Earth Search STAC API. Records carry the footprint,"
                        + " acquisition time, cloud cover, AOI coverage and links to the STAC item and any public"
                        + " thumbnail; Foresignal never downloads or proxies imagery. An AOI (a bbox or a place id)"
                        + " is required.")
                .kind(KIND)
                .homepage(source.homepage())
                .attribution(source.attribution())
                .licence(source.licence())
                .termsUrl(source.termsUrl())
                .mode(Mode.POLLING)
                .param(ParamSpec.required(AOI_PARAM, ParamType.STRING,
                        "The area to watch: a bbox west,south,east,north or a place id. Required."))
                .param(ParamSpec.optional(MAX_CLOUD_COVER_PARAM, ParamType.NUMBER,
                        "Maximum cloud cover, percent (ignored for Sentinel-1).", DEFAULT_MAX_CLOUD_COVER))
                .param(ParamSpec.optional(LOOKBACK_DAYS_PARAM, ParamType.INTEGER,
                        "On the first run, how many days back to look.", DEFAULT_LOOKBACK_DAYS))
                .param(ParamSpec.optional(MAX_PAGES_PARAM, ParamType.INTEGER,
                        "The most pages of 100 items one run reads.", DEFAULT_MAX_PAGES))
                .interval(Duration.ofMinutes(15), Duration.ofHours(1))
                .defaultBudget(Budget.of(4, 300))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .recordTtl(null)
                .defaultIngestPolicy(IngestPolicy.all())
                .defaultRetention(RETENTION)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .zeroResultsNormal(true)
                .titleField("title")
                .searchFields("platform", "mgrsTile", "gridCode")
                .tileProperties("platform", "cloudCover", "aoiCoverPct", "eventTime")
                .kindDisplay(KIND, new KindDisplay("{platform} {gridCode} {cloudCover}%", "globe", "#7048E8", null));
        b.fieldSpec("collection", new FieldSpec(FieldType.STRING, null, "Collection", "The STAC collection", null,
                Set.of()));
        b.fieldSpec("platform", new FieldSpec(FieldType.STRING, null, "Platform",
                "The satellite platform (for example sentinel-2c or landsat-9)", null, Set.of()));
        b.fieldSpec("constellation", new FieldSpec(FieldType.STRING, null, "Constellation", null, null, Set.of()));
        b.fieldSpec("instruments", new FieldSpec(FieldType.ARRAY, null, "Instruments",
                "The instrument names carried on the item", null, Set.of()));
        b.fieldSpec("cloudCover", new FieldSpec(FieldType.NUMBER, "%", "Cloud cover",
                "Scene cloud cover, percent (absent for SAR)", null, Set.of()));
        b.fieldSpec("mgrsTile", new FieldSpec(FieldType.STRING, null, "MGRS tile",
                "The Sentinel-2 MGRS tile (s2:mgrs_tile)", null, Set.of()));
        b.fieldSpec("gridCode", new FieldSpec(FieldType.STRING, null, "Grid code",
                "The item's reference grid code (grid:code)", null, Set.of()));
        b.fieldSpec("wrsPath", new FieldSpec(FieldType.STRING, null, "WRS path",
                "The Landsat WRS-2 path", null, Set.of()));
        b.fieldSpec("wrsRow", new FieldSpec(FieldType.STRING, null, "WRS row", "The Landsat WRS-2 row", null,
                Set.of()));
        b.fieldSpec("sunElevation", new FieldSpec(FieldType.NUMBER, "deg", "Sun elevation",
                "The sun elevation above the horizon at acquisition", null, Set.of()));
        b.fieldSpec("sarMode", new FieldSpec(FieldType.STRING, null, "SAR mode",
                "The SAR instrument mode (for example IW)", null, Set.of()));
        b.fieldSpec("polarizations", new FieldSpec(FieldType.ARRAY, null, "Polarizations",
                "The SAR polarizations (for example VV, VH)", null, Set.of()));
        b.fieldSpec("aoiCoverPct", new FieldSpec(FieldType.INTEGER, "%", "AOI coverage",
                "The fraction of the AOI the footprint covers, percent", null, Set.of()));
        b.fieldSpec("stacItemUrl", new FieldSpec(FieldType.STRING, null, "STAC item",
                "The item's self link", null, Set.of()));
        b.fieldSpec("thumbnailUrl", new FieldSpec(FieldType.STRING, null, "Thumbnail",
                "A public https thumbnail, when the collection publishes one", null, Set.of()));
        b.fieldSpec("assetKeys", new FieldSpec(FieldType.ARRAY, null, "Assets",
                "The sorted asset names carried on the item", null, Set.of()));
        b.fieldSpec("eventTime", new FieldSpec(FieldType.TIMESTAMP, null, "Acquisition",
                "The acquisition time", null, Set.of()));
        b.fieldSpec("title", new FieldSpec(FieldType.STRING, null, "Title", null, null, Set.of()));
        return b.build();
    }
}
