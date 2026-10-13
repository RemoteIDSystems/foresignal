package ai.foresignal.source.adapters.hydro.ea;

import ai.foresignal.source.adapters.env.EnvSources;
import ai.foresignal.source.adapters.hydro.HydroSources;
import ai.foresignal.source.kit.feature.FeatureGeometry;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
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
import ai.foresignal.source.spi.SourceHttp;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * {@code ea-flood-warnings} (prompt 079, optional): the Environment Agency's flood alerts and warnings in force in
 * England, with their flood-area polygons ({@link EaFloodParser}).
 *
 * <ul>
 *   <li><strong>Each run:</strong> a conditional GET of {@value #FLOODS_URL} (state key {@value #STATE_KEY}); a 304
 *       is {@link FetchResult#unchanged()};
 *   <li><strong>Polygons:</strong> {@code …/flood-monitoring/id/floodAreas/{id}/polygon} once per area, built from
 *       the area id (never from a URL in the data), at most {@value #NEW_POLYGONS_PER_RUN} per run, kept in state
 *       ({@value #POLYGON_PREFIX}{@code {id}}, base64 WKB, gzipped when large) for {@value #POLYGON_DAYS} days; a
 *       polygon not used for that long is dropped;
 *   <li><strong>Retirement:</strong> severity level 4 ("no longer in force") is a tombstone, and so is a warning
 *       that disappears from the list (state {@value #ACTIVE_KEY}).
 * </ul>
 */
@Component
public class EaFloodWarningsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "ea-flood-warnings";

    /** The current warnings. */
    public static final String FLOODS_URL = "https://environment.data.gov.uk/flood-monitoring/id/floods";

    /** The polygon of an area, with {@code {id}}. */
    public static final String POLYGON_URL = "https://environment.data.gov.uk/flood-monitoring/id/floodAreas/{id}/polygon";

    /** The conditional GET state key. */
    public static final String STATE_KEY = "ea-floods";

    /** The state key of the warnings in force, comma-separated area ids. */
    public static final String ACTIVE_KEY = "ea:active";

    /** The state key prefix of an area polygon. */
    public static final String POLYGON_PREFIX = "ea:polygon:";

    /** The state key of the polygon index, {@code id=epochSeconds} comma-separated. */
    public static final String POLYGON_INDEX_KEY = "ea:polygonIndex";

    /** Polygons fetched per run. */
    public static final int NEW_POLYGONS_PER_RUN = 30;

    /** How long a polygon is kept, days. */
    public static final int POLYGON_DAYS = 30;

    /** The required attribution. */
    public static final String ATTRIBUTION = "This uses Environment Agency flood and river level data from the"
            + " real-time data API (Beta)";

    private final URI floods;
    private final String polygonUrl;
    private final Supplier<MeterRegistry> meters;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value HydroSources#BASE_URL_PROPERTY}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public EaFloodWarningsAdapter(@Value("${" + HydroSources.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, HydroSources.meters(meters));
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public EaFloodWarningsAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.floods = HydroSources.uri(FLOODS_URL, baseUrlOverride);
        this.polygonUrl = HydroSources.uri(POLYGON_URL.replace("{id}", "AREA"), baseUrlOverride).toString();
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The polygon URL of {@code areaId}. */
    public URI polygonUri(String areaId) {
        return URI.create(polygonUrl.replace("AREA", ai.foresignal.source.kit.KitUrls.encode(areaId)));
    }

    /**
     * Reads the warnings conditionally, fetches missing polygons, and maps them.
     *
     * @throws AdapterException when the warnings request fails
     * @throws UpstreamFormatException when the warnings do not parse (their validators are forgotten)
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().getConditional(floods, STATE_KEY);
        if (response.notModified()) {
            return FetchResult.unchanged();
        }
        List<JsonNode> items;
        try {
            items = EaFloodParser.items(EnvSources.body(ctx, response, STATE_KEY, floods));
        } catch (UpstreamFormatException e) {
            SourceHttp.forgetValidators(ctx.state(), STATE_KEY);
            throw e;
        }
        AdapterStateStore state = ctx.state();
        Instant now = ctx.now();
        Map<String, Long> index = readIndex(state);
        int fetched = 0;
        List<NormalizedRecord> records = new ArrayList<>();
        Set<String> active = new LinkedHashSet<>();
        Set<String> seen = new LinkedHashSet<>();
        Map<String, Integer> skipped = new TreeMap<>();
        for (JsonNode item : items) {
            String id = EaFloodParser.areaId(item);
            Integer level = EaFloodParser.severityLevel(item);
            Geometry polygon = null;
            if (id != null && (level == null || level != EaFloodParser.NO_LONGER_IN_FORCE)) {
                polygon = cachedPolygon(state, index, id, now);
                if (polygon == null && fetched < NEW_POLYGONS_PER_RUN) {
                    fetched++;
                    polygon = fetchPolygon(ctx, index, id);
                }
            }
            MappedRecord mapped = EaFloodParser.item(item, polygon);
            if (!mapped.accepted()) {
                skipped.merge(mapped.reason(), 1, Integer::sum);
                continue;
            }
            if (!seen.add(mapped.record().externalId())) {
                skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
                continue;
            }
            records.add(mapped.record());
            if (!mapped.record().tombstone()) {
                active.add(mapped.record().externalId());
            }
        }
        for (String previous : state.get(ACTIVE_KEY).orElse("").split(",")) {
            if (!previous.isBlank() && !seen.contains(previous.strip())) {
                records.add(NormalizedRecord.tombstone(previous.strip(), EaFloodParser.KIND, now));
            }
        }
        state.put(ACTIVE_KEY, String.join(",", active));
        prune(state, index, active, now);
        SkipCounter.count(meters, HydroSources.SKIPPED_METRIC, HydroSources.SKIPPED_DESCRIPTION, ctx.sourceId(),
                skipped);
        ctx.log().debug("{}: {} warning(s) in force, {} polygon(s) fetched, skipped {}", ID, active.size(), fetched,
                skipped);
        return FetchResult.complete(records);
    }

    private Geometry cachedPolygon(AdapterStateStore state, Map<String, Long> index, String id, Instant now) {
        Long at = index.get(id);
        if (at == null || Instant.ofEpochSecond(at).plus(Duration.ofDays(POLYGON_DAYS)).isBefore(now)) {
            return null;
        }
        return EaFloodParser.decode(EnvSources.unpack(state.get(POLYGON_PREFIX + id).orElse(null)));
    }

    private Geometry fetchPolygon(FetchContext ctx, Map<String, Long> index, String id) {
        URI uri = polygonUri(id);
        try {
            FeatureGeometry.Result result = EaFloodParser.polygon(EnvSources.body(ctx, ctx.http().get(uri), null, uri));
            ctx.state().put(POLYGON_PREFIX + id, EnvSources.pack(EaFloodParser.encode(result.geometry())));
            index.put(id, ctx.now().getEpochSecond());
            writeIndex(ctx.state(), index);
            return result.geometry();
        } catch (AdapterException | UpstreamFormatException e) {
            ctx.log().warn("{}: the polygon of {} could not be read ({}); the warning is stored without one", ID, id,
                    e.getMessage());
            return null;
        }
    }

    /** Drops polygons older than {@value #POLYGON_DAYS} days whose area is not in force. */
    private static void prune(AdapterStateStore state, Map<String, Long> index, Set<String> active, Instant now) {
        boolean changed = false;
        for (String id : new ArrayList<>(index.keySet())) {
            boolean old = Instant.ofEpochSecond(index.get(id)).plus(Duration.ofDays(POLYGON_DAYS)).isBefore(now);
            if (old && !active.contains(id)) {
                state.remove(POLYGON_PREFIX + id);
                index.remove(id);
                changed = true;
            }
        }
        if (changed) {
            writeIndex(state, index);
        }
    }

    static Map<String, Long> readIndex(AdapterStateStore state) {
        Map<String, Long> index = new LinkedHashMap<>();
        for (String entry : state.get(POLYGON_INDEX_KEY).orElse("").split(",")) {
            int eq = entry.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            try {
                index.put(entry.substring(0, eq).strip(), Long.parseLong(entry.substring(eq + 1).strip()));
            } catch (NumberFormatException e) {
                // a damaged entry is dropped; its polygon is fetched again
            }
        }
        return index;
    }

    static void writeIndex(AdapterStateStore state, Map<String, Long> index) {
        List<String> parts = new ArrayList<>();
        index.forEach((id, at) -> parts.add(id + "=" + at));
        state.put(POLYGON_INDEX_KEY, String.join(",", parts));
    }

    private static SourceDescriptor buildDescriptor() {
        String kind = EaFloodParser.KIND;
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Environment Agency flood warnings")
                .description("Flood alerts and warnings in force in England from the Environment Agency real-time"
                        + " flood-monitoring API (Beta), with each flood area's polygon (fetched once and kept 30"
                        + " days). Severity level 1 is a severe flood warning, 2 a flood warning, 3 a flood alert;"
                        + " a warning no longer in force is retired. The message text is the agency's and is stored"
                        + " as untrusted text.")
                .kind(kind)
                .homepage("https://environment.data.gov.uk/flood-monitoring/doc/reference")
                .attribution(ATTRIBUTION)
                .licence("OGL v3 (Open Government Licence)")
                .termsUrl("https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(40, 2000))
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(IngestPolicy.all())
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .defaultRetention(HydroSources.EVENT_RETENTION)
                .kindRetention(kind, HydroSources.EVENT_RETENTION)
                .zeroResultsNormal(true)
                .tileProperties("severity", "severityLevel", "description")
                .searchFields("description", "eaAreaName", "message")
                .embedFields("description", "message")
                .titleField("description")
                .rankField("severityLevel");
        HydroSources.text(b, "severity", "Severity", "Severe flood warning, Flood warning or Flood alert");
        HydroSources.integer(b, "severityLevel", "Severity level", "1 severe flood warning, 2 flood warning, 3 flood"
                + " alert (4, no longer in force, is retired)");
        HydroSources.text(b, "description", "Area", "The flood area's description");
        HydroSources.text(b, "eaAreaName", "EA area", "The Environment Agency area");
        HydroSources.text(b, "message", "Message", "The warning message (untrusted text, at most 4 KB)");
        HydroSources.bool(b, "isTidal", "Tidal", "True for a tidal flood area");
        b.kindDisplay(kind, new KindDisplay("{severity}: {description}", "warning", "#C92A2A", null));
        return b.build();
    }
}
