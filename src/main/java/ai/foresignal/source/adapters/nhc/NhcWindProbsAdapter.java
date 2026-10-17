package ai.foresignal.source.adapters.nhc;

import ai.foresignal.source.kit.KitUrls;
import ai.foresignal.source.kit.feature.ArcGisQueryClient;
import ai.foresignal.source.kit.feature.FeatureFetch;
import ai.foresignal.source.kit.feature.FeatureGeometry;
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
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import ai.foresignal.source.support.AbsenceTombstoner;
import ai.foresignal.source.support.MappedRecord;
import ai.foresignal.source.support.ScopedFetchContext;
import ai.foresignal.source.support.SkipCounter;
import io.micrometer.core.instrument.MeterRegistry;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * NHC cumulative wind-speed probabilities (prompt 127), source {@code nhc-wind-probs}: the 34-, 50- and 64-knot
 * probability bands of NOAA's {@code NHC_tropical_weather_summary} MapServer, read with the prompt 073
 * {@link ArcGisQueryClient}. These are the published probabilities the impact projector uses for the
 * {@code tropical-wind-34}, {@code -50} and {@code -64} hazards.
 *
 * <p><strong>Each run:</strong>
 * <ol>
 *   <li>the layer ids are resolved by name ("Probabilistic Winds 34 kts", "50 kts", "64 kts"; 30, 31 and 32 on
 *       2026-10-17), at most once per {@link NhcLayerResolver#CACHE_TTL}; a missing name is an
 *       {@link UpstreamFormatException};
 *   <li>each layer is queried ({@code where=1=1&outFields=*&returnGeometry=true&outSR=4326&f=geojson}) with its
 *       own client state ({@link ScopedFetchContext}, scope {@code layer-<kt>}) and mapped by
 *       {@link NhcWindProbMapper};
 *   <li>per layer, a band missing from two consecutive complete fetches is tombstoned ({@link AbsenceTombstoner},
 *       scope {@code wind-<kt>}); out of season every layer is empty ({@code zeroResultsNormal}).
 * </ol>
 * A new advisory changes the bands' content and yields new versions of the same ids.
 *
 * <p><strong>Coverage:</strong> the Atlantic, eastern and central Pacific (NHC and CPHC). Analytic class
 * {@code event}, retention 1825 days.
 */
@Component
public class NhcWindProbsAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "nhc-wind-probs";

    /** The production MapServer (the same service as {@code nhc-cyclone-cones}). */
    public static final String SERVICE_URL = NhcCycloneConesAdapter.SERVICE_URL;

    /** The counter of features the mapper skipped. */
    public static final String SKIPPED_METRIC = "foresignal.nhc.windprobs.skipped";

    /** The state key of the resolved layer ids ({@code 34=30,50=31,64=32}). */
    public static final String LAYERS_KEY = "nhcwsp:layers";

    /** The state key of when they were resolved (ISO-8601). */
    public static final String LAYERS_AT_KEY = "nhcwsp:layersAt";

    /** The thresholds, in knots, in layer order. */
    public static final List<Integer> THRESHOLDS = List.of(34, 50, 64);

    /** Raw retention: the {@code event} analytic class (Conventions), 1825 days. */
    static final Duration RETENTION = Duration.ofDays(1825);

    /** The response cap of one query. */
    static final int MAX_RESPONSE_BYTES = 32 * 1024 * 1024;

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final URI service;
    private final Supplier<MeterRegistry> meters;
    private final ArcGisQueryClient client = new ArcGisQueryClient();
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the adapter.
     *
     * @param baseUrlOverride the value of {@value NhcCyclonesAdapter#BASE_URL_PROPERTY}, under which the service
     *     moves as {@code {base}/{host}{path}}; blank in production
     * @param meters the registry of the skip counter, resolved lazily
     */
    @Autowired
    public NhcWindProbsAdapter(@Value("${" + NhcCyclonesAdapter.BASE_URL_PROPERTY + ":}") String baseUrlOverride,
            ObjectProvider<MeterRegistry> meters) {
        this(baseUrlOverride, meters == null ? () -> null : (Supplier<MeterRegistry>) meters::getIfAvailable);
    }

    /**
     * Creates the adapter with a plain supplier (tests).
     *
     * @param baseUrlOverride the test base URL, or blank
     * @param meters supplies the meter registry, or null
     */
    public NhcWindProbsAdapter(String baseUrlOverride, Supplier<MeterRegistry> meters) {
        this.service = URI.create(KitUrls.rebased(SERVICE_URL, baseUrlOverride));
        this.meters = meters;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The MapServer URL in use. */
    public URI serviceUrl() {
        return service;
    }

    /**
     * Fetches the three layers once (see the class note).
     *
     * @throws AdapterException when a request fails
     * @throws UpstreamFormatException when a layer name is missing or a response is not a usable document
     */
    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        Map<Integer, Integer> ids = layers(ctx);
        List<NormalizedRecord> out = new ArrayList<>();
        Map<String, Integer> skipped = new TreeMap<>();
        boolean complete = true;
        for (int kt : THRESHOLDS) {
            URI layerUrl = URI.create(service.toString().replaceAll("/+$", "") + "/" + ids.get(kt));
            FeatureFetch fetch = client.fetch(new ScopedFetchContext(ctx, "layer-" + kt),
                    new ArcGisQueryClient.Request(layerUrl, "1=1", List.of(), 2000, 2, 4000, null, null, false,
                            Map.of(), Map.of()));
            List<NormalizedRecord> records = new ArrayList<>();
            Set<String> seen = new HashSet<>();
            for (MappedRecord mapped : NhcWindProbMapper.map(kt, fetch.features())) {
                if (!mapped.accepted()) {
                    skipped.merge(mapped.reason(), 1, Integer::sum);
                } else if (seen.add(mapped.record().externalId())) {
                    records.add(mapped.record());
                } else {
                    skipped.merge(MappedRecord.DUPLICATE_ID, 1, Integer::sum);
                }
            }
            out.addAll(records);
            if (!fetch.complete()) {
                complete = false;
                ctx.log().warn("{}: the {}-kt layer stopped early ({}); tombstoning nothing for it", ctx.sourceId(),
                        kt, fetch.stopReason());
                continue;
            }
            List<String> gone = AbsenceTombstoner.update(ctx.state(), "wind-" + kt,
                    AbsenceTombstoner.Outcome.COMPLETE, records.stream().map(NormalizedRecord::externalId).toList());
            out.addAll(AbsenceTombstoner.tombstones(gone, NhcWindProbMapper.KIND, ctx.now()));
        }
        SkipCounter.count(meters, SKIPPED_METRIC, "NHC wind-probability features the mapper skipped",
                ctx.sourceId(), skipped);
        return complete ? FetchResult.complete(out) : FetchResult.of(out);
    }

    /** The layer ids by threshold, from the cache while fresh, else from the service document. */
    private Map<Integer, Integer> layers(FetchContext ctx) throws AdapterException {
        Map<Integer, Integer> cached = cached(ctx.state(), ctx.now());
        if (cached != null) {
            return cached;
        }
        UpstreamResponse response = ctx.http().get(URI.create(service.toString().replaceAll("/+$", "") + "?f=json"));
        if (response.status() != 200 || response.truncated()) {
            throw new UpstreamFormatException("NHC service document answered HTTP " + response.status()
                    + (response.truncated() ? " (truncated)" : ""), "");
        }
        Map<Integer, Integer> ids = parseLayers(response.body());
        StringBuilder sb = new StringBuilder();
        ids.forEach((kt, id) -> sb.append(sb.isEmpty() ? "" : ",").append(kt).append('=').append(id));
        ctx.state().put(LAYERS_KEY, sb.toString());
        ctx.state().put(LAYERS_AT_KEY, ctx.now().toString());
        return ids;
    }

    /** The layer name of a threshold in the service document. */
    public static String layerName(int thresholdKt) {
        return "Probabilistic Winds " + thresholdKt + " kts";
    }

    /**
     * The ids of the three probability layers in a service document.
     *
     * @param body the {@code ?f=json} document
     * @throws UpstreamFormatException when it is not JSON, has no {@code layers} array or lacks a layer name
     */
    public static Map<Integer, Integer> parseLayers(byte[] body) {
        JsonNode root;
        try {
            root = JSON.readTree(body);
        } catch (JacksonException e) {
            throw new UpstreamFormatException("NHC service document is not JSON: " + e.getOriginalMessage(), "", e);
        }
        JsonNode layers = root == null ? null : root.get("layers");
        if (layers == null || !layers.isArray()) {
            throw new UpstreamFormatException("NHC service document has no layers array", "");
        }
        Map<Integer, Integer> ids = new LinkedHashMap<>();
        for (int kt : THRESHOLDS) {
            for (JsonNode l : layers) {
                if (l.has("name") && layerName(kt).equals(l.get("name").asString().strip()) && l.has("id")
                        && l.get("id").isNumber()) {
                    ids.put(kt, l.get("id").intValue());
                    break;
                }
            }
            if (!ids.containsKey(kt)) {
                throw new UpstreamFormatException("NHC service has no layer named '" + layerName(kt) + "'", "");
            }
        }
        return ids;
    }

    static Map<Integer, Integer> cached(AdapterStateStore state, Instant now) {
        String text = state.get(LAYERS_KEY).orElse(null);
        String at = state.get(LAYERS_AT_KEY).orElse(null);
        if (text == null || at == null) {
            return null;
        }
        try {
            if (Instant.parse(at).plus(NhcLayerResolver.CACHE_TTL).isBefore(now)) {
                return null;
            }
            Map<Integer, Integer> ids = new LinkedHashMap<>();
            for (String pair : text.split(",")) {
                String[] kv = pair.split("=", 2);
                ids.put(Integer.parseInt(kv[0]), Integer.parseInt(kv[1]));
            }
            return ids.keySet().containsAll(THRESHOLDS) ? ids : null;
        } catch (DateTimeParseException | NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("NHC wind-speed probabilities")
                .description("National Hurricane Center cumulative wind-speed probabilities for every active storm in"
                        + " the Atlantic, eastern and central Pacific (NOAA NHC_tropical_weather_summary MapServer,"
                        + " layers Probabilistic Winds 34, 50 and 64 kts): one polygon per published probability band"
                        + " (<5%, 5-10% … >90%) that sustained winds of at least that speed occur within 120 hours of"
                        + " the advisory. These are published probabilities; the forecast cone is not. A band is"
                        + " tombstoned once missing from two consecutive complete fetches; out of season every layer"
                        + " is empty.")
                .kinds(NhcWindProbMapper.KIND)
                .homepage("https://www.nhc.noaa.gov/gis/")
                .attribution("NOAA National Hurricane Center and Central Pacific Hurricane Center")
                .licence("US-PD")
                .termsUrl("https://www.weather.gov/disclaimer")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(5), Duration.ofMinutes(15))
                .defaultBudget(Budget.of(10, 1200))
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .fetchTimeout(Duration.ofSeconds(60))
                .followRedirects(false)
                .historyTier(HistoryTier.STANDARD)
                .defaultIngestPolicy(new IngestPolicy.All(null))
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .ttlAnchor(TtlAnchor.RECEIVED)
                .defaultRetention(RETENTION)
                .kindRetention(NhcWindProbMapper.KIND, RETENTION)
                .zeroResultsNormal(true)
                .titleField("band")
                .searchFields("band")
                .tileProperties("thresholdKt", "band", "pMin", "pMax")
                .kindDisplay(NhcWindProbMapper.KIND, new KindDisplay("{thresholdKt} kt winds {band}", "storm",
                        "#AE3EC9", null));
        b.fieldSpec("thresholdKt", new FieldSpec(FieldType.INTEGER, "[kn_i]", "Threshold", "The sustained wind"
                + " threshold of the layer: 34, 50 or 64 knots", null, Set.of()));
        b.fieldSpec("band", new FieldSpec(FieldType.STRING, null, "Band", "The probability band as published"
                + " (percentage), for example 40-50%", null, Set.of()));
        b.fieldSpec("pMin", new FieldSpec(FieldType.NUMBER, null, "Lower bound", "The band's lower bound as a"
                + " probability (0 to 1)", null, Set.of()));
        b.fieldSpec("pMax", new FieldSpec(FieldType.NUMBER, null, "Upper bound", "The band's upper bound as a"
                + " probability (0 to 1; 1 for >90%)", null, Set.of()));
        b.fieldSpec("issuedAt", new FieldSpec(FieldType.TIMESTAMP, null, "Issued", "The advisory product time"
                + " (idp_filedate)", null, Set.of()));
        b.fieldSpec("validUntil", new FieldSpec(FieldType.TIMESTAMP, null, "Valid until", "The end of the"
                + " 120-hour cumulative period", null, Set.of()));
        b.fieldSpec("horizonHours", new FieldSpec(FieldType.INTEGER, "h", "Horizon", "The cumulative period in"
                + " hours (120)", null, Set.of()));
        b.fieldSpec(FeatureGeometry.SIMPLIFIED_KEY, new FieldSpec(FieldType.BOOLEAN, null, "Geometry simplified",
                "Present and true when the geometry was simplified to fit the 10,000-vertex limit", null, Set.of()));
        b.fieldSpec(FeatureGeometry.TOLERANCE_KEY, new FieldSpec(FieldType.NUMBER, "deg", "Simplification tolerance",
                "The tolerance the geometry was simplified with", null, Set.of()));
        return b.build();
    }
}
