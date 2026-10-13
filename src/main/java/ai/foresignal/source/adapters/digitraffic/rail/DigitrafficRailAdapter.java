package ai.foresignal.source.adapters.digitraffic.rail;

import ai.foresignal.config.ForesignalVersion;
import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Egress;
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
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.TtlAnchor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.spi.UpstreamResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
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
import tools.jackson.core.type.TypeReference;

/**
 * {@code digitraffic-rail} (prompt 080): the newest GPS position of every running train in Finland, from Fintraffic's
 * Digitraffic rail API, as {@code train} records ({@link DigitrafficRailParser}).
 *
 * <p>One {@code GET /api/v1/train-locations/latest} per poll (7.5 KB gzip in the prompt's check, 6.3 KB decoded at night on 2026-10-13; {@code Cache-Control: max-age=1}),
 * with the header {@code Digitraffic-User: Foresignal/<version>} that Digitraffic asks clients to send; gzip is
 * requested by the upstream client. Interval 30 s (minimum 15 s), budget 4/min and 4,000/day.
 *
 * <p><strong>Enrichment</strong> (parameter {@value #ENRICH_PARAM}, default true): once per train per departure day,
 * {@code GET /api/v1/trains/{departureDate}/{trainNumber}} adds {@code trainType, trainCategory, operatorShortCode,
 * commuterLineID}. The answers are cached in adapter state under {@value #CACHE_KEY} (an empty answer is cached
 * too, so it is not asked again) and dropped once the departure date is more than a day old. At most
 * {@value #MAX_LOOKUPS_PER_DAY} lookups per UTC day ({@value #LOOKUPS_KEY}) and {@value #MAX_LOOKUPS_PER_RUN} per run,
 * so a run spends at most two requests and two runs a minute stay inside the 4/min budget. A failed lookup never
 * fails the run: the positions are the product.
 *
 * <p>Policy (Core-Data-Model §5.3 "Transit and rail"): TRACK, TTL 5 min OBSERVED, SAMPLED {@code {15s, 240s,
 * 150 m, thresholds, edgeMin 8s}}; {@code 240 ≤ 300 − 30 − 30}. Digitraffic publishes no heading, so there is no
 * bearing threshold and no motion spec ({@code MotionSpec} requires a course field): the sampler measures the plain
 * distance from the last persisted position. License CC BY 4.0, attribution "Fintraffic / digitraffic.fi".
 *
 * <p>A later streaming option: the MQTT feed {@code rata-mqtt.digitraffic.fi}, topic {@code train-locations/#}.
 */
@Component
public class DigitrafficRailAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "digitraffic-rail";

    /** Test-only override of the origin (Adapter Guide §8); the request paths are kept. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.digitraffic-rail.base-url";

    /** The production origin. */
    public static final String DEFAULT_ORIGIN = "https://rata.digitraffic.fi";

    /** The positions path. */
    static final String LATEST_PATH = "/api/v1/train-locations/latest";

    /** The train lookup path prefix; {@code {departureDate}/{trainNumber}} follows. */
    static final String TRAIN_PATH = "/api/v1/trains/";

    /** The enrichment parameter. */
    public static final String ENRICH_PARAM = "enrich";

    /** The adapter state key of the lookup cache. */
    public static final String CACHE_KEY = "rail:trains";

    /** The adapter state key of the daily lookup count, {@code YYYY-MM-DD:count} (UTC day). */
    public static final String LOOKUPS_KEY = "rail:lookups";

    /** Lookups per UTC day. */
    public static final int MAX_LOOKUPS_PER_DAY = 200;

    /** Lookups per run. */
    public static final int MAX_LOOKUPS_PER_RUN = 1;

    /** The record TTL. */
    public static final Duration TTL = Duration.ofMinutes(5);

    /** The significant fields (§5.3 "Transit and rail", prompt 080). */
    public static final List<String> SIGNIFICANT = List.of("trainNumber", "departureDate", "trainType",
            "operatorShortCode");

    private static final TypeReference<Map<String, List<String>>> CACHE_TYPE = new TypeReference<>() {};

    private final String origin;
    private final Supplier<String> version;
    private final SourceDescriptor descriptor = buildDescriptor();

    /**
     * Creates the source.
     *
     * @param originOverride the value of {@value #BASE_URL_PROPERTY}; blank in production
     * @param version Foresignal's version, for the {@code Digitraffic-User} header
     */
    @Autowired
    public DigitrafficRailAdapter(@Value("${" + BASE_URL_PROPERTY + ":}") String originOverride,
            ObjectProvider<ForesignalVersion> version) {
        this(originOverride, () -> {
            ForesignalVersion v = version == null ? null : version.getIfAvailable();
            return v == null ? ForesignalVersion.FALLBACK : v.version();
        });
    }

    /**
     * Creates the source with a plain version supplier (tests).
     *
     * @param originOverride the test origin, or blank
     * @param version supplies Foresignal's version, or null for {@code dev}
     */
    public DigitrafficRailAdapter(String originOverride, Supplier<String> version) {
        String chosen = originOverride == null || originOverride.isBlank() ? DEFAULT_ORIGIN : originOverride.trim();
        this.origin = chosen.endsWith("/") ? chosen.substring(0, chosen.length() - 1) : chosen;
        this.version = version == null ? () -> ForesignalVersion.FALLBACK : version;
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    /** The positions URL. */
    public URI latestUri() {
        return URI.create(origin + LATEST_PATH);
    }

    /** The lookup URL of one train. */
    public URI trainUri(String departureDate, int trainNumber) {
        return URI.create(origin + TRAIN_PATH + departureDate + "/" + trainNumber);
    }

    /** The headers of every request. */
    Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("Digitraffic-User", "Foresignal/" + version.get());
        headers.put("Accept", "application/json");
        return headers;
    }

    /**
     * Fetches the positions, then looks up at most one train without cached details (see the class note).
     *
     * @return every train with a usable fix, as a complete result
     * @throws AdapterException when the positions request fails
     * @throws UpstreamFormatException when the positions body is truncated or not a JSON array
     */
    @Override
    public FetchResult fetch(FetchContext ctx) throws AdapterException {
        UpstreamResponse response = ctx.http().get(latestUri(), headers());
        if (response.truncated()) {
            throw new UpstreamFormatException("Digitraffic train-locations exceeded maxResponseBytes", null);
        }
        DigitrafficRailParser.Locations locations = DigitrafficRailParser.parseLocations(response.body());
        if (locations.skipped() > 0) {
            ctx.log().debug("{}: skipped {} entries without a usable position", ctx.sourceId(), locations.skipped());
        }
        Map<String, DigitrafficRailParser.Enrichment> cache = ctx.params().bool(ENRICH_PARAM, true)
                ? enrich(ctx, locations.positions())
                : Map.of();
        List<NormalizedRecord> records = new ArrayList<>(locations.positions().size());
        for (DigitrafficRailParser.Position position : locations.positions()) {
            records.add(DigitrafficRailParser.record(position, cache.get(position.externalId())));
        }
        return FetchResult.complete(records);
    }

    /** Loads the cache, drops stale entries, makes this run's lookups and stores the cache. */
    private Map<String, DigitrafficRailParser.Enrichment> enrich(FetchContext ctx,
            List<DigitrafficRailParser.Position> positions) {
        AdapterStateStore state = ctx.state();
        LocalDate today = LocalDate.ofInstant(ctx.clock().instant(), ZoneOffset.UTC);
        Map<String, DigitrafficRailParser.Enrichment> cache = readCache(state.get(CACHE_KEY).orElse(null));
        boolean changed = cache.keySet().removeIf(id -> isStale(id, today));

        int used = lookupsToday(state.get(LOOKUPS_KEY).orElse(null), today);
        int thisRun = 0;
        List<DigitrafficRailParser.Position> pending = positions.stream()
                .filter(p -> !cache.containsKey(p.externalId()))
                .sorted((a, b) -> a.externalId().compareTo(b.externalId()))
                .toList();
        for (DigitrafficRailParser.Position position : pending) {
            if (thisRun >= MAX_LOOKUPS_PER_RUN || used >= MAX_LOOKUPS_PER_DAY) {
                break;
            }
            thisRun++;
            used++;
            state.put(LOOKUPS_KEY, today + ":" + used);
            try {
                UpstreamResponse answer = ctx.http().get(trainUri(position.departureDate(), position.trainNumber()),
                        headers());
                cache.put(position.externalId(), DigitrafficRailParser.parseTrain(answer.body()));
                changed = true;
            } catch (AdapterException | RuntimeException e) {
                ctx.log().info("{}: lookup of train {} failed; positions are kept without details: {}",
                        ctx.sourceId(), position.externalId(), e.getMessage());
            }
        }
        if (changed) {
            state.put(CACHE_KEY, writeCache(cache));
        }
        return cache;
    }

    /** True when the entry's departure date is more than a day before {@code today}, or unreadable. */
    static boolean isStale(String externalId, LocalDate today) {
        int colon = externalId.indexOf(':');
        if (colon <= 0) {
            return true;
        }
        try {
            return LocalDate.parse(externalId.substring(0, colon)).isBefore(today.minusDays(1));
        } catch (java.time.format.DateTimeParseException e) {
            return true;
        }
    }

    /** The lookups already made on {@code today}, from {@code YYYY-MM-DD:count}. */
    static int lookupsToday(String stored, LocalDate today) {
        if (stored == null) {
            return 0;
        }
        String prefix = today + ":";
        if (!stored.startsWith(prefix)) {
            return 0;
        }
        try {
            return Math.max(0, Integer.parseInt(stored.substring(prefix.length())));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** The cache from its stored JSON ({@code {id: [trainType, trainCategory, operatorShortCode, commuterLineID]}}). */
    static Map<String, DigitrafficRailParser.Enrichment> readCache(String stored) {
        Map<String, DigitrafficRailParser.Enrichment> cache = new TreeMap<>();
        if (stored == null || stored.isBlank()) {
            return cache;
        }
        try {
            Map<String, List<String>> raw = DigitrafficRailParser.JSON.readValue(stored, CACHE_TYPE);
            raw.forEach((id, values) -> {
                List<String> v = values == null ? List.of() : values;
                cache.put(id, new DigitrafficRailParser.Enrichment(at(v, 0), at(v, 1), at(v, 2), at(v, 3)));
            });
        } catch (JacksonException e) {
            // A cache that does not parse is rebuilt from scratch.
            cache.clear();
        }
        return cache;
    }

    /** The cache as stored JSON. */
    static String writeCache(Map<String, DigitrafficRailParser.Enrichment> cache) {
        Map<String, List<String>> raw = new TreeMap<>();
        cache.forEach((id, e) -> raw.put(id, Arrays.asList(e.trainType(), e.trainCategory(), e.operatorShortCode(),
                e.commuterLineID())));
        return DigitrafficRailParser.JSON.writeValueAsString(raw);
    }

    private static String at(List<String> values, int index) {
        return index < values.size() ? values.get(index) : null;
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("Digitraffic trains (Finland)")
                .description("The newest GPS position of every running train in Finland, from Fintraffic's Digitraffic"
                        + " rail API (rata.digitraffic.fi /api/v1/train-locations/latest), with train type, category,"
                        + " operator and commuter line looked up once per train and day (parameter enrich). TRACK,"
                        + " TTL 5 min, observedAt = the fix time. Keep the interval at or below 30 s, or shorten the"
                        + " keyframe with it. Digitraffic also publishes the positions over MQTT"
                        + " (rata-mqtt.digitraffic.fi, train-locations/#), a later streaming option.")
                .kind(DigitrafficRailParser.KIND)
                .mode(Mode.POLLING)
                .homepage("https://www.digitraffic.fi/en/railway-traffic/")
                .attribution("Fintraffic / digitraffic.fi")
                .licence("CC BY 4.0")
                .termsUrl("https://www.digitraffic.fi/en/terms-of-service/")
                .egress(Egress.OPEN)
                .interval(Duration.ofSeconds(15), Duration.ofSeconds(30))
                .defaultBudget(4, 4_000)
                .historyTier(HistoryTier.TRACK)
                .ttlAnchor(TtlAnchor.OBSERVED)
                .observedAtMode(ObservedAtMode.UPSTREAM)
                .recordTtl(TTL)
                .defaultIngestPolicy(new IngestPolicy.Sampled(Duration.ofSeconds(15), Duration.ofSeconds(240), 150.0,
                        true, Duration.ofSeconds(8)))
                .significantFields(SIGNIFICANT.toArray(String[]::new))
                .volatileFields("accuracyM")
                .titleField("trainNumber")
                .tileProperties("trainNumber", "trainType", "commuterLineID", "speedKph")
                .searchFields("trainNumber", "trainType", "commuterLineID", "operatorShortCode")
                .param(ParamSpec.optional(ENRICH_PARAM, ParamType.BOOLEAN, "Look up each train's type, category,"
                        + " operator and commuter line once per departure day (at most " + MAX_LOOKUPS_PER_DAY
                        + " lookups a day)", true))
                .kindDisplay(DigitrafficRailParser.KIND, new KindDisplay("{trainNumber}", "rail", "#0B7285", null));
        b.fieldSpec("trainNumber", new FieldSpec(FieldType.INTEGER, null, "Train number",
                "Train number; unique per departure date", null, Set.of()));
        text(b, "departureDate", "Departure date", "Departure date of the train, YYYY-MM-DD (Finnish time)");
        b.fieldSpec("speedKph", new FieldSpec(FieldType.NUMBER, "km/h", "Speed", "Speed reported with the fix", null,
                Set.of()));
        b.fieldSpec("gps", new FieldSpec(FieldType.BOOLEAN, null, "GPS fix",
                "True when the position comes from the train's GPS (isGpsLocation)", null, Set.of()));
        b.fieldSpec("accuracyM", new FieldSpec(FieldType.INTEGER, "m", "Accuracy",
                "Reported position accuracy in meters (volatile)", null, Set.of()));
        text(b, "trainType", "Train type", "Train type code, for example IC, S, HL or T (from the train lookup)");
        text(b, "trainCategory", "Train category",
                "Train category, for example Long-distance, Commuter or Cargo (from the train lookup)");
        text(b, "operatorShortCode", "Operator", "Operator short code, for example vr (from the train lookup)");
        text(b, "commuterLineID", "Commuter line", "Commuter line letter, for example K (from the train lookup)");
        return b.build();
    }

    private static void text(SourceDescriptor.Builder b, String key, String label, String description) {
        b.fieldSpec(key, new FieldSpec(FieldType.STRING, null, label, description, null, Set.of()));
    }
}
