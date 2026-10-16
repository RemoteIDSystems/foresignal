package ai.foresignal.source.adapters.env.epauv;

import ai.foresignal.source.spi.AdapterException;
import ai.foresignal.source.spi.AdapterStateStore;
import ai.foresignal.source.spi.Budget;
import ai.foresignal.source.spi.FetchContext;
import ai.foresignal.source.spi.FetchResult;
import ai.foresignal.source.spi.HistoryTier;
import ai.foresignal.source.spi.LiveIdSet;
import ai.foresignal.source.spi.Mode;
import ai.foresignal.source.spi.NormalizedRecord;
import ai.foresignal.source.spi.ObservedAtMode;
import ai.foresignal.source.spi.PlacePoints;
import ai.foresignal.source.spi.PollingAdapter;
import ai.foresignal.source.spi.ParamSpec;
import ai.foresignal.source.spi.ParamType;
import ai.foresignal.source.spi.SourceDescriptor;
import ai.foresignal.source.spi.UpstreamFormatException;
import ai.foresignal.source.support.JsonProps;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/**
 * EPA Envirofacts UV hourly forecast (prompt 106; Weather-Imagery-Design §5.3): for each place with an
 * optional {@code attributes.zip}, one {@code uv-forecast} record (externalId {@code place:{id}}) with
 * the hourly UV index in the place's timezone, {@code maxUv} and {@code maxAt}. One request per place
 * per 6 hours; a place without a ZIP is skipped with a health note.
 *
 * <p><strong>Verified 2026-10-15</strong>:
 * {@code data.epa.gov/efservice/getEnvirofactsUVHOURLY/ZIP/78701/JSON} returns
 * {@code [{"ORDER":2,"ZIP":"78701",...,"DATE_TIME":"Oct/16/2026 07 AM","UV_VALUE":0}, …]}.
 */
@Component
public class EpaUvAdapter implements PollingAdapter {

    /** The source id. */
    public static final String ID = "epa-uv";

    /** The record kind. */
    public static final String KIND = "uv-forecast";

    /** The URL pattern. */
    public static final String URL_PATTERN = "https://data.epa.gov/efservice/getEnvirofactsUVHOURLY/ZIP/%s/JSON";

    /** Test-only base URL override. */
    public static final String BASE_URL_PROPERTY = "foresignal.test.adapters.epauv.base-url";

    /** The per-place request spacing. */
    public static final Duration SPACING = Duration.ofHours(6);

    /** The most places per run. */
    public static final int MAX_PLACES_PER_RUN = 4;

    /** The {@code maxPlaces} parameter. */
    public static final String MAX_PLACES_PARAM = "maxPlaces";

    /** The health reason when no place has a ZIP. */
    public static final String NO_ZIPS = "no place has a zip attribute";

    private static final tools.jackson.databind.json.JsonMapper JSON =
            tools.jackson.databind.json.JsonMapper.builder().build();
    private static final Pattern ZIP = Pattern.compile("^\\d{5}$");
    private static final DateTimeFormatter EPA_TIME = DateTimeFormatter.ofPattern("MMM/dd/yyyy hh a", Locale.US);
    private static final int MAX_RESPONSE_BYTES = 2 * 1024 * 1024;

    private final String baseUrl;
    private final Supplier<PlacePoints> places;
    private final SourceDescriptor descriptor = buildDescriptor();

    /** Creates the adapter. */
    @Autowired
    public EpaUvAdapter(ObjectProvider<PlacePoints> places,
            @Value("${" + BASE_URL_PROPERTY + ":}") String baseUrlOverride) {
        this(() -> places == null ? null : places.getIfAvailable(), baseUrlOverride);
    }

    /** Creates the adapter. */
    public EpaUvAdapter(Supplier<PlacePoints> places, String baseUrlOverride) {
        this.places = places == null ? () -> null : places;
        this.baseUrl = baseUrlOverride == null ? "" : baseUrlOverride.trim();
    }

    @Override
    public SourceDescriptor descriptor() {
        return descriptor;
    }

    @Override
    public synchronized FetchResult fetch(FetchContext ctx) throws AdapterException {
        PlacePoints placePoints = places.get();
        List<PlacePoints.Summary> chosen = placePoints == null ? List.of()
                : placePoints.summaries().stream().sorted(java.util.Comparator.comparing(PlacePoints.Summary::id)).toList();
        List<NormalizedRecord> out = new ArrayList<>();
        List<String> live = new ArrayList<>();
        for (String gone : LiveIdSet.absentSince(ctx.state(), KIND, chosen.stream().map(PlacePoints.Summary::id).toList())) {
            out.add(NormalizedRecord.tombstone(gone, KIND, ctx.now()));
        }
        int perRun = ctx.params() == null ? MAX_PLACES_PER_RUN
                : Math.max(1, Math.min(50, ctx.params().integer(MAX_PLACES_PARAM, MAX_PLACES_PER_RUN)));
        int done = 0;
        boolean anyZip = false;
        for (PlacePoints.Summary place : chosen) {
            String zip = zip(placePoints, place.id());
            if (zip == null) {
                continue;
            }
            anyZip = true;
            live.add(place.id());
            if (done >= perRun || !due(ctx.state(), place.id(), ctx.now())) {
                continue;
            }
            try {
                NormalizedRecord record = fetchPlace(ctx, placePoints, place, zip);
                if (record != null) {
                    out.add(record);
                }
                ctx.state().put(nextKey(place.id()), ctx.now().plus(SPACING).toString());
            } catch (RuntimeException e) {
                ctx.log().warn("epa-uv {}: {}", place.id(), e.getMessage());
            }
            done++;
        }
        if (!anyZip) {
            ctx.state().degrade(NO_ZIPS);
        } else {
            ctx.state().clearDegraded();
        }
        return FetchResult.complete(out);
    }

    private NormalizedRecord fetchPlace(FetchContext ctx, PlacePoints placePoints, PlacePoints.Summary place, String zip)
            throws AdapterException {
        ZoneId zone = placePoints.timezone(place.id()).flatMap(t -> {
            try {
                return Optional.of(ZoneId.of(t));
            } catch (RuntimeException e) {
                return Optional.empty();
            }
        }).orElse(ZoneOffset.UTC);
        URI uri = URI.create((baseUrl.isEmpty() ? "" : baseUrl) + String.format(URL_PATTERN, zip));
        JsonNode root;
        try {
            root = JSON.readTree(ctx.http().get(uri).body());
        } catch (RuntimeException e) {
            ctx.rasters().reportFormatChange(KIND, "EPA UV response is not JSON: " + e.getMessage());
            return null;
        }
        if (!root.isArray()) {
            ctx.rasters().reportFormatChange(KIND, "EPA UV response is not an array");
            return null;
        }
        List<Map<String, Object>> hours = new ArrayList<>();
        int maxUv = -1;
        Instant maxAt = null;
        Instant first = null;
        Instant last = null;
        for (JsonNode row : root) {
            String time = text(row, "DATE_TIME");
            Integer uv = intOrNull(row, "UV_VALUE");
            if (time == null || uv == null) {
                continue;
            }
            Instant at = parseEpaTime(time, zone);
            if (at == null) {
                continue;
            }
            Map<String, Object> hour = new LinkedHashMap<>();
            hour.put("time", at.toString());
            hour.put("uv", uv);
            hours.add(hour);
            if (first == null || at.isBefore(first)) {
                first = at;
            }
            if (last == null || at.isAfter(last)) {
                last = at;
            }
            if (uv > maxUv) {
                maxUv = uv;
                maxAt = at;
            }
        }
        if (hours.isEmpty()) {
            return null;
        }
        NormalizedRecord.Builder b = NormalizedRecord.builder(place.id(), KIND)
                .observedAt(ctx.now())
                .point(place.lon(), place.lat())
                .put("zip", zip)
                .put("hours", hours)
                .put("maxUv", maxUv);
        if (maxAt != null) {
            b.put("maxAt", maxAt.toString());
        }
        if (first != null) {
            b.validFrom(first);
        }
        if (last != null) {
            b.validUntil(last);
        }
        return b.build();
    }

    static Instant parseEpaTime(String text, ZoneId zone) {
        try {
            LocalDateTime local = LocalDateTime.parse(text.strip(), EPA_TIME);
            return local.atZone(zone).toInstant();
        } catch (RuntimeException e) {
            return null;
        }
    }

    static String zip(PlacePoints places, String placeId) {
        Object zip = places.attributes(placeId).get("zip");
        return zip instanceof String s && ZIP.matcher(s).matches() ? s : null;
    }

    private static boolean due(AdapterStateStore state, String placeId, Instant now) {
        String next = state.get(nextKey(placeId)).orElse(null);
        if (next == null) {
            return true;
        }
        try {
            return !now.isBefore(Instant.parse(next));
        } catch (RuntimeException e) {
            return true;
        }
    }

    static String nextKey(String placeId) {
        return "next:" + placeId;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asString("");
    }

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || !v.isNumber() ? null : v.asInt();
    }

    private static SourceDescriptor buildDescriptor() {
        SourceDescriptor.Builder b = SourceDescriptor.builder(ID)
                .name("EPA UV hourly forecast")
                .description("The EPA Envirofacts hourly UV index forecast for each place with a zip attribute, one"
                        + " request per place per 6 hours, in the place's timezone.")
                .kind(KIND)
                .homepage("https://www.epa.gov/enviro/envirofacts-data-service-api")
                .attribution("US Environmental Protection Agency (Envirofacts)")
                .licence("US-PD")
                .termsUrl("https://www.epa.gov/")
                .mode(Mode.POLLING)
                .interval(Duration.ofMinutes(30), Duration.ofMinutes(30))
                .defaultBudget(Budget.of(10, 500))
                .historyTier(HistoryTier.STANDARD)
                .observedAtMode(ObservedAtMode.RECEIVED)
                .recordTtl(null)
                .defaultRetention(Duration.ofDays(7))
                .kindRetention(KIND, Duration.ofDays(7))
                .zeroResultsNormal(true)
                .maxResponseBytes(MAX_RESPONSE_BYTES)
                .titleField("zip")
                .param(ParamSpec.optional(MAX_PLACES_PARAM, ParamType.INTEGER,
                        "The most places fetched per run (1 to 50)", MAX_PLACES_PER_RUN));
        b.fieldSpec("zip", new ai.foresignal.source.spi.FieldSpec(ai.foresignal.source.spi.FieldType.STRING, null,
                "ZIP", "The place's US ZIP code", null, java.util.Set.of()));
        b.fieldSpec("maxUv", new ai.foresignal.source.spi.FieldSpec(ai.foresignal.source.spi.FieldType.INTEGER, null,
                "Max UV", "The day's peak UV index", null, java.util.Set.of()));
        return b.build();
    }

    /** Unused; retained for a future per-place timezone lookup. */
    static Optional<String> unused() {
        return Optional.empty();
    }
}
