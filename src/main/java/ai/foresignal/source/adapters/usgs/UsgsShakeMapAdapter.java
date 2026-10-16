package ai.foresignal.source.adapters.usgs;

import ai.foresignal.collect.BudgetExceededException;
import ai.foresignal.source.formats.geojson.GeoJsonReader;
import ai.foresignal.source.kit.KitUrls;
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
import java.util.Comparator;
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
 *
 * <p><strong>Progress.</strong> Changed events are processed oldest change first, at most {@value #MAX_EVENTS_PER_RUN}
 * a run. An event is marked seen only once it is finished; a failed request leaves it for the next run (given up on
 * after {@value #MAX_ATTEMPTS} attempts), and a budget refusal stops the run. The cursor stops just before the first
 * unfinished change, so nothing is skipped. Detail and contour links are followed only on the index's own origin.
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

    /** The cursor key (the newest index {@code updated} fully processed, less the overlap). */
    static final String CURSOR_KEY = "shakemap:last";

    /** The state-key prefix of the {@code updated} time an event was processed at. */
    static final String SEEN_PREFIX = "shakemap:seen:";

    /** The state-key prefix of an event's failed attempts. */
    static final String FAIL_PREFIX = "shakemap:fail:";

    /**
     * The changed events processed per run: each costs two requests (detail and contours), so with the index this
     * keeps a run inside the 20-a-minute budget. A first run's 7-day window, or any burst, is worked off over the
     * following runs, oldest change first.
     */
    static final int MAX_EVENTS_PER_RUN = 9;

    /** Failed attempts at one event before it is given up on. */
    static final int MAX_ATTEMPTS = 3;

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
        List<Candidate> candidates = new ArrayList<>();
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
            if (!alreadyProcessed(ctx, eventId, updated)) {
                candidates.add(new Candidate(feature, eventId, updated));
            }
        }
        // Oldest change first, so a run cut short (the per-run cap or the budget) leaves only later changes, and the
        // cursor can stop just before the first of them.
        candidates.sort(Comparator.comparing(Candidate::updated));
        Instant pending = null;
        int processed = 0;
        for (Candidate c : candidates) {
            if (processed >= MAX_EVENTS_PER_RUN) {
                pending = earliest(pending, c.updated());
                break;
            }
            processed++;
            Outcome outcome;
            try {
                outcome = process(ctx, c.feature(), c.eventId(), c.updated(), records);
            } catch (BudgetExceededException e) {
                pending = earliest(pending, c.updated());
                ctx.log().info("{}: the budget stopped the run at {}; the next run continues", ctx.sourceId(),
                        c.eventId());
                break;
            }
            if (outcome == Outcome.DONE || giveUp(ctx, c.eventId())) {
                ctx.state().put(SEEN_PREFIX + c.eventId(), Long.toString(c.updated().toEpochMilli()));
                if (ctx.state().get(FAIL_PREFIX + c.eventId()).isPresent()) {
                    ctx.state().remove(FAIL_PREFIX + c.eventId());
                }
            } else {
                pending = earliest(pending, c.updated());
            }
        }
        Instant cursor = pending != null ? pending : newest;
        if (cursor != null) {
            ctx.state().put(CURSOR_KEY, cursor.minus(OVERLAP).toString());
        }
        return FetchResult.of(records);
    }

    /** One changed event of the index. */
    private record Candidate(JsonNode feature, String eventId, Instant updated) {}

    /** What processing one event achieved. */
    enum Outcome {
        /** Processed, or nothing to read (no detail, no products, the detail is gone): mark it seen. */
        DONE,
        /** A request failed: leave it unseen so the next run tries again. */
        RETRY
    }

    private static Instant earliest(Instant a, Instant b) {
        return a == null || b.isBefore(a) ? b : a;
    }

    /**
     * Counts a failed attempt at {@code eventId}; after {@value #MAX_ATTEMPTS} the event is given up on, so one
     * broken detail document cannot hold the cursor back for ever.
     *
     * @return true when the event is given up on (mark it seen)
     */
    private boolean giveUp(FetchContext ctx, String eventId) {
        int attempts = 1;
        try {
            attempts += Integer.parseInt(ctx.state().get(FAIL_PREFIX + eventId).orElse("0"));
        } catch (NumberFormatException e) {
            attempts = 1;
        }
        if (attempts >= MAX_ATTEMPTS) {
            ctx.log().warn("{}: giving up on {} after {} failed attempts", ctx.sourceId(), eventId, attempts);
            return true;
        }
        ctx.state().put(FAIL_PREFIX + eventId, Integer.toString(attempts));
        return false;
    }

    /** {@code url} when it is on the index's own origin, else null (a link elsewhere is never followed). */
    private URI sameOrigin(FetchContext ctx, String url) {
        try {
            URI uri = URI.create(url.strip());
            if (KitUrls.sameOrigin(baseUrl, uri)) {
                return uri;
            }
        } catch (IllegalArgumentException e) {
            // logged below
        }
        ctx.log().warn("{}: refusing a link off {}: {}", ctx.sourceId(), baseUrl.getHost(), url);
        return null;
    }

    private boolean alreadyProcessed(FetchContext ctx, String eventId, Instant updated) {
        String seen = ctx.state().get(SEEN_PREFIX + eventId).orElse(null);
        if (seen == null) {
            return false;
        }
        try {
            return Long.parseLong(seen) >= updated.toEpochMilli();
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Reads one changed event: its detail document, then its contours.
     *
     * @return {@link Outcome#DONE} when the event is finished (or has nothing to read), {@link Outcome#RETRY} when a
     *     request failed
     * @throws BudgetExceededException when the budget refuses a request; the run stops and the event stays unseen
     */
    private Outcome process(FetchContext ctx, JsonNode feature, String eventId, Instant updated,
            List<NormalizedRecord> records) throws BudgetExceededException {
        JsonNode props = feature.get("properties");
        String detailUrl = text(props, "detail");
        URI detailUri = detailUrl == null ? null : sameOrigin(ctx, detailUrl);
        if (detailUri == null) {
            return Outcome.DONE;
        }
        JsonNode detail;
        try {
            UpstreamResponse response = ctx.http().get(detailUri);
            if (response.status() != 200) {
                return Outcome.RETRY;
            }
            detail = JSON.readTree(response.body());
        } catch (BudgetExceededException e) {
            throw e;
        } catch (AdapterException e) {
            int status = e.httpStatus().orElse(0);
            if (status == 404 || status == 410) {
                return Outcome.DONE;
            }
            ctx.log().info("{}: the detail of {} failed ({}); retried next run", ctx.sourceId(), eventId,
                    e.getMessage());
            return Outcome.RETRY;
        } catch (RuntimeException e) {
            ctx.log().info("{}: the detail of {} did not parse; retried next run", ctx.sourceId(), eventId);
            return Outcome.RETRY;
        }
        JsonNode products = detail.get("properties") == null ? null : detail.get("properties").get("products");
        if (products == null) {
            return Outcome.DONE;
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
        return contours(ctx, shakemap, eventId, updated, origin, records);
    }

    /**
     * The MMI contours of one event, and tombstones for the levels a newer version dropped. A contour file that
     * cannot be read leaves the previous levels alone and asks for a retry (the impact record is re-emitted
     * unchanged, which the ingest hash drops).
     */
    private Outcome contours(FetchContext ctx, JsonNode shakemap, String eventId, Instant updated, Instant origin,
            List<NormalizedRecord> records) throws BudgetExceededException {
        Set<String> levels = new TreeSet<>();
        String contourUrl = null;
        if (shakemap != null && shakemap.get("contents") != null) {
            JsonNode content = shakemap.get("contents").get("download/cont_mmi.json");
            if (content != null) {
                contourUrl = text(content, "url");
            }
        }
        URI contourUri = contourUrl == null ? null : sameOrigin(ctx, contourUrl);
        if (contourUrl != null && contourUri == null) {
            return Outcome.DONE;
        }
        if (contourUri != null) {
            List<NormalizedRecord> read = new ArrayList<>();
            try {
                UpstreamResponse response = ctx.http().get(contourUri);
                if (response.status() != 200) {
                    return Outcome.RETRY;
                }
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
                        read.add(mmi.build());
                    }
                }
            } catch (BudgetExceededException e) {
                throw e;
            } catch (AdapterException | RuntimeException e) {
                ctx.log().info("{}: the contours of {} could not be read ({}); retried next run", ctx.sourceId(),
                        eventId, e.getMessage());
                return Outcome.RETRY;
            }
            records.addAll(read);
        }
        tombstoneGone(ctx, eventId, updated, levels, records);
        return Outcome.DONE;
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
